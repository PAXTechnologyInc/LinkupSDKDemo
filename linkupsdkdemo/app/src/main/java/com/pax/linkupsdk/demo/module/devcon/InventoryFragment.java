package com.pax.linkupsdk.demo.module.devcon;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import com.pax.linkupsdk.demo.R;


public class InventoryFragment extends Fragment {
    CartListener mListener;


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_inventory, container, false);

        View pen = view.findViewById(R.id.ll_pen);
        pen.setOnClickListener(v -> {
            System.out.println("Pen clicked");
            mListener.onItemAdded(Consts.PEN);
        });
        View paper = view.findViewById(R.id.ll_paper);
        paper.setOnClickListener(v -> {
            System.out.println("paper clicked");
            mListener.onItemAdded(Consts.PAPER);
        });
        View notebook = view.findViewById(R.id.ll_notebook);
        notebook.setOnClickListener(v -> {
            System.out.println("notebook clicked");
            mListener.onItemAdded(Consts.NOTEBOOK);
        });
        View chocolate = view.findViewById(R.id.ll_chocolate);
        chocolate.setOnClickListener(v -> {
            System.out.println("chocolate clicked");
            mListener.onItemAdded(Consts.CHOCOLATE);
        });


        View rootView = inflater.inflate(R.layout.activity_home, container, false);
        ListView listView = rootView.findViewById(R.id.lv_cart);
        String[] items = {"Item 1", "Item 2", "Item 3", "Item 4", "Item 5"};

        // 创建ArrayAdapter
        ArrayAdapter<String> adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, items);

        // 设置适配器到ListView
        listView.setAdapter(adapter);

        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try {
            mListener = (CartListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.toString()
                    + " must implement OnItemAddedListener");
        }
    }
}
