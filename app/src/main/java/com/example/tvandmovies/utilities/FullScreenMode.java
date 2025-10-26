package com.example.tvandmovies.utilities;

import android.app.Activity;
import android.view.WindowManager;

public class FullScreenMode {
    public static void setupWindowFlags(Activity activity) {
        activity.getWindow().setFlags(
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,
                WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS
        );
    }
}
