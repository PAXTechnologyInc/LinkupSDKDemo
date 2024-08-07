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
import com.pax.util.LogUtil;

import java.io.File;
import java.util.concurrent.CountDownLatch;

public class AdFragment extends Fragment {
    private final Context mContext;
    private DeviceHelper mDeviceHelper;
    private static final String[] mListInfo = new String[]{
            "sendFile"
    };

    public AdFragment(final Context context) {
        this.mContext = context;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // LinearLayout with select source file and target file buttons on the left pane
        requireActivity().findViewById(R.id.layout_select_file).setVisibility(View.VISIBLE);
        // only show the button for selecting source file on the left pane
        requireActivity().findViewById(R.id.btn_select_target_file).setVisibility(View.GONE);
        // show selected device on the top of right pane
        requireActivity().findViewById(R.id.layout_select_device).setVisibility(View.VISIBLE);
        // show selected file on the top of right pane
        requireActivity().findViewById(R.id.select_file_layout).setVisibility(View.VISIBLE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        mDeviceHelper = DeviceHelper.getInstance(requireContext());
        View fragmentView = inflater.inflate(R.layout.fragment_right, container, false);
        GridView gridView = fragmentView.findViewById(R.id.gv_function);
        gridView.setAdapter(new ArrayAdapter<>(requireActivity(), R.layout.gridview_layoutres_btn, mListInfo));
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
        if (checkNoDevice()) {
            return;
        }
        if (checkNoFile()) {
            return;
        }

        try {
            final LinkDevice selectedDevice = DemoApplication.getSelectedDeviceList().get(0);
            final LinkDevice selfDevice = mDeviceHelper.getSelfDeviceInfo();
            String localfile = DemoApplication.getSelectedFileList().get(0);
            String remoteFile;

            if (DemoApplication.getSelectedDeviceList().get(0).getFirmwareVersion().startsWith("Android")) {
                remoteFile = Environment.getExternalStorageDirectory().toString() + File.separatorChar + "LinkUpSDKDemo" + File.separatorChar + (new File(localfile)).getName();
            } else {
                remoteFile = (new File(localfile)).getName();
            }
            addLog(String.format("send selected file from %1$s to %2$s/%3$s", selfDevice.getDeviceName(),
                                 selectedDevice.getDeviceName(), remoteFile));
            LogUtil.d("remoteFile:" + remoteFile + " filetype:" + DemoApplication.getFileType());

            CountDownLatch mCountDownLatch = new CountDownLatch(1);
            FileHelper.getInstance(mContext).transferFile(selectedDevice.getDeviceID(), localfile, remoteFile, true, (totalLen, offset, status) -> {
                if (status < 0) {
                    addLog("Transfer file failed");
                    mCountDownLatch.countDown();
                } else if (status > 0) {
                    addLog("Transferring...,  already transferred:" + offset + ", left:" + (totalLen - offset));
                } else {
                    mCountDownLatch.countDown();
                    addLog("Transfer completed. File size:" + totalLen);
                }
            });
            mCountDownLatch.await();
//            addLog("Open media");
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("install failed", e);
        } catch (InterruptedException e1) {
            e1.printStackTrace();
            addLog("install failed");
            Thread.currentThread().interrupt();
        }
    }
}
