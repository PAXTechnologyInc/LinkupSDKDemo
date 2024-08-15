package com.pax.linkupsdk.demo;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import com.pax.egarden.devicekit.LinkUpSdk;
import com.pax.linkdata.InitListener;
import com.pax.util.LogUtil;
import com.pax.egarden.devicekit.DeviceHelper;
import com.pax.linkdata.LinkDevice;
import com.pax.linkdata.cmd.LinkException;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        initLogger();
        setContentView(R.layout.activity_main);

        //提前初始化AIDL服务，同时要保证主线程在空闲状态才能很快成功
        //DeviceHelper.getInstance(getApplicationContext()).getSelfDeviceInfo();

        //SDK初始化，主要是连接AIDL服务
        LinkUpSdk.getInstance(getApplicationContext()).init(new InitListener() {
            @Override
            public void onSuccess() {
                LogUtil.d("init onSuccess");
                requestPermission();
                initDeviceInfo();
                jumpToHome(Constant.DEV_CON);
            }

            @Override
            public void onFailed(String s) {
                LogUtil.d("init onFailed");
            }
        });



    }



    @Override
    protected void onDestroy() {
        super.onDestroy();
    }

    /**
     * 申请权限
     */
    private void requestPermission() {
        String[] permissions = {
                Manifest.permission.WRITE_EXTERNAL_STORAGE,
//                Manifest.permission.ACCESS_FINE_LOCATION,
        };
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            List<String> mPermissionList = new ArrayList<>();

            for (int i = 0; i < permissions.length; i++) {
                if (ContextCompat.checkSelfPermission(this, permissions[i]) != PackageManager.PERMISSION_GRANTED) {
                    LogUtil.d("start request for permission:" + permissions[i]);
                    mPermissionList.add(permissions[i]);
                } else {
                    LogUtil.d("already grant permission:" + permissions[i]);
                }
            }

            if (!mPermissionList.isEmpty()) {
                String[] permissionsArr = mPermissionList.toArray(new String[mPermissionList.size()]);
                ActivityCompat.requestPermissions(this, permissionsArr, 1);
            }
        }
    }

    /**
     * 申请权限反馈
     *
     * @param requestCode
     * @param permissions
     * @param grantResults
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        for (int i = 0; i < grantResults.length; i++) {
            LogUtil.d("request permission " + permissions[i] + ", result:" + grantResults[i]);
            if (grantResults[i] != PackageManager.PERMISSION_GRANTED) {
                //判断是否勾选禁止后不再询问
                boolean showRequestPermission = ActivityCompat.shouldShowRequestPermissionRationale(this, permissions[i]);
                if (showRequestPermission) {
                    requestPermission();
                    return;
                } else { // false 被禁止了，不在访问
                    LogUtil.d("fail to request for permission");
                }
            }
        }
        jumpToHome(Constant.DEV_CON);
    }

    private void initDeviceInfo() {
        try {
            List<LinkDevice> deviceList = DeviceHelper.getInstance(getApplicationContext()).queryOnlineDeviceList();
            deviceList.add(DeviceHelper.getInstance(getApplicationContext()).getSelfDeviceInfo());
            for (LinkDevice linkDevice : deviceList) {
                if (!DemoApplication.getOnlineDeviceList().contains(linkDevice)) {
                    DemoApplication.getOnlineDeviceList().add(linkDevice);
                }
                if (linkDevice.isDeviceSelf(getApplicationContext())) {
                    DemoApplication.setLinkDeviceSelf(linkDevice);
                }
            }
        } catch (LinkException e) {
            e.printStackTrace();
        }
    }

    private void jumpToHome(String title) {
        Intent intent = new Intent(this, HomeActivity.class);
        intent.putExtra("title", title);
        startActivity(intent);
        finish();
//        LogUtil.d("jumpToHome, title: " + title);
    }

    private void initLogger() {
        //sTAG :日志默认TAG    bLogcat: 是否在控制台上输出  writeFile：是否保存更多的更多的日志，默认记录Error级别日志 fileName modlueName_MMddyyyy_HHmmss.log
        LogUtil.init(getApplicationContext(), "LinkUpSDKDemo", true, false, "LinkUpSDKDemo");
    }

    @Override
    public Resources getResources() {
        Resources res = super.getResources();
        Configuration config = res.getConfiguration();
        if (config.fontScale != 1) {// 非默认值
            config.fontScale = 1;
            res.updateConfiguration(config, res.getDisplayMetrics());
        }
        return res;
    }
}
