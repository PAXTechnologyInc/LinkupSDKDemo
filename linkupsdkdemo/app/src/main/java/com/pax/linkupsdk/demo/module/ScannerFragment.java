package com.pax.linkupsdk.demo.module;

import android.content.Context;
import android.os.Bundle;
import android.os.ConditionVariable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import static com.pax.linkupsdk.demo.Tools.checkNoDevice;
import static com.pax.linkupsdk.demo.Tools.checkNoScanner;
import static com.pax.linkupsdk.demo.ViewLog.addErrLog;
import static com.pax.linkupsdk.demo.ViewLog.addLog;


import com.pax.linkupsdk.demo.DemoApplication;
import com.pax.linkupsdk.demo.R;
import com.pax.linkupsdk.demo.WorkExecutor;
import com.pax.egarden.devicekit.ScannerHelper;
import com.pax.linkdata.LinkDevice;
import com.pax.linkdata.ScannerDataListener;
import com.pax.linkdata.cmd.LinkException;
import com.pax.linkdata.deviceinfo.component.Scanner;

import com.pax.util.LogUtil;

import java.util.List;

public class ScannerFragment extends Fragment {
    private Context mContext;
    private ScannerHelper mScannerHelper;

    private static final String[] mListInfo = new String[]{
            "queryScannerInfoList",
            "startScan",
            "stopScan",
            "registerAutoScanDataListener",
            "unregisterAutoScanDataListener",
            "setParam_dataMode",
            "setParam_scanMode",
            "setParam_positionLamp",
            "setParam_supplementLight",
            "setParam_scanLED",
            "setParam_sameInterval",
            "setParam_promptTone",
            "getParam",
    };

    public ScannerFragment(Context context) {
        this.mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mScannerHelper = ScannerHelper.getInstance(mContext);
        View fragmentView = inflater.inflate(R.layout.fragment_right, null);
        GridView gridView = fragmentView.findViewById(R.id.gv_function);
        gridView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.gridview_layoutres_btn, mListInfo));
        gridView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            switch (position) {
                case 0:
                    queryScannerInfoList();
                    break;
                case 1:
                    WorkExecutor.execute(ScannerFragment.this::startScan);
                    break;
                case 2:
                    WorkExecutor.execute(ScannerFragment.this::stopScan);
                    break;
                case 3:
                    registerAutoScanDataListener();
                    break;
                case 4:
                    unregisterAutoScanDataListener();
                    break;
                case 5:
                    setParam("dataMode");
                    break;
                case 6:
                    setParam("scanMode");  //Single/Continuous/Auto
                    break;
                case 7:
                    setParam("positionLamp");
                    break;
                case 8:
                    setParam("supplementLight");
                    break;
                case 9:
                    setParam("scanLED");
                    break;
                case 10:
                    setParam("sameInterval");
                    break;
                case 11:
                    setParam("promptTone");
                    break;
                case 12:
                    WorkExecutor.execute(ScannerFragment.this::getParamTest);
                    break;
                default:
                    break;
            }
        });
        return fragmentView;
    }

    public void registerAutoScanDataListener() {
        if (checkNoDevice()) {
            return;
        }
        LinkDevice linkDevice = DemoApplication.getSelectedDeviceList().get(0);
        linkDevice.setCurrentComponentID(DemoApplication.getSelectedScannerID());
        try {
            mScannerHelper.registerAutoScanDataListener(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID(), mMessageListener);
            addLog("registerAutoScanDataListener successful");
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("registerAutoScanDataListener failed", e);
        }
    }

    public void unregisterAutoScanDataListener() {
        if (checkNoDevice()) {
            return;
        }
        LinkDevice linkDevice = DemoApplication.getSelectedDeviceList().get(0);
        linkDevice.setCurrentComponentID(DemoApplication.getSelectedScannerID());
        try {
            mScannerHelper.unregisterAutoScanDataListener(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID());
            addLog("unregisterAutoScanDataListener successful");
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("unregisterAutoScanDataListener failed", e);
        }
    }

    public void queryScannerInfoList() {
        try {
            List<LinkDevice> list = mScannerHelper.queryScannerInfoList();
            addLog("query done");
            if (list != null) {
                for (LinkDevice linkDevice : list) {
                    for (Scanner scanner : linkDevice.getScannerList()) {
                        LogUtil.d("DeviceName:" + linkDevice.getDeviceName()
                                + ", DeviceID:" + linkDevice.getDeviceID()
                                + " CompinentID:" + scanner.getComponentID()
                                + " model:"+ scanner.getModel());
                        addLog("DeviceName:" + linkDevice.getDeviceName()
                                + ", DeviceID:" + linkDevice.getDeviceID()
                                + " CompinentID:" + scanner.getComponentID()
                                + " model:"+ scanner.getModel());
                    }
                }
            }
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("queryScannerInfoList failed", e);
        }
    }

    final ConditionVariable cv = new ConditionVariable(false);
    private ScannerDataListener mMessageListener = new ScannerDataListener() {
        @Override
        public void onMessage(int code, String message, String listenerOwner) {
            addLog("onMessage code:" + code + ", message:" + message + ", listenerOwner:" + listenerOwner);
            cv.open();
        }
    };

    private void startScan() {
        if (checkNoDevice()) {
            return;
        }
        if (checkNoScanner()) {
            return;
        }

        LinkDevice linkDevice = DemoApplication.getSelectedDeviceList().get(0);
        linkDevice.setCurrentComponentID(DemoApplication.getSelectedScannerID());
        LogUtil.d("device id:" + linkDevice.getDeviceID() + ",scanner id:" + linkDevice.getCurrentComponentID());
        try {
            mScannerHelper.startScan(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID(), 10000, mMessageListener);
            addLog("startScan success,");
        } catch (LinkException e) {
            e.printStackTrace();
            if (e.getErrCode() == LinkException.ERR_SCANNER_IS_USED) {
                addLog("listener is exist, start closeScanner");
                try {
                    mScannerHelper.stopScan(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID());
                    mScannerHelper.startScan(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID(), 10000, mMessageListener);
                    addLog("startScan success,");
                } catch (LinkException ex) {
                    ex.printStackTrace();
                    addErrLog("startScan failed", e);
                }
            } else {
                addErrLog("startScan failed", e);
            }
        }
//        cv.block();
//        cv.close();

//        stopScanTest();
    }

    private void stopScan() {
        if (checkNoDevice()) {
            return;
        }

        if (checkNoScanner()) {
            return;
        }

        LinkDevice linkDevice = DemoApplication.getSelectedDeviceList().get(0);
        linkDevice.setCurrentComponentID(DemoApplication.getSelectedScannerID());
        try {
            mScannerHelper.stopScan(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID());
            addLog("stopScan success,");
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("stopScan failed", e);
        }
    }

    /**
     * 将一台指定的设备加入到本Group中，本设备为Group Owner
     * //"dataMode=Data&scanMode=Single&positionLamp=true&supplementLight=true&promptTone=true&scanLED=true&sameInterval=1000");
     */
    private int index = 0;

    private void setParam(String key) {
        WorkExecutor.execute(() -> {
            if (checkNoDevice()) {
                return;
            }
            if (checkNoScanner()) {
                return;
            }
            String value = "";
            LinkDevice linkDevice = DemoApplication.getSelectedDeviceList().get(0);
            linkDevice.setCurrentComponentID(DemoApplication.getSelectedScannerID());
            try {
                if (key.equals("scanMode")) {
                    if (index % 3 == 0) {
                        value = "Single";
                    } else if (index % 3 == 1) {
                        value = "Continuous";
                    } else if (index % 3 == 2) {
                        value = "Auto";
                    }
                } else if (key.equals("dataMode")) {
                    if (index % 2 == 0) {
                        value = "Data";
                    } else if (index % 2 == 1) {
                        value = "KeyEvent";
                    }
                } else if (key.equals("sameInterval")) {
                    if (index % 3 == 0) {
                        value = "1000";
                    } else if (index % 3 == 1) {
                        value = "2000";
                    } else if (index % 3 == 2) {
                        value = "3000";
                    }
                } else {
                    //boolean
                    if (index % 2 == 0) {
                        value = "true";
                    } else if (index % 2 == 1) {
                        value = "false";
                    }
                }

                mScannerHelper.setParam(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID(), key + "=" + value);
                addLog("setParam + " + key + "=" + value + " OK");
                index++;
            } catch (LinkException e) {
                e.printStackTrace();
                addErrLog("setParam" + key + "=" + value + " failed:", e);
            }
        });
    }

//    private void setParamTest1(String paramList) {
//        WorkExecutor.execute(new Runnable() {
//            @Override
//            public void run() {
//                if (checkNoDevice()) {
//                    return;
//                }
//                String value = "";
//                LinkDevice linkDevice = DemoApplication.getSelectedDeviceList().get(0);
//                linkDevice.setCurrentComponentID(DemoApplication.getSelectedScannerID());
//                try {
//                    mScannerHelper.setParam(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID(), paramList);
//                    addLog("setParam " + paramList + " OK");
//                } catch (LinkException e) {
//                    e.printStackTrace();
//                    addErrLog("setParam" + paramList + " failed:", e);
//                }
//            }
//        });
//    }


    private void getParamTest() {
        if (checkNoDevice()) {
            return;
        }
        if (checkNoScanner()) {
            return;
        }
        LinkDevice linkDevice = DemoApplication.getSelectedDeviceList().get(0);
        linkDevice.setCurrentComponentID(DemoApplication.getSelectedScannerID());
        try {
            String value = mScannerHelper.getParam(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID(), "dataMode&scanMode&positionLamp&supplementLight&sameInterval&promptTone");
//            String value = mScannerHelper.getParam(linkDevice.getDeviceID(),linkDevice.getCurrentComponentID(), "scannerName");
            addLog("getParam OK:" + value);
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("getParam failed", e);
        }
    }
}
