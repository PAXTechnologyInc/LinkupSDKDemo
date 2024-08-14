package com.pax.linkupsdk.demo.module.devcon.utils;

import android.app.Activity;
import android.app.Dialog;
import android.view.Window;
import android.widget.TextView;

import com.pax.linkupsdk.demo.R;

public class IndicatorUtil {
    private static Dialog spinnerDialog;

    public static void showSpin(Activity activity, String message) {
        if (spinnerDialog == null || !spinnerDialog.isShowing()) {
            spinnerDialog = new Dialog(activity);
            spinnerDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            spinnerDialog.setCancelable(false);
            spinnerDialog.setContentView(R.layout.spinner_dialog);

            TextView textView = spinnerDialog.findViewById(R.id.spinner_text);
            textView.setText(message);  // 设置自定义文本

            spinnerDialog.show();
        }
    }

    public static void hideSpin() {
        if (spinnerDialog != null && spinnerDialog.isShowing()) {
            spinnerDialog.dismiss();
        }
    }


}
