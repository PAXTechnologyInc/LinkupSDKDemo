package com.pax.linkupsdk.demo;

import static com.pax.linkupsdk.demo.ViewLog.addLog;

import android.content.Context;
import android.graphics.Bitmap;
import android.text.TextUtils;

import com.pax.linkdata.LinkDevice;

import java.io.ByteArrayOutputStream;
import java.util.List;

public class Tools {
    private Tools() {
        throw new IllegalStateException("Tools class");
    }
//    private static List<LinkDevice> mDeviceList = new ArrayList<>();

//    public static LinkDevice getSelectedDevice(Activity activity, ArrayList<LinkDevice> list) {
//        LinkDevice device = null;
//        TextView textView = activity.findViewById(R.id.tv_device_selected);
//        String name1 = textView.getText().toString();
//        String name = name1.replace(",", "");
//        for (LinkDevice deviceInfo : list) {
//            if (deviceInfo.getDeviceName().equals(name)) {
//                device = deviceInfo;
//            }
//        }
//        if (device == null) {
//            return null;
//        }
//        return device;
//    }

    public static boolean checkNoDevice() {
        if (DemoApplication.getSelectedDeviceList().isEmpty()) {
            addLog("please select device");
            return true;
        }
        return false;
    }

    public static boolean checkNoScanner() {
        if (TextUtils.isEmpty(DemoApplication.getSelectedScannerID())) {
            addLog("please select scannerID");
            return true;
        }
        return false;
    }

    public static boolean checkNoPrinter() {
        if (TextUtils.isEmpty(DemoApplication.getSelectedPrinterID())) {
            addLog("please select printerID");
            return true;
        }
        return false;
    }

    public static LinkDevice getSelfInfo(Context context, List<LinkDevice> deviceList) {
        for (LinkDevice linkDevice : deviceList) {
            if (linkDevice.isDeviceSelf(context)) {
                return linkDevice;
            }
        }
        return null;
    }

    public static boolean checkNoFile() {
        if (DemoApplication.getSelectedFileList().isEmpty()) {
            addLog("please select file");
            return true;
        }
        return false;
    }

    public static boolean checkNoTargetFile() {
        if (TextUtils.isEmpty(DemoApplication.getTargetFilePath())) {
            addLog("please input target file path");
            return true;
        }
        return false;
    }

    public static boolean checkNoFileTypeFile() {
        if (TextUtils.isEmpty(DemoApplication.getFileType())) {
            addLog("please select file type");
            return true;
        }
        return false;
    }

    public static String listToString(List<LinkDevice> linkDeviceList) {
        StringBuilder sb = new StringBuilder();
        for (LinkDevice linkDevice : linkDeviceList) {
            sb.append(linkDevice.getDeviceName());
            sb.append(",");
        }
        return sb.toString();
    }

    public static byte[] bitmap2Byte(Bitmap bm) {
        if (bm == null) {
            return null;
        }
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
        bm.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
        return byteArrayOutputStream.toByteArray();
    }
}
