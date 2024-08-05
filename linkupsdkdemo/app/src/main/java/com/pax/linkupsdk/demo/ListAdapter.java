package com.pax.linkupsdk.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.TextView;

import com.pax.util.LogUtil;
import com.pax.linkdata.LinkDevice;

import java.util.ArrayList;
import java.util.List;

public class ListAdapter extends BaseAdapter {

    private List<LinkDevice> mDeviceList = new ArrayList<>();
    private LayoutInflater mInflater;

    /**
     * 直接用外面的deviceList，有时因外面的deviceList变化而未及时通知导致程序崩溃
     * @param deviceList
     */
    private void copyList(List<LinkDevice> deviceList) {
        for (LinkDevice linkDevice:deviceList) { //增加上线设备
            if (!mDeviceList.contains(linkDevice)) {
                mDeviceList.add(linkDevice);
            }
        }
        int count = mDeviceList.size()-deviceList.size(); //删除下线的设备
        for (int i = 0; i< count; i++) {
            for (LinkDevice linkDevice:mDeviceList) {
                if (!deviceList.contains(linkDevice)) {
                    mDeviceList.remove(linkDevice);
                    break;
                }
            }
        }
    }
    public ListAdapter (List<LinkDevice> list, Context context) {
        copyList(list);
        this.mInflater = LayoutInflater.from(context);
    }

    public void setDataList(List<LinkDevice> list) {
        copyList(list);
        notifyDataSetChanged();
        LogUtil.d("ListAdapter", "setDataList:"+list.size());
    }
    @Override
    public int getCount() {
        return mDeviceList == null ? 0 : mDeviceList.size();
    }

    @Override
    public Object getItem(int position) {
        if (position >= mDeviceList.size()) {
            return null;
        }
        return mDeviceList.get(position);
    }

    @Override
    public long getItemId(int position) {
        return position;
    }

    @Override
    public View getView(int position, View convertView, ViewGroup parent) {
        View view = mInflater.inflate(R.layout.item_list_device, null);
        LinkDevice deviceInfo = (LinkDevice) getItem(position);

        TextView tvName = (TextView) view.findViewById(R.id.tv_list_item);
        if (deviceInfo != null) {
            tvName.setText(deviceInfo.getDeviceName()+" : "+deviceInfo.getDeviceID());
        }
        return view;
    }
}
