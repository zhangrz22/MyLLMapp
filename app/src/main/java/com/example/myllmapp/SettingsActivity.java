package com.example.myllmapp;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SwitchCompat;

import com.example.myllmapp.databinding.ActivitySettingsBinding;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class SettingsActivity extends AppCompatActivity {

    public static final String PREF_NAME = "MyLLMAppSettings";
    public static final String PREF_USE_STREAMING = "use_streaming";
    public static final String PREF_MODEL = "selected_model";
    
    // 定义支持的模型
    private static final Map<String, String> SUPPORTED_MODELS = new HashMap<>();
    static {
        SUPPORTED_MODELS.put("通义千问-plus-latest", "qwen-plus-latest");
        SUPPORTED_MODELS.put("通义千问3-235B-A22B", "qwen3-235b-a22b");
        SUPPORTED_MODELS.put("通义千问3-30B-A3B", "qwen3-30b-a3b");
    }
    
    private ActivitySettingsBinding binding;
    private SharedPreferences sharedPreferences;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

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
        
        // 设置开关状态为当前配置
        boolean useStreaming = sharedPreferences.getBoolean(PREF_USE_STREAMING, true);
        switchStreaming.setChecked(useStreaming);
        
        // 设置开关变化监听器
        switchStreaming.setOnCheckedChangeListener((buttonView, isChecked) -> {
            // 保存新设置
            SharedPreferences.Editor editor = sharedPreferences.edit();
            editor.putBoolean(PREF_USE_STREAMING, isChecked);
            editor.apply();
            
            // 显示保存成功提示
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
                
                // 保存选中的模型ID
                SharedPreferences.Editor editor = sharedPreferences.edit();
                editor.putString(PREF_MODEL, selectedModelId);
                editor.apply();
                
                // 显示保存成功提示
                Toast.makeText(SettingsActivity.this, R.string.settings_saved, Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // 什么也不做
            }
        });
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
        return prefs.getString(PREF_MODEL, "qwen-plus-latest"); // 默认为通义千问-plus-latest
    }
} 