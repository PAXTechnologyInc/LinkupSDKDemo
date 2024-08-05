package com.pax.linkupsdk.demo.module;

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

import com.pax.linkdata.deviceinfo.component.Printer;
import com.pax.linkdata.deviceinfo.component.Scanner;
import com.pax.linkupsdk.demo.R;
import com.pax.egarden.devicekit.DeviceHelper;
import com.pax.linkdata.LinkDevice;
import com.pax.linkdata.cmd.LinkException;

import java.util.List;

import static com.pax.linkupsdk.demo.ViewLog.addErrLog;
import static com.pax.linkupsdk.demo.ViewLog.addLog;

public class DeviceFragment extends Fragment {
    private Context mContext;
    private DeviceHelper mDeviceHelper;

    private static final String[] mListInfo = new String[]{
            "getSelfDeviceInfo",
            "queryOnlineDeviceList",
            "queryOfflineDeviceList",
            "registerStatusListener",
            "unregisterStatusListener",
    };

    public DeviceFragment(Context context) {
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
                    getSelfDeviceInfo();
                    break;
                case 1:
                    queryOnlineDeviceList();
                    break;
                case 2:
                    queryOfflineDeviceList();
                    break;
                case 3:
                    registerStatusListener();
                    break;
                case 4:
                    unregisterStatusListener();
                    break;
                default:
                    break;
            }
        });
        return fragmentView;
    }

    private String toString(LinkDevice linkDevice) {
        return "{" +
                "deviceID='" + linkDevice.getDeviceID() + '\'' +
                ", deviceName='" + linkDevice.getDeviceName() + '\'' +
                ", deviceModel='" + linkDevice.getDeviceModel() + '\'' +
                ", connectionType='" + linkDevice.getConnectionType() + '\'' +
                ", groupOwnerID='" + linkDevice.getGroupOwnerID() + '\'' +
                ", isUserDefinedCenterNode='" + linkDevice.isUserDefinedCenterNode() + '\'' +
                ", linkIP='" + linkDevice.getLinkIP() + '\'' +
                ", firmwareVersion='" + linkDevice.getFirmwareVersion() + '\'' +
                '}';
    }

    private void getSelfDeviceInfo() {
        try {
            LinkDevice linkDevice = mDeviceHelper.getSelfDeviceInfo();
            addLog("getSelfDeviceInfo OK, self device info:" + toString(linkDevice));
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("getSelfDeviceInfo failed", e);
        }
    }

    private void queryOnlineDeviceList() {
        try {
            List<LinkDevice> linkDeviceList = mDeviceHelper.queryOnlineDeviceList();
            addLog("query done, total size:" + linkDeviceList.size());
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
            addErrLog("queryOnlineDeviceList failed", e);
        }
    }

    private void queryOfflineDeviceList() {
        try {
            List<LinkDevice> linkDeviceList = mDeviceHelper.queryOfflineDeviceList();
            addLog("query done, total size:" + linkDeviceList.size());
            for (LinkDevice linkDevice : linkDeviceList) {
                addLog("DeviceName:" + linkDevice.getDeviceName() + " DeviceID:" + linkDevice.getDeviceID());
            }
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("queryOfflineDeviceList failed", e);
        }
    }

    DeviceHelper.IStatusListener listener = new DeviceHelper.IStatusListener() {
        @Override
        public void onConnectDevices(List<LinkDevice> infoList) {
            addLog("onConnectDevices deviceID:" + infoList.get(0).getDeviceID());
        }

        @Override
        public void onDisconnectDevices(List<LinkDevice> infoList) {
            addLog("onDisconnectDevices deviceID:" + infoList.get(0).getDeviceID());
        }

        @Override
        public void onOnlineDevices(List<LinkDevice> infoList) {
            addLog("onOnlineDevices deviceID:" + infoList.get(0).getDeviceID());
        }

        @Override
        public void onOfflineDevices(List<LinkDevice> infoList) {
            addLog("onOfflineDevices deviceID:" + infoList.get(0).getDeviceID());
        }

        @Override
        public void onUpdateDevices(List<LinkDevice> infoList) {
            addLog("onUpdateDevices deviceID:" + infoList.get(0).getDeviceID());
        }
    };

    private void registerStatusListener() {
        try {
            mDeviceHelper.registerStatusListener(listener);
            addLog("registerStatusListener ok");
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("registerStatusListener failed", e);
        }
    }

    private void unregisterStatusListener() {
        try {
            mDeviceHelper.unregisterStatusListener(listener);
            addLog("unregisterStatusListener ok");
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("registerStatusListener failed", e);
        }
    }
}
