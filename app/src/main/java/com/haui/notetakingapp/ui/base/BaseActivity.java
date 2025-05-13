package com.haui.notetakingapp.ui.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

public class BaseActivity extends AppCompatActivity {
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        applyTheme();
    }

    private void applyTheme() {
        SharedPreferences prefs = getSharedPreferences("settings", MODE_PRIVATE);
        String theme = prefs.getString("theme", "Sáng");
        
        switch (theme) {
            case "Tối":
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                break;
            case "Sáng":
            default:
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                break;
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        SharedPreferences prefs = newBase.getSharedPreferences("settings", MODE_PRIVATE);
        String textSize = prefs.getString("textSize", "Vừa");
        float scale = getTextScale(textSize);

        Configuration config = newBase.getResources().getConfiguration();
        config.fontScale = scale;

        Context scaledContext = newBase.createConfigurationContext(config);
        super.attachBaseContext(scaledContext);
    }

    private float getTextScale(String textSize) {
        switch (textSize) {
            case "Nhỏ":
                return 0.85f;
            case "Lớn":
                return 1.15f;
            case "Vừa":
            default:
                return 1.0f;
        }
    }
}
