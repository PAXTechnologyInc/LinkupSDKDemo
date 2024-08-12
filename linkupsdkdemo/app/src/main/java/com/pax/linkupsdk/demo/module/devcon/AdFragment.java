package com.pax.linkupsdk.demo.module.devcon;

import static com.pax.linkupsdk.demo.Tools.checkNoDevice;
import static com.pax.linkupsdk.demo.Tools.checkNoFile;
import static com.pax.linkupsdk.demo.ViewLog.addErrLog;
import static com.pax.linkupsdk.demo.ViewLog.addLog;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Looper;
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
import androidx.core.content.FileProvider;
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
import com.pax.util.FileUtils;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class AdFragment extends Fragment {
    private final Context mContext;
    private String mLastReceivedFile;
    private Handler mHandler;

    // list of the names of available functionalities
    private static final String[] mListInfo = new String[]{
            "registerDataListener",
            "unregisterDataListener",
            "exchangeData",
            "sendFile",
            "openFile"
    };

    public AdFragment(final Context context) {
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mHandler = new Handler(Looper.getMainLooper());
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
                case 4:
                    openFile();
                    break;
                default:
                    break;
            }
        });

        return fragmentView;
    }

    /**
     * Create a data listener for exchanging data between linkup devices
     * @return an instance of IExchangeDataListener
     */
    private IExchangeDataListener getDataListener() {
        return (ExchangeDataRequestContent requestContent) -> {
            addLog("received content: " + requestContent.getStringArg1());
            if (!TextUtils.isEmpty(requestContent.getStringArg2())) {
                addLog("file received: " + requestContent.getStringArg2());
                mLastReceivedFile = requestContent.getStringArg2();
            }
            if (!TextUtils.isEmpty(requestContent.getCmd1()) && "play".equals(requestContent.getCmd1())) {
                addLog("play " + mLastReceivedFile);
                mHandler.post(AdFragment.this::openFile);
            }
            ExchangeDataResponseContent responseContent = new ExchangeDataResponseContent();
            responseContent.setStringArg1("success");
            return responseContent;
        };
    }

    private final IExchangeDataListener dataListener = getDataListener();

    /**
     * Register a listener to observe data transmitted from another linkup device. This is a required
     * step in the receiving device before doing exchangeData task in the originating device.
     */
    private void registerDataListener() {
        try {
            MiscHelper.getInstance(mContext).registerDataListener(getPackageName(mContext), dataListener);
            addLog("registerListener(" + getPackageName(mContext) + ") success");
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("registerListener failed", e);
        }
    }

    /**
     * Un-register the registered data listener
     */
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
        // check if a target device is selected to which the data is sent
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

                    /* Send a message to the destination device to inform of the file transfer is completed and the
                     * action to see the result */
                    try {
                        ExchangeDataRequestContent requestContent = new ExchangeDataRequestContent();
                        requestContent.setStringArg1("Press \"openFile\" button to open: ");
                        requestContent.setStringArg2(remoteFile);
                        requestContent.setCmd1("play");
                        requestContent.setTargetOwner(getPackageName(mContext));
                        ExchangeDataResponseContent responseContent = MiscHelper.getInstance(mContext).exchangeData(DemoApplication.getSelectedDeviceList().get(0).getDeviceID(), requestContent);
                        addLog("exchangeData succeeded, response:[" + responseContent.getStringArg1() + "]");
                        addLog("request: " + requestContent.getCmd1() + " " + requestContent.getStringArg2());
                        addLog("response: " + responseContent.getStringArg1());
                    } catch (LinkException e) {
                        e.printStackTrace();
                        // Show a message to the message area on the bottom half of the right pane
                        addErrLog("exchangeData failed", e);
                    }
                }
            });
            mCountDownLatch.await();
        } catch (LinkException e) {
            e.printStackTrace();
            // Show a message to the message area on the bottom half of the right pane
            addErrLog("File transfer failed", e);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
            // Show a message to the message area on the bottom half of the right pane
            addLog("File transfer failed");
            Thread.currentThread().interrupt();
        }
    }

    /**
     * Open and play the recently received image (JPG) or video file (MP4)
     */
    private void openFile() {
        if (TextUtils.isEmpty(mLastReceivedFile)) {
            addLog("No file received");
            return;
        }

        try {
            Uri mediaUri = FileProvider.getUriForFile(mContext,
                                                      mContext.getPackageName() + ".provider",
                                                      new File(mLastReceivedFile));
            String ext = FileUtils.getFileExtension(mLastReceivedFile);

            // open and play the image or video file
            if (ext.equalsIgnoreCase("jpg") || ext.equalsIgnoreCase("mp4")) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                if (ext.equalsIgnoreCase("jpg"))
                    intent.setDataAndType(mediaUri, "image/jpg");
                else
                    intent.setDataAndType(mediaUri, "video/mp4");
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                intent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);
                mContext.startActivity(intent);
            }
            else {
                addLog("Can open only jpg and mp4 files");
            }
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            addErrLog("Failed to open media file", new LinkException());
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
