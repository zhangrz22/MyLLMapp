package com.example.myllmapp.api;

import android.content.Context;
import android.content.SharedPreferences;

public class ApiKeyManager {
    private static final String PREF_NAME = "api_settings";
    private static final String KEY_OPENAI_API_KEY = "openai_api_key";
    private static final String KEY_DASHSCOPE_API_KEY = "dashscope_api_key";

    private static SharedPreferences getPreferences(Context context) {
        return context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
    }

    public static void saveOpenAiApiKey(Context context, String apiKey) {
        getPreferences(context).edit()
                .putString(KEY_OPENAI_API_KEY, apiKey)
                .apply();
    }


    public static void saveDashScopeApiKey(Context context, String apiKey) {
        getPreferences(context).edit()
                .putString(KEY_DASHSCOPE_API_KEY, apiKey)
                .apply();
    }

    public static String getDashScopeApiKey(Context context) {
        return getPreferences(context).getString(KEY_DASHSCOPE_API_KEY, "");
    }
}