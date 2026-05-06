package com.adam.maestro.pro;

import android.app.Activity;
import android.os.Bundle;
import android.content.Intent;
import android.provider.Settings;

public class MainActivity extends Activity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // طلب تفعيل خدمة السيطرة والسحب التلقائي
        if (!isAccessibilityEnabled()) {
            Intent intent = new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS);
            startActivity(intent);
        }
    }

    private boolean isAccessibilityEnabled() {
        return false; 
    }
}
