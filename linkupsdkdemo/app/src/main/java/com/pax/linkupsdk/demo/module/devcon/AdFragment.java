package com.pax.linkupsdk.demo.module.devcon;

import static com.pax.linkupsdk.demo.Tools.checkNoDevice;
import static com.pax.linkupsdk.demo.Tools.checkNoFile;
import static com.pax.linkupsdk.demo.ViewLog.addErrLog;
import static com.pax.linkupsdk.demo.ViewLog.addLog;

import android.content.Context;
import android.os.Bundle;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pax.egarden.devicekit.DeviceHelper;
import com.pax.egarden.devicekit.FileHelper;
import com.pax.linkdata.LinkDevice;
import com.pax.linkdata.cmd.LinkException;
import com.pax.linkupsdk.demo.DemoApplication;
import com.pax.linkupsdk.demo.R;
import com.pax.linkupsdk.demo.WorkExecutor;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class AdFragment extends Fragment {
    private final Context mContext;

    // list of the names of available functionalities
    private static final String[] mListInfo = new String[]{
            "sendFile"
    };

    public AdFragment(final Context context) {
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Show the section enclosing "Select file" and "Select target file" buttons on the left pane
        requireActivity().findViewById(R.id.layout_select_file).setVisibility(View.VISIBLE);
        // Hide the "Select target file button but let only the "Select file" button show on the left pane
        requireActivity().findViewById(R.id.btn_select_target_file).setVisibility(View.GONE);
        // Show the "Selected devices" on the top of right pane
        requireActivity().findViewById(R.id.layout_select_device).setVisibility(View.VISIBLE);
        // Show the "Selected files" on the top of right pane
        requireActivity().findViewById(R.id.select_file_layout).setVisibility(View.VISIBLE);
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
                    WorkExecutor.execute(AdFragment.this::sendMedia);
                    break;
                case 1:
                    break;
                case 2:
                    break;
                default:
                    break;
            }
        });

        return fragmentView;
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
}
