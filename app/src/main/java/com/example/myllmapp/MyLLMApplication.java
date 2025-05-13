package com.example.myllmapp;

import android.app.Application;
import androidx.appcompat.app.AppCompatDelegate;
import android.content.SharedPreferences;

/**
 * 应用程序类，用于全局初始化
 */
public class MyLLMApplication extends Application {

    private static final String PREF_NIGHT_MODE = "night_mode_pref";
    private static final String PREF_NIGHT_MODE_VALUE = "night_mode_value";

    @Override
    public void onCreate() {
        super.onCreate();
        
        // 初始化夜间模式
        restoreNightModePreference();
    }
    
    /**
     * 从SharedPreferences中恢复夜间模式设置
     */
    private void restoreNightModePreference() {
        SharedPreferences preferences = getSharedPreferences(PREF_NIGHT_MODE, MODE_PRIVATE);
        int nightMode = preferences.getInt(PREF_NIGHT_MODE_VALUE, AppCompatDelegate.MODE_NIGHT_FOLLOW_SYSTEM);
        AppCompatDelegate.setDefaultNightMode(nightMode);
    }
} 