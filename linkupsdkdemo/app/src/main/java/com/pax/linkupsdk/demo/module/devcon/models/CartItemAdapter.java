package com.pax.linkupsdk.demo.module.devcon.models;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;

import com.pax.linkupsdk.demo.R;
import com.pax.linkupsdk.demo.module.devcon.CartListener;

import java.util.List;

public class CartItemAdapter extends ArrayAdapter<String> {
    private final Context context;
    private List<String> items;

    public CartItemAdapter(Context context, List<String> items) {
        super(context, 0, items);
        this.context = context;
        this.items = items;
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.cart_item, parent, false);
        }

        TextView textView = convertView.findViewById(R.id.text_view_item);
        Button button = convertView.findViewById(R.id.button_delete);

        String item = getItem(position);
        textView.setText(item);

        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                CartListener mListener = (CartListener) context;
                mListener.onItemDeleted(position);
            }
        });

        return convertView;
    }
}
