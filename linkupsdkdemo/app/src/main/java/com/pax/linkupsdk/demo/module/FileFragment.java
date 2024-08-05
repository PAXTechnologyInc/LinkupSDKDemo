package com.pax.linkupsdk.demo.module;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.GridView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.pax.egarden.devicekit.FileHelper;
import com.pax.linkdata.IFileProcess;
import com.pax.linkdata.LinkDevice;
import com.pax.linkdata.cmd.LinkException;
import com.pax.linkupsdk.demo.DemoApplication;
import com.pax.linkupsdk.demo.R;
import com.pax.linkupsdk.demo.WorkExecutor;
import com.pax.util.LogUtil;

import java.util.concurrent.CountDownLatch;

import static com.pax.linkupsdk.demo.Tools.checkNoDevice;
import static com.pax.linkupsdk.demo.Tools.checkNoFile;
import static com.pax.linkupsdk.demo.Tools.checkNoTargetFile;
import static com.pax.linkupsdk.demo.ViewLog.addErrLog;
import static com.pax.linkupsdk.demo.ViewLog.addLog;

public class FileFragment extends Fragment {
    private Context mContext;
    private FileHelper mFileHelper;
    private TextView cancelBt;
    private CountDownLatch mCountDownLatch;
    private long startTime;

    private static final String[] mListInfo = new String[]{
            "transferFile",
            "removeFile",
            "cancelTransferFile"
    };

    public FileFragment(Context context) {
        this.mContext = context;
    }

    private Handler mHandler = new Handler(Looper.getMainLooper());


    @SuppressLint("ResourceAsColor")
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mFileHelper = FileHelper.getInstance(mContext);
        View fragmentView = inflater.inflate(R.layout.fragment_right, null);
        GridView gridView = fragmentView.findViewById(R.id.gv_function);
        gridView.setAdapter(new ArrayAdapter<String>(getActivity(), R.layout.gridview_layoutres_btn, mListInfo));
        gridView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            switch (position) {
                case 0:
                    WorkExecutor.execute(FileFragment.this::transferFileTest);
                    break;
                case 1:
                    WorkExecutor.execute(FileFragment.this::removeFileTest);
                    break;
                case 2:
//                    WorkExecutor.execute(FileFragment.this::cancelTransferFile);
                    break;
                default:
                    break;
            }
        });

        mHandler.postDelayed(() -> {
            cancelBt = (TextView) gridView.getChildAt(2);
            cancelBt.setOnClickListener(null);
            cancelBt.setBackgroundColor(Color.GRAY);
//            addLog("current target file path:" + DemoApplication.getTargetFilePath());
        }, 500);
        return fragmentView;
    }

    private void removeFileTest() {
        if (checkNoDevice()) {
            return;
        }

        if (checkNoTargetFile()) {
            return;
        }

        LinkDevice linkDevice = DemoApplication.getSelectedDeviceList().get(0);
        try {
            mFileHelper.removeFile(linkDevice.getDeviceID(), DemoApplication.getTargetFilePath());
            addLog("remove " + linkDevice.getDeviceID() + "'s file[" + DemoApplication.getTargetFilePath() + "] OK");
        } catch (LinkException e) {
            e.printStackTrace();
            addErrLog("remove " + linkDevice.getDeviceID() + "'s file[" + DemoApplication.getTargetFilePath() + "] failed, errMsg:", e);
        }
    }

    private IFileProcess processListener = new IFileProcess() {
        @Override
        public void onProcess(int totalLen, int offset, int status) {
            long costTime = System.currentTimeMillis() - startTime;
            LogUtil.d("totalLen:" + totalLen + ",offset:" + offset + ",status:" + status);
            if (status < 0) {
                addLog("Transfer failed,  already transferred:" + offset + ", left:" + (totalLen - offset) + ", speed:" + ((offset * 1000L) / (costTime * 1024)) + "kb/s");
                if (mCountDownLatch != null) {
                    mCountDownLatch.countDown();
                }
            } else if (status > 0) {
                addLog("Transferring...,  already transferred:" + offset + ", left:" + (totalLen - offset) + ", speed:" + ((offset * 1000L) / (costTime * 1024)) + "kb/s");
            } else if (status == 0) {
                if (mCountDownLatch != null) {
                    mCountDownLatch.countDown();
                }
                addLog("Transfer complete, totalLen:" + totalLen + ", cost time:" + costTime + "ms, speed:" + ((totalLen * 1000L) / (costTime * 1024)) + "kb/s");
            }
        }
    };

    @SuppressLint("ResourceAsColor")
    private void transferFileTest() {
        if (checkNoDevice()) {
            return;
        }

        if (checkNoFile()) {
            return;
        }

        if (checkNoTargetFile()) {
            return;
        }

        final LinkDevice deviceInfo = DemoApplication.getSelectedDeviceList().get(0);
        try {
            addLog("start transfer file from local to remote");
            mCountDownLatch = new CountDownLatch(1);
//            try {
//                //先删除目标文件  注：此接口可删除目录
//                mFileHelper.removeFile(deviceInfo.getDeviceID(), DemoApplication.getTargetFilePath());
//            } catch (LinkException e) {
//                e.printStackTrace();
//            }
            startTime = System.currentTimeMillis();

            mHandler.post(() -> {
                cancelBt.setOnClickListener(v -> {
                    WorkExecutor.execute(FileFragment.this::cancelTransferFile);
                });
                cancelBt.setBackgroundColor(Color.BLUE);
            });

            mFileHelper.transferFile(deviceInfo.getDeviceID(), DemoApplication.getSelectedFileList().get(0), DemoApplication.getTargetFilePath(), true, processListener);
            mCountDownLatch.await();

//            addLog("transferFile file...Remote-->Local");
//            mCountDownLatch = new CountDownLatch(1);
//            try {
//                mFileHelper.removeFile(DeviceInfoImpl.getDeviceID(), LOCAL_PUB_FILE);
//            } catch (LinkException e) {
//                e.printStackTrace();
//            }
//            startTime = System.currentTimeMillis();
//            mFileHelper.transferFile(deviceInfo.getDeviceID(), LOCAL_PUB_FILE, REMOTE_PUB_FILE, false, processListener);
//            mCountDownLatch.await();
//            costTime = System.currentTimeMillis() - startTime;
//            fileSize = getFileSize(LOCAL_PUB_FILE);
//            addLog("(Remote-->Local)Done, file len:" + fileSize + "B,cost time:" + costTime + "ms,speed:" + fileSize * 1000 / (costTime * 1024) + "kb/s");
//            addLog("CRC32:" + FileUtils.getCRC32(new File(DemoApplication.getSelectedFileList().get(0))) + " ? " + FileUtils.getCRC32(new File(LOCAL_PUB_FILE)));
//            LogUtil.d("CRC32:" + FileUtils.getCRC32(new File(DemoApplication.getSelectedFileList().get(0))));
//            LogUtil.d("CRC32:" + FileUtils.getCRC32(new File(LOCAL_PUB_FILE)));
        } catch (LinkException e) {
            e.printStackTrace();
            addLog("transferFile failed, error code:" + e.getErrCode() + ", error msg:" + e.getErrMsg());
        } catch (InterruptedException e) {
            e.printStackTrace();
            Thread.currentThread().interrupt();
        } finally {
            mHandler.post(() -> {
                cancelBt.setBackgroundColor(Color.GRAY);
                cancelBt.setOnClickListener(null);
            });
        }
    }

    private void cancelTransferFile() {
        if (checkNoDevice()) {
            return;
        }
        if (checkNoFile()) {
            return;
        }
        final LinkDevice deviceInfo = DemoApplication.getSelectedDeviceList().get(0);
        try {
            mFileHelper.cancelTransferFile(deviceInfo.getDeviceID(), DemoApplication.getSelectedFileList().get(0), DemoApplication.getTargetFilePath(), true, processListener);
            addLog("cancelTransferFile success");
        } catch (LinkException e) {
            e.printStackTrace();
            addLog("cancelTransferFile failed, error code:" + e.getErrCode() + ", error msg:" + e.getErrMsg());
        }
    }
}


