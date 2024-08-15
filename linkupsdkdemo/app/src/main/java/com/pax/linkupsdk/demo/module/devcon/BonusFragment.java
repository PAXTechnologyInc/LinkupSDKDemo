package com.pax.linkupsdk.demo.module.devcon;

import static com.pax.linkupsdk.demo.Tools.checkNoDevice;
import static com.pax.linkupsdk.demo.ViewLog.addErrLog;
import static com.pax.linkupsdk.demo.ViewLog.addLog;

import android.content.Context;
import android.os.Bundle;
import android.text.TextUtils;
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
import com.pax.egarden.devicekit.MiscHelper;
import com.pax.linkdata.LinkDevice;
import com.pax.linkdata.cmd.LinkException;
import com.pax.linkupsdk.demo.DemoApplication;
import com.pax.linkupsdk.demo.R;
import com.pax.linkupsdk.demo.WorkExecutor;
import com.pax.linkupsdk.demo.module.devcon.utils.IndicatorUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.List;

public class BonusFragment extends Fragment {
    private final Context mContext;
    private final DeviceHelper mDeviceHelper;

    // list of the names of available functionalities
    private static final String[] mListInfo = new String[]{
            "rebootDevice",
            "getDeviceInfo",
            "uploadMenu"
    };

    public BonusFragment(Context context) {
        this.mContext = context;
        mDeviceHelper = DeviceHelper.getInstance(mContext);
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
        // respond to the user's click on the buttons
        gridView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            switch (position) {
                case 0:
                    WorkExecutor.execute(BonusFragment.this::rebootDevice);
                    break;
                case 1:
                    getDeviceInfo();
                    break;
                case 2:
                    uploadMenu();
                    break;
                default:
                    break;
            }
        });

        return fragmentView;
    }

    /**
     *  Upload menu json to server.
     */
    private void uploadMenu() {
        try {
            // get the menu file
            if(DemoApplication.getSelectedFileList().isEmpty()){
                addLog("Please select the menu json file first.");
                return;
            }
            String localFile = DemoApplication.getSelectedFileList().get(0);
            if(!localFile.endsWith(".json")){
                addLog("Only support json file.");
                return;
            }

            File file = new File(localFile);
            InputStream is = null;
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();

            // get menu json string
            String json = new String(buffer, StandardCharsets.UTF_8);

            // send to server
            sendPostRequest("store_07TqXV2hGL7e6QQzX1DvM", "testAPIKey2", json);
        } catch (IOException ex) {
            ex.printStackTrace();
        }
    }

    /**
     * Upload menu to serve.
     * @param storeId unique store id
     * @param apiKey unique api key
     * @param json menu json string
     */
    public void sendPostRequest(final String storeId, final String apiKey, final String json) {


        new Thread(() -> {
            addLog("uploading menu");
            HttpURLConnection conn = null;
            BufferedReader reader = null;

            try {
                requireActivity().runOnUiThread(() -> IndicatorUtil.showSpin(requireActivity(), "Uploading Menu"));

                String url = "https://dev-api-dev.up.railway.app/v1/stores/" + storeId + "/menu_configuration/";
                URL urlObj = new URL(url);
                conn = (HttpURLConnection) urlObj.openConnection();
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Content-Type", "application/json");
                conn.setRequestProperty("x-api-key", apiKey);

                // get OutputStream，ready to send
                OutputStream os = conn.getOutputStream();
                os.write(json.getBytes());
                os.flush();
                os.close();

                // get response from server
                int responseCode = conn.getResponseCode();
                if (responseCode == HttpURLConnection.HTTP_OK) {
                    reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
                    StringBuilder response = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        response.append(line);
                    }
                    final String responseStr = response.toString();
                    System.out.println("resp: " + responseStr);
                    addLog("Uploading menu resp: " + responseStr);
                    addLog("Uploading menu done, please check on the portal.");
                } else {
                    addLog("Uploading menu failed.");
                }
            } catch (Exception e) {
                e.printStackTrace();
            } finally {
                if (conn != null) {
                    conn.disconnect();
                }
                if (reader != null) {
                    try {
                        reader.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
                requireActivity().runOnUiThread(IndicatorUtil::hideSpin);
            }
        }).start();
    }

    /*
     * Connect and reboot the selected device
     */
    private void rebootDevice() {
        // Show a message to remind user to select a device
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

            // Show a message to the message area on the bottom half of the right pane
            addLog(String.format("Reboot %1$s(ID:%2$s)", linkDevice.getDeviceName(), linkDevice.getDeviceID()));

            // Call the SDK API to do the reboot
            MiscHelper.getInstance(mContext).reboot(linkDevice.getDeviceID(), linkDevice.getCurrentComponentID());
            // Show the success message to the message area on the bottom half of the right pane
            addLog("Reboot succeeded");
        } catch (LinkException e) {
            e.printStackTrace();
            // Show the failure message
            addErrLog("Reboot failed", e);
        }
    }

    /*
     * Retrieve and display the information of the selected device
     */
    private void getDeviceInfo() {
        // Show a message to remind user to select a device
        if (checkNoDevice()) {
            return;
        }

        // Retrieve the selected device
        final LinkDevice oldSelected = DemoApplication.getSelectedDeviceList().get(0);
        LinkDevice newSelected = null;
        try {
            // Find all online linked devices and compare the ID and name with the selected device.
            // Same device will have the same ID and name. LinkIP address might be changed.
            List<LinkDevice> linkDeviceList = mDeviceHelper.queryOnlineDeviceList();
            for(LinkDevice d : linkDeviceList) {
                if (d.getDeviceID().equals(oldSelected.getDeviceID())
                        && d.getDeviceName().equalsIgnoreCase(oldSelected.getDeviceName())) {
                    newSelected = d;
                    break;
                }
            }
        } catch (LinkException e) {
            addLog(String.format("Failed to query online devices - %1$s:%2$s", e.getErrCode(), e.getErrMsg()));
            return;
        }

        if (newSelected != null) {
            // Set the component ID to the selected device (only scanner and printer have component ID)
            if (!TextUtils.isEmpty(DemoApplication.getSelectedComponentID()))
                newSelected.setCurrentComponentID(DemoApplication.getSelectedComponentID());
            else
                newSelected.setCurrentComponentID("");
        }
        else {
            newSelected = DemoApplication.getSelectedDeviceList().get(0);
        }
        // Show a message to the message area on the bottom half of the right pane
        addLog(String.format("Device Info: %1$s", toString(newSelected)));
    }

    /*
     * Construct a string to contain the information of the specified LinkDevice instance
     */
    private String toString(LinkDevice linkDevice) {
        return "{" +
                "deviceID='" + linkDevice.getDeviceID() + '\'' +
                ", deviceName='" + linkDevice.getDeviceName() + '\'' +
                ", deviceModel='" + linkDevice.getDeviceModel() + '\'' +
                ", connectionType='" + linkDevice.getConnectionType() + '\'' +
                ", groupOwnerID='" + linkDevice.getGroupOwnerID() + '\'' +
                ", isUserDefinedCenterNode='" + linkDevice.isUserDefinedCenterNode() + '\'' +
                ", linkIP='" + linkDevice.getLinkIP() + '\'' +
                ", firmwareVersion='" + linkDevice.getFirmwareVersion() + '\'' +
                ", currentComponentID='" + linkDevice.getCurrentComponentID() + '\'' +
                '}';
    }
}
