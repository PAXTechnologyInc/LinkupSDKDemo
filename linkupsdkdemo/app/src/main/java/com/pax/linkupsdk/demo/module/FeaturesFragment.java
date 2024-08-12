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
import com.pax.linkupsdk.demo.module.devcon.InventoryFragment;
import com.pax.linkupsdk.demo.module.devcon.PosFragment;

public class FeaturesFragment extends Fragment {

    private Button buttonInventory, buttonPOS, buttonAD, buttonBonus;

    private final Context mContext;

    public FeaturesFragment(Context context) {
        this.mContext = context;
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_features, container, false);

        buttonInventory = view.findViewById(R.id.buttonInventory);
        buttonPOS = view.findViewById(R.id.buttonPOS);
        buttonAD = view.findViewById(R.id.buttonAD);
        buttonBonus = view.findViewById(R.id.buttonBonus);

        Button[] btns = {buttonInventory, buttonPOS, buttonAD, buttonBonus};

        // Different colors for the function tabs
        ColorStateList currentButtonColorState = ColorStateList.valueOf(getResources().getColor(R.color.defaultColor));
        ColorStateList otherButtonColorState = ColorStateList.valueOf(getResources().getColor(R.color.inactive_color));

        buttonInventory.setOnClickListener(v -> {
            resetAllBtnColor(btns, otherButtonColorState);
            v.setBackgroundTintList(currentButtonColorState);

            showFragment(new InventoryFragment());
        });
        buttonPOS.setOnClickListener(v -> {
            // Hide the "Select file" and "Select Target file" buttons on the left pane
            requireActivity().findViewById(R.id.layout_select_file).setVisibility(View.GONE);
            // Show the section of "Selected devices" on the top of the right pane
            requireActivity().findViewById(R.id.layout_select_device).setVisibility(View.GONE);
            // Hide the section of "Selected files" on the top of the right pane
            requireActivity().findViewById(R.id.select_file_layout).setVisibility(View.GONE);
            // Set the default color for the
            resetAllBtnColor(btns, otherButtonColorState);
            v.setBackgroundTintList(currentButtonColorState);

            showFragment(new PosFragment(getContext()));
        });
        buttonAD.setOnClickListener(v -> {
            // Hide the "Select file" and "Select Target file" buttons on the left pane
            requireActivity().findViewById(R.id.layout_select_file).setVisibility(View.VISIBLE);
            // Hide the "Select target file button but let only the "Select file" button show on the left pane
            requireActivity().findViewById(R.id.btn_select_target_file).setVisibility(View.GONE);
            // Show the section of "Selected devices" on the top of the right pane
            requireActivity().findViewById(R.id.layout_select_device).setVisibility(View.VISIBLE);
            // Hide the section of "Selected files" on the top of the right pane
            requireActivity().findViewById(R.id.select_file_layout).setVisibility(View.VISIBLE);

            // set button color
            resetAllBtnColor(btns, otherButtonColorState);
            v.setBackgroundTintList(currentButtonColorState);

            showFragment(new AdFragment(mContext));
        });
        buttonBonus.setOnClickListener(v -> {
            // Hide the "Select file" and "Select Target file" buttons on the left pane
            requireActivity().findViewById(R.id.layout_select_file).setVisibility(View.GONE);
            // Show the section of "Selected devices" on the top of the right pane
            requireActivity().findViewById(R.id.layout_select_device).setVisibility(View.VISIBLE);
            // Hide the section of "Selected files" on the top of the right pane
            requireActivity().findViewById(R.id.select_file_layout).setVisibility(View.GONE);
            resetAllBtnColor(btns, otherButtonColorState);
            v.setBackgroundTintList(currentButtonColorState);

            showFragment(new BonusFragment(mContext));
        });

        // Set default to PosFragment
        if (savedInstanceState == null) {
            resetAllBtnColor(btns, otherButtonColorState);
            buttonPOS.setBackgroundTintList(currentButtonColorState);
            showFragment(new PosFragment(getContext()));
        }

        return view;
    }

    private void resetAllBtnColor(Button[] btns, ColorStateList buttonColorState) {
        for (Button btn : btns) {
            btn.setBackgroundTintList(buttonColorState);
        }
    }


    private void showFragment(Fragment fragment) {
        FragmentManager fragmentManager = getChildFragmentManager();
        FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
        fragmentTransaction.replace(R.id.fragmentContainer, fragment);
        fragmentTransaction.commit();
    }
}
