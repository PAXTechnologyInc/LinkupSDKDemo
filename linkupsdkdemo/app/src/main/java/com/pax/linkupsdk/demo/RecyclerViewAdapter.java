package com.pax.linkupsdk.demo;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.pax.util.LogUtil;
import com.pax.linkdata.LinkDevice;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {
    private Context context;
    private List mDataList;
    private View inflater;
    private OnItemClickListener mListener;
    private int itemNumber;

    //构造方法，传入数据
    public RecyclerViewAdapter(Context context, OnItemClickListener listener) {
        this.context = context;
        mListener = listener;
        itemNumber = 0;
    }

    //构造方法，传入数据
    public RecyclerViewAdapter(Context context, OnItemClickListener listener, int itemNumber) {
        this.context = context;
        mListener = listener;
        this.itemNumber = itemNumber;
    }

    public void setRcvClickDataList(List<String> list) {
        LogUtil.d("", "setRcvClickDataList: " + list.size());
        notifyDataSetChanged();
    }

    public void setStringList(List<String> stringList) {
//        LogUtil.d("", "setStringList: " + stringList.size());
        mDataList = stringList;
        notifyDataSetChanged();
    }

    public void setDeviceList(List<LinkDevice> deviceList) {
//        LogUtil.d("", "setDeviceList: " + deviceList.size());
        mDataList = deviceList;
        notifyDataSetChanged();
    }

    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        //创建ViewHolder，返回每一项的布局
//        LogUtil.d("", "onCreateViewHolder: " + itemNumber);
        inflater = LayoutInflater.from(context).inflate(R.layout.item_list_device, parent, false);
        MyViewHolder myViewHolder = new MyViewHolder(inflater);
        if (itemNumber > 0) {
            int parentWidth = parent.getWidth();
            ViewGroup.LayoutParams layoutParams = myViewHolder.itemView.getLayoutParams();
            layoutParams.width = (parentWidth / itemNumber);
        }
        return myViewHolder;
    }

    @Override
    public void onBindViewHolder(MyViewHolder holder, int position) {
        final Object object = mDataList.get(position);
//        LogUtil.d("test", "onBindViewHolder:" + position + ",holder:" + holder + ",object:" + object);
        //将数据和控件绑定
        if (object instanceof String) {
            holder.textView.setText((String) object);
        } else if (object instanceof LinkDevice) {
            holder.textView.setText(((LinkDevice) object).getDeviceName() + " : " + ((LinkDevice) object).getDeviceID());
        }

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (mListener != null) {
                    if (object instanceof String) {
                        mListener.onItemClick((String) object);
                    } else if (object instanceof LinkDevice) {
                        mListener.onItemClick((LinkDevice) object);
                    }
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        //返回Item总条数
        return mDataList.size();
    }

    //内部类，绑定控件
    class MyViewHolder extends RecyclerView.ViewHolder {
        TextView textView;

        public MyViewHolder(View itemView) {
            super(itemView);
            textView = itemView.findViewById(R.id.tv_list_item);
        }
    }

    public interface OnItemClickListener {
        void onItemClick(String content);

        void onItemClick(LinkDevice linkDevice);
    }
}