package com.pax.linkupsdk.demo.module;

import android.app.AlertDialog;
import android.content.Context;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.util.Base64;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pax.egarden.devicekit.PrinterHelper;
import com.pax.linkdata.LinkDevice;
import com.pax.linkdata.PrintParam;
import com.pax.linkdata.ResultListener;
import com.pax.linkdata.cmd.LinkException;
import com.pax.linkupsdk.demo.DemoApplication;
import com.pax.linkupsdk.demo.R;
import com.pax.linkupsdk.demo.Tools;
import com.pax.linkupsdk.demo.WorkExecutor;
import com.pax.linkdata.cmd.printer.CommandChannelRequestContent;
import com.pax.linkdata.deviceinfo.component.Printer;
import com.pax.util.LogUtil;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.UnsupportedEncodingException;
import java.util.List;

import static com.pax.linkupsdk.demo.Tools.checkNoDevice;
import static com.pax.linkupsdk.demo.Tools.checkNoFile;
import static com.pax.linkupsdk.demo.Tools.checkNoPrinter;
import static com.pax.linkupsdk.demo.ViewLog.addErrLog;
import static com.pax.linkupsdk.demo.ViewLog.addLog;

public class PrinterFragment extends Fragment {
    private Context mContext;
    private PrinterHelper mPrinterHelper;

    private static final String[] mListInfo = new String[]{
            "queryPrinterInfoList",
            "sendRawCommand",
            "printImage",
    };

    public PrinterFragment(Context context) {
        this.mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mPrinterHelper = PrinterHelper.getInstance(mContext);
        View view = inflater.inflate(R.layout.fragment_right, null);
        GridView gridView = view.findViewById(R.id.gv_function);
        gridView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.gridview_layoutres_btn, mListInfo));
        gridView.setOnItemClickListener((parent, view1, position, id) -> {
            switch (position) {
                case 0:
                    queryPrinterInfoListTest();
                    break;
                case 1:
                    sendRawCommandTest();
                    break;
                case 2:
                    WorkExecutor.execute(PrinterFragment.this::printImageTest);
                    break;
                default:
                    break;
            }
        });
        return view;
    }

    private void queryPrinterInfoListTest() {
        try {
            List<LinkDevice> list = mPrinterHelper.queryPrinterInfoList();
            addLog("query done");
            if (list != null) {
//                List<String> nameList = new ArrayList<>();
                for (LinkDevice linkDevice : list) {
                    for (Printer printer : linkDevice.getPrinterList()) {
                        LogUtil.d("DeviceName:" + linkDevice.getDeviceName()
                                + ", DeviceID:" + linkDevice.getDeviceID()
                                + " ComponentID:" + printer.getComponentID()
                                + " model:" + printer.getModel());
                        addLog("DeviceName:" + linkDevice.getDeviceName()
                                + ", DeviceID:" + linkDevice.getDeviceID()
                                + " ComponentID:" + printer.getComponentID()
                                + " model:" + printer.getModel());
                    }
                }
//                addLog(nameList.toString());
            }
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("queryPrinterInfoList failed", e);
        }
    }

    private long starttime;

    private void printImageTest() {
        final List<LinkDevice> linkDeviceList = DemoApplication.getSelectedDeviceList();
        if (checkNoDevice()) {
            return;
        }
        if (checkNoFile()) {
            return;
        }

        if (checkNoPrinter()) {
            return;
        }

        PrintParam printParam = new PrintParam();
        printParam.feedLen = 120;
        printParam.cutMode = 0;
        ResultListener mListener = new ResultListener() {
            @Override
            public void onSuccess() {
                addLog("ResultListener print success, cost time:" + (System.currentTimeMillis() - starttime) + "ms");
            }

            @Override
            public void onFailed(String s) {
                addLog("ResultListener print failed==>" + s);
            }
        };


        byte[] bytes = null;
        try {
            FileInputStream stream = new FileInputStream(DemoApplication.getSelectedFileList().get(0));
            bytes = Tools.bitmap2Byte(BitmapFactory.decodeStream(stream));
            if (bytes == null) {
                addLog("Please select a valid image file.");
                return;
            }
        } catch (FileNotFoundException e) {
            LogUtil.d("FileNotFoundException:", e.toString());
            e.printStackTrace();
            return;
        }

        LinkDevice linkDevice = linkDeviceList.get(0);
        linkDevice.setCurrentComponentID(DemoApplication.getSelectedPrinterID());
        try {
            printParam.printData = bytes;
            starttime = System.currentTimeMillis();
            addLog("start print, " + DemoApplication.getSelectedFileList().get(0));
            mPrinterHelper.printImage(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID(), printParam, mListener);
        } catch (LinkException e) {
            e.printStackTrace();
            addLog("throw LinkException error code: " + e.getErrCode() + "error msg: " + e.getErrMsg());
        }
    }

    private void sendRawCommandTest() {
        if (checkNoDevice()) {
            return;
        }
        if (checkNoPrinter()) {
            return;
        }

        //显示内容对话框
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(mContext);
        inputDialog.setTitle(R.string.input_print_content);
        View view = View.inflate(mContext, R.layout.layout_input_diaglog, null);
        inputDialog.setView(view);
        EditText editText = view.findViewById(R.id.txt_content);
        inputDialog.setPositiveButton(R.string.sure, (dialog, which) -> {
            if (editText.getText().length() == 0) {
                Toast.makeText(mContext, "please input effective content", Toast.LENGTH_LONG).show();
                return;
            }

            WorkExecutor.execute(() -> {
                try {
                    CommandChannelRequestContent requestContent = new CommandChannelRequestContent();
                    LinkDevice linkDevice = DemoApplication.getSelectedDeviceList().get(0);
                    linkDevice.setCurrentComponentID(DemoApplication.getSelectedPrinterID());
                    requestContent.setCmdCaller("DemoTest");
                    requestContent.setSendTimeout(10000);
                    requestContent.setRecvTimeout(10000);
                    requestContent.setLastCmd(false);
                    requestContent.setExpRecvLen(0);
                    requestContent.setCmdRequestData(Base64.encodeToString(new byte[]{0x1B, 0x40}, Base64.NO_WRAP));
                    mPrinterHelper.sendRawCommand(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID(), requestContent);

                    requestContent.setCmdRequestData(Base64.encodeToString(editText.getText().toString().getBytes("GBK"), Base64.NO_WRAP));
                    mPrinterHelper.sendRawCommand(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID(), requestContent);

                    requestContent.setLastCmd(true);
                    requestContent.setCmdRequestData(Base64.encodeToString(new byte[]{0x0D, 0x0A}, Base64.NO_WRAP));
                    mPrinterHelper.sendRawCommand(linkDevice.getDeviceID(), DemoApplication.getSelectedPrinterID(), requestContent);
                } catch (LinkException e) {
                    e.printStackTrace();
                    addErrLog("sendRawCommand failed", e);
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
            });

            dialog.dismiss();
        }).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss()).show();
    }
}
