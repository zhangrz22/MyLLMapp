package com.example.myllmapp;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.myllmapp.databinding.ActivitySettingsBinding;
import com.example.myllmapp.db.AppDatabase;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREF_NAME = "MyLLMAppSettings";
    public static final String PREF_USE_STREAMING = "use_streaming";
    public static final String PREF_MODEL = "selected_model";
    public static final String PREF_ENABLE_SEARCH = "enable_search";
    public static final String EXTRA_CONVERSATION_ID = "conversation_id";
    
    // 定义支持的模型
    private static final Map<String, String> SUPPORTED_MODELS = new HashMap<>();
    // 需要强制流式输出的模型ID集合
    public static final java.util.Set<String> FORCE_STREAMING_MODELS = new java.util.HashSet<>();
    static {
        SUPPORTED_MODELS.put("通义千问3", "qwen-plus-latest");
        SUPPORTED_MODELS.put("通义千问3-Turbo", "qwen-turbo-latest");
        SUPPORTED_MODELS.put("通义千问3-235B-A22B", "qwen3-235b-a22b");
        SUPPORTED_MODELS.put("通义千问3-30B-A3B", "qwen3-30b-a3b");
        SUPPORTED_MODELS.put("通义千问3-32B", "qwen3-32b");
        SUPPORTED_MODELS.put("DeepSeek-R1", "deepseek-r1");
        // 保留原有模型（如有）
        SUPPORTED_MODELS.put("通义千问-plus", "qwen-plus");
        SUPPORTED_MODELS.put("deepseek-v3", "deepseek-v3");

        // 需要强制流式输出的模型ID
        FORCE_STREAMING_MODELS.add("qwen-plus-latest");
        FORCE_STREAMING_MODELS.add("qwen-turbo-latest");
        FORCE_STREAMING_MODELS.add("qwen3-235b-a22b");
        FORCE_STREAMING_MODELS.add("qwen3-30b-a3b");
        FORCE_STREAMING_MODELS.add("qwen3-32b");
        FORCE_STREAMING_MODELS.add("deepseek-r1");
        // 你可以根据需要添加更多模型ID
    }
    
    private ActivitySettingsBinding binding;
    private SharedPreferences sharedPreferences;
    private long currentConversationId = -1;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // 获取传入的对话ID
        Intent intent = getIntent();
        if (intent != null) {
            currentConversationId = intent.getLongExtra(EXTRA_CONVERSATION_ID, -1);
        }

        // 设置工具栏
        setSupportActionBar(binding.toolbar);
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setTitle(R.string.settings);
        }
        
        // 设置工具栏导航点击监听器
        binding.toolbar.setNavigationOnClickListener(v -> onBackPressed());

        // 获取SharedPreferences
        sharedPreferences = getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        
        // 设置模型选择下拉框
        setupModelSpinner();
        
        // 获取流式输出开关
        SwitchCompat switchStreaming = binding.switchStreaming;
        
        // 获取当前模型ID
        String currentModelId = sharedPreferences.getString(PREF_MODEL, "qwen-plus-latest");
        // 设置开关状态为当前配置
        boolean useStreaming = sharedPreferences.getBoolean(PREF_USE_STREAMING, true);
        // 如果当前模型需要强制流式输出，则强制开关为true且禁用
        if (FORCE_STREAMING_MODELS.contains(currentModelId)) {
            switchStreaming.setChecked(true);
            switchStreaming.setEnabled(false);
            // 强制保存为true
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(PREF_USE_STREAMING, true);
            editor.apply();
        } else {
            switchStreaming.setChecked(useStreaming);
            switchStreaming.setEnabled(true);
        }
        
        // 设置开关变化监听器
        switchStreaming.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 如果当前模型需要强制流式输出，则不允许用户更改
            String modelNow = sharedPreferences.getString(PREF_MODEL, "qwen-plus-latest");
            if (FORCE_STREAMING_MODELS.contains(modelNow)) {
                buttonView.setChecked(true);
                switchStreaming.setEnabled(false);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(PREF_USE_STREAMING, true);
                editor.apply();
                return;
            }
            // 保存新设置
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(PREF_USE_STREAMING, isChecked);
            editor.apply();
            // 显示保存成功提示
            Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        });

        // 获取联网搜索开关
        SwitchCompat switchNetworkSearch = binding.switchNetworkSearch;
        
        // 设置开关状态为当前配置
        boolean enableSearch = sharedPreferences.getBoolean(PREF_ENABLE_SEARCH, false);
        // 推理模型不允许联网搜索
        if (FORCE_STREAMING_MODELS.contains(currentModelId)) {
            switchNetworkSearch.setChecked(false);
            switchNetworkSearch.setEnabled(false);
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(PREF_ENABLE_SEARCH, false);
            editor.apply();
        } else {
            switchNetworkSearch.setChecked(enableSearch);
            switchNetworkSearch.setEnabled(true);
        }
        
        // 设置开关变化监听器
        switchNetworkSearch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            String modelNow = sharedPreferences.getString(PREF_MODEL, "qwen-plus-latest");
            if (FORCE_STREAMING_MODELS.contains(modelNow) || "deepseek-v3".equals(modelNow)) {
                buttonView.setChecked(false);
                switchNetworkSearch.setEnabled(false);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putBoolean(PREF_ENABLE_SEARCH, false);
                editor.apply();
                return;
            }
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(PREF_ENABLE_SEARCH, isChecked);
            editor.apply();
            if (currentConversationId != -1) {
                updateCurrentConversationSearch(isChecked);
            }
            Toast.makeText(this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
        });
    }
    
    /**
     * 设置模型选择下拉框
     */
    private void setupModelSpinner() {
        // 准备模型名称列表（显示给用户的）
        List<String> modelNameList = new ArrayList<>(SUPPORTED_MODELS.keySet());
        
        // 创建适配器
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, modelNameList);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        
        // 设置适配器
        binding.spinnerModel.setAdapter(adapter);
        
        // 获取保存的模型ID
        String savedModelId = sharedPreferences.getString(PREF_MODEL, "qwen-plus-latest");
        
        // 查找对应的显示名称在列表中的位置
        int position = 0;
        for (Map.Entry<String, String> entry : SUPPORTED_MODELS.entrySet()) {
            if (entry.getValue().equals(savedModelId)) {
                position = modelNameList.indexOf(entry.getKey());
                break;
            }
        }
        
        // 设置默认选择
        binding.spinnerModel.setSelection(position);
        
        // 设置选择监听器
        binding.spinnerModel.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedModelName = (String) parent.getItemAtPosition(position);
                String selectedModelId = SUPPORTED_MODELS.get(selectedModelName);
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PREF_MODEL, selectedModelId);
                editor.apply();
                // 联动enable_search开关：推理模型和deepseek-v3都不允许联网搜索
                if (FORCE_STREAMING_MODELS.contains(selectedModelId) || "deepseek-v3".equals(selectedModelId)) {
                    binding.switchNetworkSearch.setChecked(false);
                    binding.switchNetworkSearch.setEnabled(false);
                    SharedPreferences.Editor e2 = sharedPreferences.edit();
                    e2.putBoolean(PREF_ENABLE_SEARCH, false);
                    e2.apply();
                    if (currentConversationId != -1) {
                        updateCurrentConversationSearch(false);
                    }
                } else {
                    binding.switchNetworkSearch.setEnabled(true);
                    boolean enableSearch = sharedPreferences.getBoolean(PREF_ENABLE_SEARCH, false);
                    binding.switchNetworkSearch.setChecked(enableSearch);
                }
                // 联动流式输出开关
                if (FORCE_STREAMING_MODELS.contains(selectedModelId)) {
                    binding.switchStreaming.setChecked(true);
                    binding.switchStreaming.setEnabled(false);
                    SharedPreferences.Editor e3 = sharedPreferences.edit();
                    e3.putBoolean(PREF_USE_STREAMING, true);
                    e3.apply();
                } else {
                    boolean useStreaming = sharedPreferences.getBoolean(PREF_USE_STREAMING, true);
                    binding.switchStreaming.setChecked(useStreaming);
                    binding.switchStreaming.setEnabled(true);
                }
                updateCurrentConversationModel(selectedModelId);
                Toast.makeText(SettingsActivity.this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 什么也不做
            }
        });
    }
    
    /**
     * 更新当前对话的模型
     * @param modelId 模型ID
     */
    private void updateCurrentConversationModel(String modelId) {
        if (currentConversationId != -1) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                db.conversationDao().updateConversationModel(currentConversationId, modelId);
            });
        }
    }
    
    /**
     * 更新当前对话的联网搜索设置
     * @param enableSearch 是否启用联网搜索
     */
    private void updateCurrentConversationSearch(boolean enableSearch) {
        if (currentConversationId != -1) {
            AppDatabase.databaseWriteExecutor.execute(() -> {
                AppDatabase db = AppDatabase.getDatabase(getApplicationContext());
                db.conversationDao().updateConversationSearch(currentConversationId, enableSearch);
            });
        }
    }
    
    /**
     * 获取是否使用流式输出设置
     * @param context 上下文
     * @return 是否使用流式输出
     */
    public static boolean useStreaming(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getBoolean(PREF_USE_STREAMING, true); // 默认启用流式输出
    }
    
    /**
     * 获取当前选择的模型
     * @param context 上下文
     * @return 模型ID
     */
    public static String getSelectedModel(Context context) {
        SharedPreferences prefs = context.getSharedPreferences(PREF_NAME, Context.MODE_PRIVATE);
        return prefs.getString(PREF_MODEL, "qwen-plus"); // 默认为通义千问-plus-latest
    }
} 