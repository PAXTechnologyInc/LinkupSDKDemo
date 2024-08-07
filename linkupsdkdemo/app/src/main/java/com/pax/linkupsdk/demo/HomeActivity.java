package com.pax.linkupsdk.demo;

import android.app.AlertDialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.Message;
import android.text.Selection;
import android.text.Spannable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.pax.commonlib.file.FileUtils;
import com.pax.linkdata.deviceinfo.component.ComponentBase;
import com.pax.linkupsdk.demo.module.FeaturesFragment;
import com.pax.linkupsdk.demo.module.MiscFragment;
import com.pax.util.LogUtil;
import com.pax.linkupsdk.demo.module.ScannerFragment;
import com.pax.egarden.devicekit.DeviceHelper;
import com.pax.linkdata.LinkDevice;
import com.pax.linkdata.cmd.LinkException;
import com.pax.linkdata.deviceinfo.component.Printer;
import com.pax.linkdata.deviceinfo.component.Scanner;
import com.pax.linkupsdk.demo.module.DeviceFragment;
import com.pax.linkupsdk.demo.module.FileFragment;
import com.pax.linkupsdk.demo.module.PrinterFragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

public class HomeActivity extends FragmentActivity implements RecyclerViewAdapter.OnItemClickListener, View.OnClickListener {
    private DeviceHelper mDeviceHelper;
    private ListView mDeviceList;
    private Handler mHandler;
    private ArrayList<LinkDevice> mList;

    private LinkDevice selectDevice = null;
    private ListAdapter mOnlineDeviceListAdapter;
    private RecyclerViewAdapter mDeviceRVadapter;
    private RecyclerViewAdapter mFileRVadapter;
    private int[] printerIDArray;
    private int[] scannerIDArray;
    private int[] fileTypeArray;
    private int[] componentIDArray;
    private int printerSecletID;
    private int scannerSelectID;
    private int componentSelectID;
    private int fileTypeSelected;
    private TextView targetfile;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        initDeviceListener();
        LogUtil.d("HomeActivity onCreate");
        mList = (ArrayList<LinkDevice>) DemoApplication.getOnlineDeviceList();
        final String title = getIntent().getStringExtra("title");
        TextView titleView = findViewById(R.id.tv_title);
        titleView.setText(title);
        ImageButton imageButton = findViewById(R.id.ibtn_log_delete);
        imageButton.setOnClickListener(v -> {
            TextView logView = findViewById(R.id.tv_log);
            logView.setText("");
        });

        int radioSize = DemoApplication.isPortDisplay(getApplicationContext()) ? 12 : 16;

        findViewById(R.id.btn_select_file).setOnClickListener(this);
        findViewById(R.id.btn_select_target_file).setOnClickListener(this);
        targetfile = findViewById(R.id.target_file);
        mDeviceList = findViewById(R.id.lv_devices);
        mOnlineDeviceListAdapter = new ListAdapter(mList, this);
        mDeviceList.setAdapter(mOnlineDeviceListAdapter);
        mDeviceList.setOnItemClickListener((parent, view, position, id) -> {
            LogUtil.d("click position:" + position);
            //不显示内存数据，每次都先清空，由用户选择
            DemoApplication.getSelectedDeviceList().clear();
//                DemoApplication.getSelectedFileList().clear();
            DemoApplication.setSelectedScannerID("");
            DemoApplication.setSelectedPrinterID("");
            DemoApplication.setSelectedComponentID("");
            DemoApplication.setFileType("");

            LinkDevice deviceInfo1 = (LinkDevice) mDeviceList.getItemAtPosition(position);
            if (deviceInfo1 == null) {
                return;
            }
            try {
                List<LinkDevice> linkDevices = mDeviceHelper.queryOnlineDeviceList();
                for (LinkDevice linkDevice : linkDevices) {
                    if (linkDevice.getDeviceID().equals(deviceInfo1.getDeviceID())) {
                        deviceInfo1 = linkDevice;
                    }
                }
            } catch (LinkException e) {
                return;
            }
            if (deviceInfo1 == null) {
                return;
            }
            if (!DemoApplication.getSelectedDeviceList().contains(deviceInfo1)) {
                //20240201 多设备一般用在多媒体，linkup demo仅需展示对一个设备的处理即可,所以先清空历史列表
                DemoApplication.getSelectedDeviceList().clear();
                DemoApplication.getSelectedDeviceList().add(deviceInfo1);
            }

            setTargetDefault();

            selectDevice = deviceInfo1;
            mDeviceRVadapter.setDeviceList(DemoApplication.getSelectedDeviceList());
            if (title.equals(Constant.PRINTER_HELPER)) { //被选择的设备发生变化后，打印机ID的选择无法处理
                LinearLayout layout = findViewById(R.id.line_component_list);
                layout.setVisibility(View.VISIBLE);
                findViewById(R.id.btn_uncheck).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.tv_component)).setText(R.string.Printer_list);
                RadioGroup group = findViewById(R.id.rg_component_devices);
                group.clearCheck();
                group.removeAllViews();

                List<Printer> printers = new ArrayList<>();
                printers.addAll(deviceInfo1.getPrinterList());
                group.setOnCheckedChangeListener((group12, checkedId) -> {
                    if (checkedId == -1) {
                        printerSecletID = -1;
                        DemoApplication.setSelectedPrinterID("");
                        return;
                    }
                    if (printerSecletID == checkedId) {
                        return;
                    }
                    printerSecletID = checkedId;
                    DemoApplication.setSelectedPrinterID(printers.get(checkedId - 1).getComponentID());
                });

                printerIDArray = new int[printers.size()];
                int count = 0;
                for (Printer printer : printers) {
                    RadioButton radioButton = new RadioButton(view.getContext());
                    RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, 50);
                    layoutParams.setMargins(0, 0, 3, 0);
                    radioButton.setLayoutParams(layoutParams);
                    radioButton.setText(printer.getComponentID());
                    radioButton.setTextSize(radioSize);
                    //radioButton.setButtonDrawable(R.color.defaultColor);
                    radioButton.setPadding(0, 0, 3, 0);
                    radioButton.setId(count + 1);

                    //一个对象时，默认选中
                    if (printers.size() == 1) {
                        radioButton.setChecked(true);
                        DemoApplication.setSelectedPrinterID(printers.get(0).getComponentID());
                    }

                    printerIDArray[count++] = radioButton.getId();
                    group.addView(radioButton);
                    LogUtil.d("printerID:" + printerIDArray[count - 1]);
                }
            } else if (title.equals(Constant.SCANNER_HELPER)) {
                LinearLayout layout = findViewById(R.id.line_component_list);
                layout.setVisibility(View.VISIBLE);
                findViewById(R.id.btn_uncheck).setVisibility(View.GONE);
                ((TextView) findViewById(R.id.tv_component)).setText(R.string.Scanner_list);
                RadioGroup group = findViewById(R.id.rg_component_devices);
                group.clearCheck();
                group.removeAllViews();

                List<Scanner> scanners = new ArrayList<>();
                group.setOnCheckedChangeListener((group1, checkedId) -> {
                    if (checkedId == -1) {
                        scannerSelectID = -1;
                        DemoApplication.setSelectedScannerID("");
                        return;
                    }
                    if (scannerSelectID == checkedId) {
                        return;
                    }
                    scannerSelectID = checkedId;
                    DemoApplication.setSelectedScannerID(scanners.get(checkedId - 1).getComponentID());
                });

                scanners.addAll(deviceInfo1.getScannerList());
//                LogUtil.d("size:" + scanners.size());
                scannerIDArray = new int[scanners.size()];
                int count = 0;
                for (Scanner scanner : scanners) {
                    RadioButton radioButton = new RadioButton(view.getContext());
                    RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, 50);
                    layoutParams.setMargins(0, 0, 3, 0);
                    radioButton.setLayoutParams(layoutParams);
                    radioButton.setText(scanner.getComponentID());
                    radioButton.setTextSize(radioSize);
                    radioButton.setPadding(0, 0, 3, 0);
                    radioButton.setId(count + 1);
                    scannerIDArray[count++] = radioButton.getId();

                    //一个对象时，默认选中
                    if (scanners.size() == 1) {
                        radioButton.setChecked(true);
                        DemoApplication.setSelectedScannerID(scanners.get(0).getComponentID());
                    }

                    group.addView(radioButton);
                }
            } else if (title.equals(Constant.MISC_HELPER)) {
                //组件选择， 可用于getAppInfo、installFile、reboot、shutdown接口
                LinearLayout layout = findViewById(R.id.line_component_list);
                layout.setVisibility(View.VISIBLE);
                ((TextView) findViewById(R.id.tv_component)).setText(R.string.Component_list);
                RadioGroup group = findViewById(R.id.rg_component_devices);
                group.clearCheck();
                group.removeAllViews();

                List<ComponentBase> componentBaseList = new ArrayList<>();
                List<Printer> printerList = deviceInfo1.getPrinterList();
                for (Printer printer : printerList) {
                    if (printer.getModel().equals("T3180")) {
                        componentBaseList.add(printer);
                    }
                }
                List<Scanner> scannerList = deviceInfo1.getScannerList();
                for (Scanner scanner : scannerList) {
                    LogUtil.d("model:" + scanner.getModel());
                    if (scanner.getModel().equals("T3320") || scanner.getModel().equals("T3350")) {
                        componentBaseList.add(scanner);
                    }
                }
                if (componentBaseList.isEmpty()) {
                    layout.setVisibility(View.GONE);
                }

                group.setOnCheckedChangeListener((group12, checkedId) -> {
                    if (checkedId == -1) {
                        componentSelectID = -1;
                        DemoApplication.setSelectedComponentID("");
                        return;
                    }
                    if (componentSelectID == checkedId) {
                        return;
                    }
                    componentSelectID = checkedId;
                    LogUtil.d("onCheckedChanged,checkedId:" + checkedId + ", ComponenID:" + componentBaseList.get(checkedId - 1).getComponentID());
                    DemoApplication.setSelectedComponentID(componentBaseList.get(checkedId - 1).getComponentID());
                });
                findViewById(R.id.btn_uncheck).setOnClickListener(v -> group.clearCheck());
                componentIDArray = new int[componentBaseList.size()];
                int count = 0;
                for (ComponentBase componentBase : componentBaseList) {
                    RadioButton radioButton = new RadioButton(view.getContext());
                    RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, 50);
                    layoutParams.setMargins(0, 0, 1, 0);
                    radioButton.setLayoutParams(layoutParams);
                    radioButton.setText(componentBase.getComponentID());
                    radioButton.setTextSize(radioSize);
                    radioButton.setPadding(0, 0, 0, 0);
                    radioButton.setId(count + 1);

                    componentIDArray[count++] = radioButton.getId();
                    group.addView(radioButton);
                }

                //     *                    - "FILE_TYPE_APP":        supported for Android, Prolin, Runthos platforms
                //     *                    - "FILE_TYPE_OS":         supported for Prolin, Runthos, Printer platforms
                //     *                    - "FILE_TYPE_AUP":        supported for Prolin platform
                //     *                    - "FILE_TYPE_APP_PARAM"   supported for Prolin platform
                //     *                    - "FILE_TYPE_SYS_LIB"     supported for Prolin platform
                //     *                    - "FILE_TYPE_WIFI"        supported for Printer platform
                String[] fileTypes = new String[]{"APP", "OS", "AUP", "APP_PARAM", "SYS_LIB", "OS_WIFI"};
                String[] fileTypes2 = new String[]{"FILE_TYPE_APP", "FILE_TYPE_OS", "FILE_TYPE_AUP", "FILE_TYPE_APP_PARAM", "FILE_TYPE_SYS_LIB", "FILE_TYPE_WIFI"};
                LinearLayout linearLayout = findViewById(R.id.line_file_type);
                linearLayout.setVisibility(View.VISIBLE);
                RadioGroup radioGroup = findViewById(R.id.rg_file_type);
                radioGroup.clearCheck();
                radioGroup.removeAllViews();
                radioGroup.setOnCheckedChangeListener((group13, checkedId) -> {
                    if (checkedId == -1) {
                        fileTypeSelected = -1;
                        DemoApplication.setFileType("");
                        return;
                    }
                    if (fileTypeSelected == checkedId) {
                        return;
                    }
                    fileTypeSelected = checkedId;
                    DemoApplication.setFileType(fileTypes2[checkedId - 1]);
                    LogUtil.d("checkedId:" + checkedId + ",fileType.size:" + fileTypes.length + ",current printer id:" + fileTypes2[checkedId - 1]);
                });

                fileTypeArray = new int[fileTypes.length];
                count = 0;
                for (String str : fileTypes) {
                    RadioButton radioButton = new RadioButton(view.getContext());
                    RadioGroup.LayoutParams layoutParams = new RadioGroup.LayoutParams(RadioGroup.LayoutParams.WRAP_CONTENT, 50);
                    layoutParams.setMargins(0, 0, 1, 0);
                    radioButton.setLayoutParams(layoutParams);
                    radioButton.setText(str);
                    radioButton.setTextSize(radioSize);
                    radioButton.setPadding(0, 0, 0, 0);
                    radioButton.setId(count + 1);
                    fileTypeArray[count++] = radioButton.getId();
                    radioGroup.addView(radioButton);
                }
            }
        });

        RecyclerView deviceRecyclerView = findViewById(R.id.recycler_view_device);
        mDeviceRVadapter = new RecyclerViewAdapter(this, this, getResources().getInteger(R.integer.device_span_count));
        mDeviceRVadapter.setDeviceList(new ArrayList<>());
        LinearLayoutManager deviceManager = new LinearLayoutManager(this);
        deviceManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        deviceRecyclerView.setLayoutManager(deviceManager);
        deviceRecyclerView.setAdapter(mDeviceRVadapter);

        RecyclerView fileRecyclerView = findViewById(R.id.recycler_view_file);
        mFileRVadapter = new RecyclerViewAdapter(this, this, getResources().getInteger(R.integer.file_span_count));
        mFileRVadapter.setStringList(new ArrayList<>());
        LinearLayoutManager fileManager = new LinearLayoutManager(this);
        fileManager.setOrientation(LinearLayoutManager.HORIZONTAL);
        fileRecyclerView.setLayoutManager(fileManager);
        fileRecyclerView.setAdapter(mFileRVadapter);

        switch (title) {
            case Constant.DEVICE_HELPER:
                findViewById(R.id.layout_select_device).setVisibility(View.GONE);
                replaceFragment(new DeviceFragment(HomeActivity.this));
                break;
            case Constant.PRINTER_HELPER:
                findViewById(R.id.select_file_layout).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_select_file).setVisibility(View.VISIBLE);
                findViewById(R.id.btn_select_target_file).setVisibility(View.GONE);
                replaceFragment(new PrinterFragment(HomeActivity.this));
                break;
            case Constant.FILE_HELPER:
                findViewById(R.id.select_file_layout).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_select_file).setVisibility(View.VISIBLE);
                findViewById(R.id.select_target_file_layout).setVisibility(View.VISIBLE);
                replaceFragment(new FileFragment(HomeActivity.this));
                break;
            case Constant.SCANNER_HELPER:
                replaceFragment(new ScannerFragment(HomeActivity.this));
                break;
            case Constant.MISC_HELPER:
                findViewById(R.id.select_file_layout).setVisibility(View.VISIBLE);
                findViewById(R.id.layout_select_file).setVisibility(View.VISIBLE);
                findViewById(R.id.btn_select_target_file).setVisibility(View.GONE);
                replaceFragment(new MiscFragment(HomeActivity.this));
                break;
            case Constant.DEV_CON:
                findViewById(R.id.layout_select_device).setVisibility(View.GONE);
                replaceFragment(new FeaturesFragment());
                break;
            default:
                break;
        }

        ViewLog viewLog = new ViewLog(this);
        mHandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                if (msg.what == 1) {
                    ViewLog.addLogMain(msg.obj);
                } else if (msg.what == 2) {
                    LogUtil.d("recv handle what 2");
                    mOnlineDeviceListAdapter.setDataList(DemoApplication.getOnlineDeviceList());
                }
            }
        };
        viewLog.setHandler(mHandler);

        //不显示内存数据，每次都先清空，由用户选择
        DemoApplication.getSelectedDeviceList().clear();
        DemoApplication.getSelectedFileList().clear();
        DemoApplication.setTargetFilePath("");
        DemoApplication.setSelectedScannerID("");
        DemoApplication.setSelectedPrinterID("");
        DemoApplication.setSelectedComponentID("");
        DemoApplication.setFileType("");
//        Runnable runnable = new Runnable() {
//            @Override
//            public void run() {
//                mDeviceRVadapter.setDeviceList(DemoApplication.getSelectedDeviceList());
//                mFileRVadapter.setStringList(DemoApplication.getSelectedFileList());
//            }
//        };
//        mHandler.postDelayed(runnable, 300); //延时载入才能正常显示已选择的设备和文件
    }

    @Override
    public void onItemClick(String content) {
        if (DemoApplication.getSelectedFileList().contains(content)) {
            DemoApplication.getSelectedFileList().remove(content);
        }
        mFileRVadapter.setStringList(DemoApplication.getSelectedFileList());
    }

    @Override
    public void onItemClick(LinkDevice linkDevice) {
        if (DemoApplication.getSelectedDeviceList().contains(linkDevice)) {
            DemoApplication.getSelectedDeviceList().remove(linkDevice);
        }
        mDeviceRVadapter.setDeviceList(DemoApplication.getSelectedDeviceList());
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.btn_select_file:
                Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
                intent.putExtra(Intent.EXTRA_ALLOW_MULTIPLE, true);  //多选文件,好像不管用
                intent.setType("*/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                startActivityForResult(intent, 2000);
                break;
            case R.id.btn_select_target_file:
//                EditText editText = new EditText(HomeActivity.this);
                LayoutInflater inflater = getLayoutInflater();
                final View layout = inflater.inflate(R.layout.dialog_edittext, (ViewGroup) findViewById(R.id.dialog_edit_view));
                EditText editText = layout.findViewById(R.id.item_ed);
                editText.setText(DemoApplication.getTargetFilePath());
                CharSequence cs = editText.getText();
                Selection.setSelection((Spannable) cs, cs.length());

                AlertDialog.Builder inputDialog = new AlertDialog.Builder(HomeActivity.this);
                inputDialog.setTitle(R.string.input_target_file_title);
                inputDialog.setView(layout);
                inputDialog.setPositiveButton(R.string.sure, (dialog, which) -> {
                    if (editText.getText().length() == 0) {
                        Toast.makeText(getApplicationContext(), "please input effective path", Toast.LENGTH_LONG).show();
                        return;
                    }
                    DemoApplication.setTargetFilePath(editText.getText().toString());
                    targetfile.setText(editText.getText().toString());
                    dialog.dismiss();
                }).setNegativeButton(R.string.cancel, (dialog, which) -> dialog.dismiss()).show();
                break;
            default:
                break;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (2000 == requestCode && resultCode == RESULT_OK) {
            Uri uri = data.getData();
            if (uri == null) {
                LogUtil.e("uri is null");
                return;
            }
            String filePath = FileUtils.getUriPath(this, uri);
            LogUtil.d("uri.getPath():" + uri.getPath() + ",getScheme:" + uri.getScheme() + ",getAuthority:" + uri.getAuthority() + ",toString:" + uri.toString() + "filePath:" + filePath);
            DemoApplication.getSelectedFileList().clear();
            DemoApplication.getSelectedFileList().add(filePath);

            mFileRVadapter.setStringList(DemoApplication.getSelectedFileList());

            setTargetDefault();
        }
    }

    /**
     * 设置默认的目标文件路径，用于选择源文件后或者选择设备后
     */
    private void setTargetDefault() {
        //源文件设置好后， 同时将Demo的目标文件文件名字同步修改
        if (getIntent().getStringExtra("title").equals(Constant.FILE_HELPER) && !DemoApplication.getSelectedFileList().isEmpty()) {
            String filePath = DemoApplication.getSelectedFileList().get(0);
            boolean targetDeviceIsAndroid = true;
            if (!DemoApplication.getSelectedDeviceList().isEmpty()) {
                if (!DemoApplication.getSelectedDeviceList().get(0).getFirmwareVersion().startsWith("Android")) {
                    targetDeviceIsAndroid = false;
                }
            }
            if (targetDeviceIsAndroid) {
                DemoApplication.setTargetFilePath(Environment.getExternalStorageDirectory().toString() + File.separatorChar + "LinkUpSDKDemo" + File.separatorChar + new File(filePath).getName());
            } else {
                DemoApplication.setTargetFilePath(new File(filePath).getName());
            }

            targetfile.setText(DemoApplication.getTargetFilePath());
        }
    }

    private void replaceFragment(Fragment fragment) {
        Bundle bundle = new Bundle();
        fragment.setArguments(bundle);
        FragmentManager fragmentManager = getSupportFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.right_layout, fragment);
        fragmentTransaction.addToBackStack(null);
        fragmentTransaction.commit();
    }

    @Override
    public void onBackPressed() {
        super.onBackPressed();
        LinearLayout layout = findViewById(R.id.line_component_list);
        layout.setVisibility(View.GONE);
        finish();
    }

    private void initDeviceListener() {
        mDeviceHelper = DeviceHelper.getInstance(getApplicationContext());
//        LogUtil.d("demo", "mDeviceClient init OK." + mDeviceHelper);
        try {
            mDeviceHelper.registerStatusListener(mDeviceListener);
        } catch (LinkException e) {
            e.printStackTrace();
        }
    }

    private DeviceHelper.IStatusListener mDeviceListener = new DeviceHelper.IStatusListener() {
        @Override
        public void onConnectDevices(List<LinkDevice> list) {
            LogUtil.d("demo", "onConnectDevices:" + list.get(0).getDeviceID());
            for (LinkDevice linkDevice : list) {
                if (DemoApplication.getLinkDeviceSelf() == null && linkDevice.isDeviceSelf(getApplicationContext())) {
                    DemoApplication.setLinkDeviceSelf(linkDevice);
                }
                if (!DemoApplication.getOnlineDeviceList().contains(linkDevice)) {
                    DemoApplication.getOnlineDeviceList().add(linkDevice);
                }
            }
            updateOnlineDeviceList();
        }

        @Override
        public void onDisconnectDevices(List<LinkDevice> list) {
            LogUtil.d("demo", "onDisconnectDevices:" + list.get(0).getDeviceID());
            DemoApplication.getOnlineDeviceList().removeAll(list);
            updateOnlineDeviceList();
        }

        @Override
        public void onOnlineDevices(List<LinkDevice> list) {
            LogUtil.d("demo", "onOnlineDevices:" + list.get(0).getDeviceID());
            for (LinkDevice linkDevice : list) {
                if (DemoApplication.getLinkDeviceSelf() == null && linkDevice.isDeviceSelf(getApplicationContext())) {
                    DemoApplication.setLinkDeviceSelf(linkDevice);
                }
                if (!DemoApplication.getOnlineDeviceList().contains(linkDevice)) {
                    DemoApplication.getOnlineDeviceList().add(linkDevice);
                }
            }
            updateOnlineDeviceList();
        }

        @Override
        public void onOfflineDevices(List<LinkDevice> list) {
            LogUtil.d("demo", "onOfflineDevices:" + list.get(0).getDeviceID());
            DemoApplication.getOnlineDeviceList().removeAll(list);
            updateOnlineDeviceList();
        }

        @Override
        public void onUpdateDevices(List<LinkDevice> list) {
            for (Iterator<LinkDevice> i = list.iterator(); i.hasNext(); ) {
                LinkDevice linkDevice = i.next();
                LogUtil.d("demo", "onUpdateDevices:" + linkDevice.getDeviceName() + " " + linkDevice.getDeviceID());
                if (DemoApplication.getOnlineDeviceList().contains(linkDevice)) {
                    DemoApplication.getOnlineDeviceList().remove(queryDeviceFromList(linkDevice.getDeviceID(), DemoApplication.getOnlineDeviceList()));
                    DemoApplication.getOnlineDeviceList().add(linkDevice);
                }
            }
            updateOnlineDeviceList();
        }
    };

    public LinkDevice queryDeviceFromList(String deviceID, List<LinkDevice> deviceList) {
        for (LinkDevice linkDevice : deviceList) {
            if (linkDevice.getDeviceID().equals(deviceID)) {
                return linkDevice;
            }
        }
        return null;
    }

    private void updateOnlineDeviceList() {
        Message message = Message.obtain();
        message.what = 2;
        mHandler.sendMessage(message);
    }

    @Override
    protected void onDestroy() {
        LogUtil.d("HomeActivity onDestroy");
        super.onDestroy();
        try {
            if (mDeviceHelper != null) {
                mDeviceHelper.unregisterStatusListener(mDeviceListener);
            }
        } catch (LinkException e) {
            e.printStackTrace();
        }
    }

    /**
     * 忽略系统字体大小修改
     * @return
     */
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
