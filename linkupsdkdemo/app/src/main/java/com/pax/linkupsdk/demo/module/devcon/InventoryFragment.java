package com.pax.linkupsdk.demo.module.devcon;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.pax.linkupsdk.demo.R;
import com.pax.linkupsdk.demo.module.devcon.models.Item;


public class InventoryFragment extends Fragment {
    CartListener mListener;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_inventory, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        addItemsDynamically(view);
    }

    @SuppressLint("SetTextI18n")
    private void addItemsDynamically(View view) {
        LinearLayout container = view.findViewById(R.id.item_container);

        // items in a Const file
        Item[] items = {Consts.PEN, Consts.PAPER, Consts.NOTEBOOK, Consts.CHOCOLATE};

        // add view
        for (int i = 0; i < items.length; i++) {
            LinearLayout itemLayout = new LinearLayout(getContext());
            itemLayout.setOrientation(LinearLayout.VERTICAL);
            LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT);
            layoutParams.setMargins(10, 10, 10, 10);
            itemLayout.setLayoutParams(layoutParams);

            ImageView imageView = new ImageView(getContext());
            LinearLayout.LayoutParams imageParams = new LinearLayout.LayoutParams(200, 200);
            imageView.setLayoutParams(imageParams);
            imageView.setScaleType(ImageView.ScaleType.CENTER_CROP);
            imageView.setImageResource(items[i].imgId);
            imageView.setContentDescription("DEMO");

            TextView textView = new TextView(getContext());
            textView.setLayoutParams(new LinearLayout.LayoutParams(LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT));
            textView.setPadding(8, 8, 8, 8);
            textView.setText(items[i].description + "    $" + items[i].price);

            itemLayout.addView(imageView);
            itemLayout.addView(textView);
            container.addView(itemLayout);

            int finalI = i;
            itemLayout.setOnClickListener(v -> {
                String itemConst = (String) v.getTag();
                System.out.println(itemConst + " clicked");
                mListener.onItemAdded(items[finalI]);
            });
        }
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
