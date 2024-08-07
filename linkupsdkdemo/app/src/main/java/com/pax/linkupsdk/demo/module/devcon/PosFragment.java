package com.pax.linkupsdk.demo.module.devcon;


import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.alibaba.fastjson.JSON;
import com.pax.linkdata.deviceinfo.component.Printer;
import com.pax.linkdata.deviceinfo.component.Scanner;
import com.pax.linkupsdk.demo.R;
import com.pax.egarden.devicekit.DeviceHelper;
import com.pax.linkdata.LinkDevice;
import com.pax.linkdata.cmd.LinkException;
import com.pax.poslink.CommSetting;
import com.pax.poslink.PaymentRequest;
import com.pax.poslink.PosLink;
import com.pax.poslink.ProcessTransResult;
import com.pax.poslink.constant.EDCType;
import com.pax.poslink.constant.TransType;
import com.pax.poslink.poslink.POSLinkCreator;

import java.util.List;
import java.util.Random;

import static com.pax.linkupsdk.demo.ViewLog.addErrLog;
import static com.pax.linkupsdk.demo.ViewLog.addLog;

public class PosFragment extends Fragment {
    private Context mContext;
    private DeviceHelper mDeviceHelper;

    private static final String[] mListInfo = new String[]{
            "Detect Devices",
            "Scan SKU",
            "Payment",
            "Print Receipt",
    };

    public PosFragment(Context context) {
        this.mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mDeviceHelper = DeviceHelper.getInstance(mContext);
        View fragmentView = inflater.inflate(R.layout.fragment_right, null);
        GridView gridView = fragmentView.findViewById(R.id.gv_function);
        gridView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.gridview_layoutres_btn, mListInfo));
        gridView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            switch (position) {
                case 0:
                    queryOnlineDeviceList();
                    break;
                case 1:
//                    queryOnlineDeviceList();
                    break;
                case 2:
                    pay();
                    break;
                case 3:
//                    registerStatusListener();
                    break;
                default:
                    break;
            }
        });
        return fragmentView;
    }

    private void queryOnlineDeviceList() {
        try {
            List<LinkDevice> linkDeviceList = mDeviceHelper.queryOnlineDeviceList();
            addLog("Total devices detected: " + linkDeviceList.size());
            for (LinkDevice linkDevice : linkDeviceList) {
                StringBuilder scannerStr = new StringBuilder();
                StringBuilder printerStr = new StringBuilder();
                StringBuilder componentStr = new StringBuilder();
                if(linkDevice.isDeviceSelf(getContext())) {
                    for (Scanner scanner : linkDevice.getScannerList()) {
                        if (!TextUtils.isEmpty(scanner.getSn())) {
                            scannerStr.append(scanner.getSn());
                            scannerStr.append(";");
                        }
                    }
                    for (Printer printer : linkDevice.getPrinterList()) {
                        if (!TextUtils.isEmpty(printer.getSn())) {
                            printerStr.append(printer.getSn());
                            printerStr.append(";");
                        }
                    }
                    if (scannerStr.length() > 0) {
                        componentStr.append(" \n\t\tScannerList:").append(scannerStr);
                    }
                    if (printerStr.length() > 0) {
                        componentStr.append(" \n\t\tPrinterList:").append(printerStr);
                    }
                }
                addLog("DeviceName:" + linkDevice.getDeviceName() + " DeviceID:" + linkDevice.getDeviceID() + componentStr);
            }
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("[queryOnlineDeviceList] detect devices failed", e);
        }
    }

    private void pay(){
        System.out.println("Pressed pay btn");

            new Thread(() -> {
                // init poslink
                PosLink poslink = POSLinkCreator.createPoslink(getContext());
                CommSetting commSetting = new CommSetting();
                commSetting.setType(CommSetting.TCP);
                commSetting.setDestIP("10.1.47.199");
                commSetting.setDestPort("10009");
                commSetting.setTimeOut("60000");
                commSetting.setEnableProxy(true);
                poslink.SetCommSetting(commSetting);

                // init request
                PaymentRequest request = new PaymentRequest();
                request.Amount = "100";
                request.TenderType = request.ParseTenderType(EDCType.CREDIT);
                request.TransType = request.ParseTransType(TransType.SALE);
                request.TipAmt = "0";
                request.TaxAmt = "0";
                Random random = new Random();
                long min = 100_000_000_000L; // Minimum 12-digit number
                long max = 999_999_999_999L; // Maximum 12-digit number
                long range = max - min + 1;
                long refNum = min + (long) (random.nextDouble() * range);
                request.ECRRefNum = String.valueOf(refNum);
                poslink.PaymentRequest = request;

                // transact
                ProcessTransResult result = poslink.ProcessTrans();
                addLog(JSON.toJSONString(result));
                addLog("result code: " + result.Code);
                addLog("result MSG: " + result.Msg);
            }).start();
    }

}
