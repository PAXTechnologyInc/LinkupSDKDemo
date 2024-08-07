package com.pax.linkupsdk.demo.module.devcon;

import static com.pax.linkupsdk.demo.Tools.checkNoDevice;
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

import com.pax.linkdata.LinkDevice;
import com.pax.linkupsdk.demo.DemoApplication;
import com.pax.linkupsdk.demo.R;

public class BonusFragment extends Fragment {
    private static final String[] mListInfo = new String[]{
            "getIPAddress"
    };

    public BonusFragment(final Context context) {
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // LinearLayout with select source file and target file buttons on the left pane
        requireActivity().findViewById(R.id.layout_select_file).setVisibility(View.GONE);
        // show selected device on the top of right pane
        requireActivity().findViewById(R.id.layout_select_device).setVisibility(View.VISIBLE);
        // show selected file on the top of right pane
        requireActivity().findViewById(R.id.select_file_layout).setVisibility(View.GONE);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
//        mDeviceHelper = DeviceHelper.getInstance(mContext);
        View fragmentView = inflater.inflate(R.layout.fragment_right, container, false);
        GridView gridView = fragmentView.findViewById(R.id.gv_function);
        gridView.setAdapter(new ArrayAdapter<>(requireActivity(), R.layout.gridview_layoutres_btn, mListInfo));
        gridView.setOnItemClickListener((AdapterView<?> parent, View view, int position, long id) -> {
            switch (position) {
                case 0:
                    getIPAddress();
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

    private void getIPAddress() {
        if (checkNoDevice()) {
            return;
        }

        final LinkDevice linkDevice = DemoApplication.getSelectedDeviceList().get(0);
        if (!TextUtils.isEmpty(DemoApplication.getSelectedComponentID())) {
            linkDevice.setCurrentComponentID(DemoApplication.getSelectedComponentID());
        } else {
            linkDevice.setCurrentComponentID("");
        }

        addLog(String.format("Retrieve Device Info: %1$s", toString(linkDevice)));
    }

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
                '}';
    }
}
