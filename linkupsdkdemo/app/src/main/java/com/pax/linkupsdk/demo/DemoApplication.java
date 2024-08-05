package com.pax.linkupsdk.demo;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;

import com.pax.linkdata.LinkDevice;

import java.util.ArrayList;
import java.util.List;

public class DemoApplication extends Application {
    private static List<LinkDevice> mOnlineDeviceList = new ArrayList<>(); //在线设备列表
    private static List<LinkDevice> mSelectedDeviceList = new ArrayList<>(); //已选择的设备列表
    private static LinkDevice mLinkDeviceSelf;      //本设备信息
    private static List<String> mSelectedFileList = new ArrayList<>(); //已选择的文件列表
    private static String mSelectedPrinterID;
    private static String mSelectedScannerID;
    private static String mSelectedComponentID;
    private static LinkDevice mRecordDevice = null;

    private static String targetFilePath;
    private static String fileType;

    public static List<LinkDevice> getOnlineDeviceList() {
        return mOnlineDeviceList;
    }

    public static void setLinkDeviceSelf(LinkDevice linkDevice) {
        mLinkDeviceSelf = linkDevice;
    }

    public static LinkDevice getLinkDeviceSelf() {
        return mLinkDeviceSelf;
    }

    public static List<LinkDevice> getSelectedDeviceList() {
        return mSelectedDeviceList;
    }

    public static String getSelectedPrinterID() {
        return mSelectedPrinterID;
    }

    public static void setSelectedPrinterID(String selectedPrinterID) {
        DemoApplication.mSelectedPrinterID = selectedPrinterID;
    }

    public static String getSelectedScannerID() {
        return mSelectedScannerID;
    }

    public static void setSelectedScannerID(String mSelectedScannerID) {
        DemoApplication.mSelectedScannerID = mSelectedScannerID;
    }

    public static String getSelectedComponentID() {
        return mSelectedComponentID;
    }

    public static void setSelectedComponentID(String selectedPrinterID) {
        DemoApplication.mSelectedComponentID = selectedPrinterID;
    }

    public static List<String> getSelectedFileList() {
        return mSelectedFileList;
    }


    public static void setRecordDevice(LinkDevice recordDevice) {
        DemoApplication.mRecordDevice = recordDevice;
    }

    public static LinkDevice getRecordDevice() {
        return mRecordDevice;
    }

    public static String getTargetFilePath() {
        return targetFilePath;
    }

    public static void setTargetFilePath(String targetFilePath) {
        DemoApplication.targetFilePath = targetFilePath;
    }

    public static String getFileType() {
        return fileType;
    }

    public static void setFileType(String fileType) {
        DemoApplication.fileType = fileType;
    }

    public static boolean isPortDisplay(Context c) {
//        WindowManager manager = this.getWindowManager();
//        DisplayMetrics outMetrics = new DisplayMetrics();
//        manager.getDefaultDisplay().getMetrics(outMetrics);
//        return (outMetrics.heightPixels > outMetrics.widthPixels);
        Configuration mConfiguration = c.getResources().getConfiguration(); //获取设置的配置信息
        return mConfiguration.orientation == mConfiguration.ORIENTATION_PORTRAIT;
    }
}
