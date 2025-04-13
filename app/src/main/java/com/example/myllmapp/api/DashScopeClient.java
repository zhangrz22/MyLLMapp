package com.example.myllmapp.api;

import android.util.Log;

import com.openai.client.OpenAIClient;
import com.openai.client.okhttp.OpenAIOkHttpClient;

public class DashScopeClient {
    private static final String TAG = "DashScopeClient";
    private static final String BASE_URL = "https://dashscope.aliyuncs.com/compatible-mode/v1";
    private static final String DEFAULT_MODEL = "qwen-plus"; // 默认使用的模型

    private static DashScopeClient instance;
    private final OpenAIClient client;

    private DashScopeClient(String apiKey) {
        client = OpenAIOkHttpClient.builder()
                .apiKey(apiKey)
                .baseUrl(BASE_URL)
                .build();
    }

    public static synchronized DashScopeClient getInstance(String apiKey) {
        if (instance == null) {
            instance = new DashScopeClient(apiKey);
        }
        return instance;
    }

    public OpenAIClient getClient() {
        return client;
    }

    public String getDefaultModel() {
        return DEFAULT_MODEL;
    }
}