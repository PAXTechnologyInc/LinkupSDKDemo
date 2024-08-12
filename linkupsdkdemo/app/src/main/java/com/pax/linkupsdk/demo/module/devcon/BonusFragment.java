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

import com.pax.egarden.devicekit.MiscHelper;
import com.pax.linkdata.LinkDevice;
import com.pax.linkdata.cmd.LinkException;
import com.pax.linkupsdk.demo.DemoApplication;
import com.pax.linkupsdk.demo.R;
import com.pax.linkupsdk.demo.WorkExecutor;

public class BonusFragment extends Fragment {
    private final Context mContext;

    // list of the names of available functionalities
    private static final String[] mListInfo = new String[]{
            "rebootDevice",
            "getDeviceInfo"
    };

    public BonusFragment(Context context) {
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
                    break;
                default:
                    break;
            }
        });

        return fragmentView;
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
        final LinkDevice linkDevice = DemoApplication.getSelectedDeviceList().get(0);
        // Set the component ID to the selected device (only scanner and printer have component ID)
        if (!TextUtils.isEmpty(DemoApplication.getSelectedComponentID())) {
            linkDevice.setCurrentComponentID(DemoApplication.getSelectedComponentID());
        } else {
            linkDevice.setCurrentComponentID("");
        }
        // Show a message to the message area on the bottom half of the right pane
        addLog(String.format("Retrieve Device Info: %1$s", toString(linkDevice)));
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
