package com.pax.linkupsdk.demo.module;

import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
//import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.pax.linkupsdk.demo.R;
import com.pax.linkupsdk.demo.module.devcon.AdFragment;
import com.pax.linkupsdk.demo.module.devcon.BonusFragment;
import com.pax.linkupsdk.demo.module.devcon.InventoryFragment;
import com.pax.linkupsdk.demo.module.devcon.PosFragment;

public class FeaturesFragment extends Fragment {

    private Button buttonInventory, buttonPOS, buttonAD, buttonBonus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_features, container, false);

        buttonInventory = view.findViewById(R.id.buttonInventory);
        buttonPOS = view.findViewById(R.id.buttonPOS);
        buttonAD = view.findViewById(R.id.buttonAD);
        buttonBonus = view.findViewById(R.id.buttonBonus);

        Button[] btns = {buttonInventory, buttonPOS, buttonAD, buttonBonus};

        buttonInventory.setOnClickListener(v -> {
            showFragment(new InventoryFragment());
            resetAllBtnColor(btns);
            buttonInventory.setBackgroundColor(Color.RED);

        });
        buttonPOS.setOnClickListener(v -> {
            showFragment(new PosFragment(getContext()));
            resetAllBtnColor(btns);
            buttonPOS.setBackgroundColor(Color.RED);
        });
        buttonAD.setOnClickListener(v -> {
            showFragment(new AdFragment());
            resetAllBtnColor(btns);
            buttonAD.setBackgroundColor(Color.RED);
        });
        buttonBonus.setOnClickListener(v -> {
            showFragment(new BonusFragment());
            resetAllBtnColor(btns);
            buttonBonus.setBackgroundColor(Color.RED);
        });

        // 默认显示第一个Fragment
        if (savedInstanceState == null) {
            showFragment(new PosFragment(getContext()));
            resetAllBtnColor(btns);
            buttonPOS.setBackgroundColor(Color.RED);
        }

        return view;
    }

    private void resetAllBtnColor(Button[] btns) {
        for (int i = 0; i < btns.length; i++) {
            btns[i].setBackgroundColor(Color.GRAY);
        }
    }


    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        fragmentTransaction.commit();
    }
}
