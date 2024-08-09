package com.pax.linkupsdk.demo.module;
import android.content.Context;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.pax.linkupsdk.demo.R;
import com.pax.linkupsdk.demo.module.devcon.AdFragment;
import com.pax.linkupsdk.demo.module.devcon.BonusFragment;
import com.pax.linkupsdk.demo.module.devcon.PosFragment;

public class FeaturesFragment extends Fragment {

    private Button buttonPOS, buttonAD, buttonBonus;

    private final Context mContext;

    public FeaturesFragment(Context context) {
        this.mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_features, container, false);

        buttonPOS = view.findViewById(R.id.buttonPOS);
        buttonAD = view.findViewById(R.id.buttonAD);
        buttonBonus = view.findViewById(R.id.buttonBonus);

        // Different colors for the function tabs
        ColorStateList currentButtonColorState = ColorStateList.valueOf(getResources().getColor(R.color.defaultColor));
        ColorStateList otherButtonColorState = ColorStateList.valueOf(getResources().getColor(R.color.inactive_color));

        // Set the listener for the button click
        buttonPOS.setOnClickListener(v -> {
            // Set the default color for the
            v.setBackgroundTintList(currentButtonColorState);
            buttonAD.setBackgroundTintList(otherButtonColorState);
            buttonBonus.setBackgroundTintList(otherButtonColorState);
            showFragment(new PosFragment(getContext()));
        });
        buttonAD.setOnClickListener(v -> {
            v.setBackgroundTintList(currentButtonColorState);
            buttonPOS.setBackgroundTintList(otherButtonColorState);
            buttonBonus.setBackgroundTintList(otherButtonColorState);
            showFragment(new AdFragment(mContext));
        });
        buttonBonus.setOnClickListener(v -> {
            v.setBackgroundTintList(currentButtonColorState);
            buttonPOS.setBackgroundTintList(otherButtonColorState);
            buttonAD.setBackgroundTintList(otherButtonColorState);
            showFragment(new BonusFragment(mContext));
        });

        // 默认显示第一个Fragment
        if (savedInstanceState == null) {
            buttonPOS.setBackgroundTintList(currentButtonColorState);
            showFragment(new PosFragment(getContext()));
        }

        return view;
    }

    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        fragmentTransaction.commit();
    }
}
