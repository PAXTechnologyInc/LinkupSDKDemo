package com.pax.linkupsdk.demo.module.devcon;

import static com.pax.linkupsdk.demo.Tools.checkNoDevice;
import static com.pax.linkupsdk.demo.Tools.checkNoFile;
import static com.pax.linkupsdk.demo.ViewLog.addErrLog;
import static com.pax.linkupsdk.demo.ViewLog.addLog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Environment;
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

import com.pax.egarden.devicekit.DeviceHelper;
import com.pax.egarden.devicekit.FileHelper;
import com.pax.egarden.devicekit.MiscHelper;
import com.pax.linkdata.IExchangeDataListener;
import com.pax.linkdata.LinkDevice;
import com.pax.linkdata.cmd.LinkException;
import com.pax.linkdata.cmd.channel.ExchangeDataRequestContent;
import com.pax.linkdata.cmd.channel.ExchangeDataResponseContent;
import com.pax.linkupsdk.demo.DemoApplication;
import com.pax.linkupsdk.demo.R;
import com.pax.linkupsdk.demo.WorkExecutor;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class AdFragment extends Fragment {
    private final Context mContext;

    // list of the names of available functionalities
    private static final String[] mListInfo = new String[]{
            "registerDataListener",
            "unregisterDataListener",
            "exchangeData",
            "sendFile"
    };

    public AdFragment(final Context context) {
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        // Set up the grid view to list the functionalities
        View fragmentView = inflater.inflate(R.layout.fragment_right, container, false);
        GridView gridView = fragmentView.findViewById(R.id.gv_function);
        gridView.setAdapter(new ArrayAdapter<>(requireActivity(), R.layout.gridview_layoutres_btn, mListInfo));

        // Respond to the user's click on the buttons
        gridView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            switch (position) {
                case 0:
                    registerDataListener();
                    break;
                case 1:
                    unregisterDataListener();
                    break;
                case 2:
                    exchangeData();
                    break;
                case 3:
                    WorkExecutor.execute(AdFragment.this::sendMedia);
                    break;
                default:
                    break;
            }
        });

        return fragmentView;
    }

    private IExchangeDataListener getDataListener() {
        return (ExchangeDataRequestContent requestContent) -> {
            addLog("received content:" + requestContent.getStringArg1());
            ExchangeDataResponseContent responseContent = new ExchangeDataResponseContent();
            responseContent.setStringArg1("success");
            return responseContent;
        };
    }

    private final IExchangeDataListener dataListener = getDataListener();

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

    /**
     * Exchange data between 2 linkup devices.
     * Require to do registerDataListener() on the destination device before exchanging data.
     */
    private void exchangeData() {
        if (checkNoDevice()) {
            return;
        }

        DemoApplication.getSelectedDeviceList().get(0).setCurrentComponentID("");

        //显示内容对话框
        AlertDialog.Builder inputDialog = new AlertDialog.Builder(mContext);
        inputDialog.setTitle(R.string.input_exchange_content);
        View view = View.inflate(mContext, R.layout.layout_input_diaglog, null);
        inputDialog.setView(view);
        EditText editText = view.findViewById(R.id.txt_content);
        editText.setText(DemoApplication.getTargetFilePath());
        inputDialog.setPositiveButton(R.string.sure, (dialog, which) -> {
            if (editText.getText().length() == 0) {
                Toast.makeText(mContext, "Please input effective content", Toast.LENGTH_LONG).show();
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

    private void sendMedia() {
        // check if a target device is selected to which the file is sent
        if (checkNoDevice()) {
            return;
        }
        // check if a file is selected to send
        if (checkNoFile()) {
            return;
        }

        try {
            // Get the selected target device
            final LinkDevice selectedDevice = DemoApplication.getSelectedDeviceList().get(0);
            // Get the linked device that the file is located
            final LinkDevice selfDevice = DeviceHelper.getInstance(mContext).getSelfDeviceInfo();

            // Get the path of the selected file
            String localfile = DemoApplication.getSelectedFileList().get(0);

            // define the remote file path
            String remoteFile;
            if (DemoApplication.getSelectedDeviceList().get(0).getFirmwareVersion().startsWith("Android")) {
                remoteFile = Environment.getExternalStorageDirectory().toString() + File.separatorChar + "LinkUpSDKDemo" + File.separatorChar + (new File(localfile)).getName();
            } else {
                remoteFile = (new File(localfile)).getName();
            }

            // Show a message to the message area on the bottom half of the right pane
            addLog(String.format("Send selected file from %1$s to %2$s/%3$s", selfDevice.getDeviceName(),
                                 selectedDevice.getDeviceName(), remoteFile));

            CountDownLatch mCountDownLatch = new CountDownLatch(1);
            // Call an SDK API to send file from the starting device to the selected target device
            FileHelper.getInstance(mContext).transferFile(selectedDevice.getDeviceID(), localfile, remoteFile, true, (totalLen, offset, status) -> {
                // The callback actions after the file transfer fails, is still in progress, or is completed
                if (status < 0) {
                    // Show the failure message to the message area at the bottom half of the right pane
                    addLog("Transfer file failed");
                    mCountDownLatch.countDown();
                } else if (status > 0) {
                    // Show the progress to the message area at the bottom half of the right pane
                    addLog("Transferring...,  already transferred:" + offset + ", left:" + (totalLen - offset));
                } else {
                    mCountDownLatch.countDown();
                    // Show the success message to the message area at the bottom half of the right pane
                    addLog("Transfer completed. File size:" + totalLen);
                }
            });
            mCountDownLatch.await();
        } catch (LinkException e) {
            e.printStackTrace();
            // Show a message to the message area on the bottom half of the right pane
            addErrLog("install failed", e);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
            // Show a message to the message area on the bottom half of the right pane
            addLog("install failed");
            Thread.currentThread().interrupt();
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
}
