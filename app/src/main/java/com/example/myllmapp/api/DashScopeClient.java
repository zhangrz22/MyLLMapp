package com.example.myllmapp.api;

import com.alibaba.dashscope.aigc.generation.Generation;
import com.alibaba.dashscope.aigc.generation.GenerationParam;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.aigc.generation.SearchOptions;
import com.alibaba.dashscope.common.Message;
import com.alibaba.dashscope.common.Role;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;

import java.util.ArrayList;
import java.util.List;
import java.util.HashMap;
import java.util.Map;
import android.util.Log;
import io.reactivex.Flowable;
import io.reactivex.functions.Consumer;


public class DashScopeClient {
    private static final String TAG = "DashScopeClient";
    public static final String DEFAULT_MODEL = "qwen-plus";

    private static DashScopeClient instance;
    private final String apiKey;

    private DashScopeClient(String apiKey) {
        this.apiKey = apiKey;
    }

    public static synchronized DashScopeClient getInstance(String apiKey) {
        if (instance == null) {
            instance = new DashScopeClient(apiKey);
        }
        return instance;
    }

    /**
     * 调用 DashScope 生成接口，支持 enableSearch
     */
    public static GenerationResult callWithMessages(String model, String apiKey, List<Message> messages, boolean enableSearch)
            throws ApiException, NoApiKeyException, InputRequiredException {

        for (Message msg : messages) {
            if (msg.getRole().equals(Role.SYSTEM.getValue()) && msg.getContent().contains("model:")) {
                // 可扩展：从system prompt中提取model
            }
        }
        Generation gen = new Generation();

        SearchOptions searchOptions = SearchOptions.builder()
            .enableSource(true)
            .enableCitation(true)
            .citationFormat("[ref_<number>]")
            .forcedSearch(false)
            .searchStrategy("standard")
            .build();


        GenerationParam param = GenerationParam.builder()
                .apiKey(apiKey)
                .model(model)
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .enableSearch(enableSearch)
                .searchOptions(searchOptions)
                .build();
        return gen.call(param);
    }
    
    /**
     * 流式调用DashScope生成接口，支持enableSearch
     * @param model 模型名称
     * @param apiKey API密钥
     * @param messages 消息列表
     * @param enableSearch 是否启用搜索
     * @param messageHandler 处理流式消息的回调
     * @return Flowable流对象
     */
    public static Flowable<GenerationResult> streamCallWithMessages(
            String model, 
            String apiKey, 
            List<Message> messages, 
            boolean enableSearch,
            Consumer<GenerationResult> messageHandler)
            throws ApiException, NoApiKeyException, InputRequiredException {
        
        Generation gen = new Generation();
        
        SearchOptions searchOptions = SearchOptions.builder()
            .enableSource(true)
            .enableCitation(true)
            .citationFormat("[ref_<number>]")
            .forcedSearch(false)
            .searchStrategy("standard")
            .build();
            
        GenerationParam param = GenerationParam.builder()
                .apiKey(apiKey)
                .model(model)
                .messages(messages)
                .resultFormat(GenerationParam.ResultFormat.MESSAGE)
                .enableSearch(enableSearch)
                .searchOptions(searchOptions)
                .incrementalOutput(true)  // 启用增量输出
                .build();
                
        Flowable<GenerationResult> result = gen.streamCall(param);
        
        // 处理流式结果
        if (messageHandler != null) {
            return result.doOnNext(messageHandler);
        }
        
        return result;
    }

    /**
     * 工具方法：将你的 Message 实体转为 DashScope Message
     */
    public static List<Message> convertToDashScopeMessages(List<com.example.myllmapp.model.Message> appMessages, boolean enableSearch) {
        List<Message> dashScopeMessages = new ArrayList<>();
        // 系统提示
        StringBuilder systemPrompt = new StringBuilder("你是一个友好、有帮助的助手，能以用户使用的语言回答用户的问题。");
        if (enableSearch) {
            systemPrompt.append("记住, 你可以使用联网搜索");
        }
        dashScopeMessages.add(Message.builder()
                .role(Role.SYSTEM.getValue())
                .content(systemPrompt.toString())
                .build());
        for (com.example.myllmapp.model.Message msg : appMessages) {
            String role = msg.sender == com.example.myllmapp.model.Sender.USER
                    ? Role.USER.getValue() : Role.ASSISTANT.getValue();
            dashScopeMessages.add(Message.builder()
                    .role(role)
                    .content(msg.text)
                    .build());
        }
        return dashScopeMessages;
    }

}