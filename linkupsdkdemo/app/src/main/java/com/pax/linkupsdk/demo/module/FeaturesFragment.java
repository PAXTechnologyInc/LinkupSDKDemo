package com.pax.linkupsdk.demo.module;
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
import com.pax.linkupsdk.demo.module.devcon.PosFragment;

public class FeaturesFragment extends Fragment {

    private Button buttonPOS, buttonAD, buttonBonus;

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_features, container, false);

        buttonPOS = view.findViewById(R.id.buttonPOS);
        buttonAD = view.findViewById(R.id.buttonAD);
        buttonBonus = view.findViewById(R.id.buttonBonus);

        buttonPOS.setOnClickListener(v -> showFragment(new PosFragment(getContext())));
        buttonAD.setOnClickListener(v -> showFragment(new AdFragment(getContext())));
        buttonBonus.setOnClickListener(v -> showFragment(new BonusFragment(getContext())));

        // 默认显示第一个Fragment
        if (savedInstanceState == null) {
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
