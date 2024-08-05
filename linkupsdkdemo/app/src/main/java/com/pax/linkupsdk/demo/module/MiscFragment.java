package com.pax.linkupsdk.demo.module;

import static com.pax.linkupsdk.demo.Tools.checkNoDevice;
import static com.pax.linkupsdk.demo.Tools.checkNoFile;
import static com.pax.linkupsdk.demo.Tools.checkNoFileTypeFile;
import static com.pax.linkupsdk.demo.ViewLog.addErrLog;
import static com.pax.linkupsdk.demo.ViewLog.addLog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.EditText;
import android.widget.GridView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pax.egarden.devicekit.FileHelper;
import com.pax.linkdata.IExchangeDataListener;
import com.pax.linkupsdk.demo.DemoApplication;
import com.pax.linkupsdk.demo.R;
import com.pax.linkupsdk.demo.WorkExecutor;
import com.pax.egarden.devicekit.MiscHelper;
import com.pax.linkdata.AppInfo;
import com.pax.linkdata.LinkDevice;
import com.pax.linkdata.cmd.LinkException;
import com.pax.linkdata.cmd.channel.ExchangeDataRequestContent;
import com.pax.linkdata.cmd.channel.ExchangeDataResponseContent;
import com.pax.util.LogUtil;


import java.io.File;
import java.util.List;
import java.util.concurrent.CountDownLatch;

public class MiscFragment extends Fragment {
    private Context mContext;

    private static final String[] mListInfo = new String[]{
            "getAllAppInfo",
            "installFile",
            "registerDataListener",
            "unregisterDataListener",
            "exchangeData",
            "reboot",
            "shutdown",
    };

    public MiscFragment() {

    }
    public MiscFragment(Context context) {
        this.mContext = context;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View fragmentView = inflater.inflate(R.layout.fragment_right, null);
        GridView gridView = fragmentView.findViewById(R.id.gv_function);
        gridView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.gridview_layoutres_btn, mListInfo));
        gridView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            switch (position) {
                case 0:
                    WorkExecutor.execute(MiscFragment.this::getAllAppInfo);
                    break;
                case 1:
                    WorkExecutor.execute(MiscFragment.this::installFile);
                    break;
                case 2:
                    registerDataListener();
                    break;
                case 3:
                    unregisterDataListener();
                    break;
                case 4:
                    exchangeData();
                    break;
                case 5:
                    WorkExecutor.execute(MiscFragment.this::rebootTest);
                    break;
                case 6:
                    WorkExecutor.execute(MiscFragment.this::shutdownTest);
                    break;
                default:
                    break;
            }
        });
        return fragmentView;
    }

    public void installFile() {
        if (checkNoDevice()) {
            return;
        }
        if (checkNoFile()) {
            return;
        }
        if (checkNoFileTypeFile()) {
            return;
        }

        LinkDevice linkDevice = DemoApplication.getSelectedDeviceList().get(0);
        if (!TextUtils.isEmpty(DemoApplication.getSelectedComponentID())) {
            linkDevice.setCurrentComponentID(DemoApplication.getSelectedComponentID());
        } else {
            linkDevice.setCurrentComponentID("");
        }

        try {
            if (linkDevice.isDeviceSelf(mContext)) {
                addLog("start install");
                MiscHelper.getInstance(mContext).installFile(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID(), DemoApplication.getSelectedFileList().get(0), DemoApplication.getFileType());
            } else {
                //先传输文件
                String localfile = DemoApplication.getSelectedFileList().get(0);
                String remotefile;
                if (DemoApplication.getSelectedDeviceList().get(0).getFirmwareVersion().startsWith("Android")) {
                    remotefile = Environment.getExternalStorageDirectory().toString() + File.separatorChar + "LinkUpSDKDemo" + File.separatorChar + (new File(localfile)).getName();
                } else {
                    remotefile = (new File(localfile)).getName();
                }
                LogUtil.d("remotefile:" + remotefile + " filetype:" + DemoApplication.getFileType());
                CountDownLatch mCountDownLatch = new CountDownLatch(1);
                long startTime = System.currentTimeMillis();
                addLog("start transfer file");
                FileHelper.getInstance(mContext).transferFile(linkDevice.getDeviceID(), localfile, remotefile, true, (totalLen, offset, status) -> {
                    if (status < 0) {
                        addLog("Transfer file failed");
                        mCountDownLatch.countDown();
                    } else if (status > 0) {
                        //todo
                    } else if (status == 0) {
                        mCountDownLatch.countDown();
                        addLog("Transfer file complete");
                    }
                });
                mCountDownLatch.await();
                addLog("start install");
                MiscHelper.getInstance(mContext).installFile(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID(), remotefile, DemoApplication.getFileType());
            }

            //根据各设备系统特点，是否重启。 T3300/T3180/T3350/T3320等设备安装后需要重启生效
            if (!TextUtils.isEmpty(linkDevice.getCurrentComponentID()) || linkDevice.getDeviceID().startsWith("271")) {
                MiscHelper.getInstance(mContext).reboot(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID());
            }
            addLog("install success");
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("install failed", e);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
            addLog("install failed");
            Thread.currentThread().interrupt();
        }
    }

    public void getAllAppInfo() {
        if (checkNoDevice()) {
            return;
        }

        LinkDevice linkDevice = DemoApplication.getSelectedDeviceList().get(0);
        if (!TextUtils.isEmpty(DemoApplication.getSelectedComponentID())) {
            linkDevice.setCurrentComponentID(DemoApplication.getSelectedComponentID());
        } else {
            linkDevice.setCurrentComponentID("");
        }
        try {
            List<AppInfo> appInfos = MiscHelper.getInstance(mContext).getAllAppInfo(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID());
            for (AppInfo appInfo : appInfos) {
                addLog("appID:" + appInfo.appID + " appName:" + appInfo.appName + " appVer:" + appInfo.appVer);
            }
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("getAllAppInfo failed", e);
        }
    }

    private static synchronized String getPackageName(Context context) {
        try {
            PackageManager packageManager = context.getPackageManager();
            PackageInfo packageInfo = packageManager.getPackageInfo(
                    context.getPackageName(), 0);
            return packageInfo.packageName;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    private void rebootTest() {
        if (checkNoDevice()) {
            return;
        }
        try {
            LinkDevice linkDevice = DemoApplication.getSelectedDeviceList().get(0);
            if (!TextUtils.isEmpty(DemoApplication.getSelectedComponentID())) {
                linkDevice.setCurrentComponentID(DemoApplication.getSelectedComponentID());
            } else {
                linkDevice.setCurrentComponentID("");
            }
            MiscHelper.getInstance(mContext).reboot(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID());
            addLog("reboot success");
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("reboot failed", e);
        }
    }

    private void shutdownTest() {
        if (checkNoDevice()) {
            return;
        }
        try {
            LinkDevice linkDevice = DemoApplication.getSelectedDeviceList().get(0);
            if (!TextUtils.isEmpty(DemoApplication.getSelectedComponentID())) {
                linkDevice.setCurrentComponentID(DemoApplication.getSelectedComponentID());
            } else {
                linkDevice.setCurrentComponentID("");
            }
            MiscHelper.getInstance(mContext).shutdown(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID());
            addLog("shutdown success");
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("shutdown failed", e);
        }
    }

    private IExchangeDataListener getDataListener() {
        return (ExchangeDataRequestContent requestContent) -> {
            addLog("received content:" + requestContent.getStringArg1());
            ExchangeDataResponseContent responseContent = new ExchangeDataResponseContent();
            responseContent.setStringArg1("success");
            return responseContent;
        };
    }

    private IExchangeDataListener dataListener = getDataListener();

    private void registerDataListener() {
        try {
            MiscHelper.getInstance(mContext).registerDataListener(getPackageName(mContext), dataListener);
            addLog("registerListener(" + getPackageName(mContext) + ") success");
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("registerListener failed", e);
        }
    }

    private void unregisterDataListener() {
        try {
            MiscHelper.getInstance(mContext).unregisterDataListener(getPackageName(mContext), dataListener);
            addLog("unregisterListener(" + getPackageName(mContext) + ") success");
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("unregisterListener failed", e);
        }
    }

    private void exchangeData() {
        if (checkNoDevice()) {
            return;
        }
        DemoApplication.getSelectedDeviceList().get(0).setCurrentComponentID("");

        //显示内容对话框
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(mContext);
        inputDialog.setTitle(R.string.input_exchange_content);
        inputDialog.setTitle(R.string.input_print_content);
        View view = View.inflate(mContext, R.layout.layout_input_diaglog, null);
        inputDialog.setView(view);
        EditText editText = view.findViewById(R.id.txt_content);
        editText.setText(DemoApplication.getTargetFilePath());
        inputDialog.setPositiveButton(R.string.sure, (dialog, which) -> {
            if (editText.getText().length() == 0) {
                Toast.makeText(mContext, "please input effective content", Toast.LENGTH_LONG).show();
                return;
            }

            WorkExecutor.execute(() -> {
                try {
                    ExchangeDataRequestContent requestContent = new ExchangeDataRequestContent();
                    requestContent.setStringArg1(editText.getText().toString());
                    requestContent.setTargetOwner(getPackageName(mContext));
                    ExchangeDataResponseContent responseContent = MiscHelper.getInstance(mContext).exchangeData(DemoApplication.getSelectedDeviceList().get(0).getDeviceID(), requestContent);
                    addLog("exchangeData success, response:[" + responseContent.getStringArg1() + "]");
                } catch (LinkException e) {
                    e.printStackTrace();
                    addErrLog("exchangeData failed", e);
                }
            });

            dialog.dismiss();
        }).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss()).show();

        editText.setHeight(200);
    }
}

