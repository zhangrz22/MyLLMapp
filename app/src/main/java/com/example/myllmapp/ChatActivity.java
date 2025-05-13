package com.example.myllmapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // For logging / 用于日志记录
import android.view.Menu;
import android.view.MenuItem;
import android.view.View; // 添加View的导入
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myllmapp.adapter.MessageAdapter;
import com.example.myllmapp.databinding.ActivityChatBinding; // Import ViewBinding
import com.example.myllmapp.db.AppDatabase;
import com.example.myllmapp.db.ConversationDao;
import com.example.myllmapp.db.MessageDao;
import com.example.myllmapp.model.Conversation;
import com.example.myllmapp.model.Message;
import com.example.myllmapp.model.Sender;

import java.util.List;
import java.util.concurrent.atomic.AtomicLong; // For handling conversation ID across threads

// 导入必要的类
import com.example.myllmapp.api.ApiKeyManager;
import com.example.myllmapp.api.DashScopeClient;
import com.alibaba.dashscope.aigc.generation.GenerationResult;
import com.alibaba.dashscope.exception.ApiException;
import com.alibaba.dashscope.exception.InputRequiredException;
import com.alibaba.dashscope.exception.NoApiKeyException;

import java.util.ArrayList;
import java.util.List;

import io.reactivex.Flowable;

/**
 * ChatActivity handles the conversation interaction between the user and the LLM.
 * ChatActivity 处理用户与大模型之间的对话交互。
 */
public class ChatActivity extends AppCompatActivity {

    private static final String TAG = "ChatActivity"; // Log tag / 日志标签
    private ActivityChatBinding binding; // ViewBinding instance / ViewBinding 实例
    private AppDatabase db;
    private MessageDao messageDao;
    private ConversationDao conversationDao;
    private MessageAdapter adapter;
    private LinearLayoutManager layoutManager;

    // 在ChatActivity.java中添加成员变量
    private static final String DEFAULT_MODEL = "qwen-plus";
    private static final double DEFAULT_TEMPERATURE = 0.7;
    private static final int DEFAULT_MAX_TOKENS = 500;
    private static final int MAX_HISTORY_MESSAGES = 200; // 最多保留多少条历史消息
    private boolean isLoadingResponse = false; // 标记是否正在加载LLM响应

    private AtomicLong currentConversationId = new AtomicLong(-1L); // Use AtomicLong for thread safety / 使用 AtomicLong 保证线程安全, -1 indicates new conversation / -1 表示新对话

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityChatBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set the title / 设置标题
        setTitle(getString(R.string.chat_with_llm));

        // Get database instance and DAOs / 获取数据库实例和 DAO
        db = AppDatabase.getDatabase(getApplicationContext());
        messageDao = db.messageDao();
        conversationDao = db.conversationDao();

        // Setup RecyclerView / 设置 RecyclerView
        setupRecyclerView();
        
        // 设置工具栏
        setSupportActionBar(binding.toolbar);
        // 启用返回箭头
        if (getSupportActionBar() != null) {
            getSupportActionBar().setDisplayHomeAsUpEnabled(true);
            getSupportActionBar().setDisplayShowHomeEnabled(true);
        }
        
        // 设置工具栏导航点击监听器
        binding.toolbar.setNavigationOnClickListener(v -> {
            // 返回上一个界面
            onBackPressed();
        });

        // Get conversation ID from intent / 从 Intent 获取对话 ID
        long conversationIdFromIntent = getIntent().getLongExtra(MainActivity.EXTRA_CONVERSATION_ID, -1L);
        currentConversationId.set(conversationIdFromIntent);

        // Load messages if it's an existing conversation / 如果是现有对话，则加载消息
        if (currentConversationId.get() != -1L) {
            observeMessages(currentConversationId.get());
        } else {
            // It's a new conversation, messages will be loaded dynamically as they are added
            // 这是新对话，消息将在添加时动态加载
            // (Alternatively, observeMessages can be called initially with -1,
            // and the observer re-attached when the ID becomes valid)
            // (或者，可以用 -1 初始化调用 observeMessages，并在 ID 有效时重新附加观察者)
        }

        // Setup send button click listener / 设置发送按钮点击监听器
        binding.buttonSendMessage.setOnClickListener(v -> sendMessage());

        // Scroll to bottom when keyboard appears (optional, requires manifest adjustment)
        // 当键盘出现时滚动到底部（可选，需要调整 manifest）
        // Add android:windowSoftInputMode="adjustResize" to <activity> in AndroidManifest.xml
        binding.recyclerViewMessages.addOnLayoutChangeListener((v, left, top, right, bottom, oldLeft, oldTop, oldRight, oldBottom) -> {
            if (bottom < oldBottom) {
                scrollToBottom();
            }
        });
    }

    /**
     * 创建菜单
     */
    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_chat, menu);
        return true;
    }

    /**
     * 处理菜单项点击事件
     */
    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.action_settings) {
            // 启动设置活动，并传递当前对话ID
            Intent intent = new Intent(this, SettingsActivity.class);
            intent.putExtra(SettingsActivity.EXTRA_CONVERSATION_ID, currentConversationId.get());
            startActivity(intent);
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    /**
     * Sets up the RecyclerView with its adapter and layout manager.
     * 设置 RecyclerView 及其 Adapter 和 LayoutManager。
     */
    private void setupRecyclerView() {
        adapter = new MessageAdapter();
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start scrolled to the bottom / 从底部开始滚动
        
        // 优化RecyclerView性能
        binding.recyclerViewMessages.setHasFixedSize(false); // 消息项大小不固定
        binding.recyclerViewMessages.setItemViewCacheSize(20); // 增加缓存大小
        binding.recyclerViewMessages.setDrawingCacheEnabled(true);
        binding.recyclerViewMessages.setDrawingCacheQuality(View.DRAWING_CACHE_QUALITY_HIGH);
        
        // 设置预取和缓存机制
        RecyclerView.RecycledViewPool viewPool = new RecyclerView.RecycledViewPool();
        viewPool.setMaxRecycledViews(MessageAdapter.VIEW_TYPE_USER, 10);
        viewPool.setMaxRecycledViews(MessageAdapter.VIEW_TYPE_LLM, 10);
        binding.recyclerViewMessages.setRecycledViewPool(viewPool);
        
        // 启用预取
        binding.recyclerViewMessages.setItemAnimator(null); // 流式更新不需要动画，禁用可提高性能
        binding.recyclerViewMessages.getRecycledViewPool().setMaxRecycledViews(MessageAdapter.VIEW_TYPE_LLM, 20);
        
        binding.recyclerViewMessages.setAdapter(adapter);
        binding.recyclerViewMessages.setLayoutManager(layoutManager);

        // Scroll to bottom when new items are added / 当新项目添加时滚动到底部
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                scrollToBottom();
            }
            
            @Override
            public void onItemRangeChanged(int positionStart, int itemCount) {
                super.onItemRangeChanged(positionStart, itemCount);
                // 如果更新的是最后一项并且在加载响应，则滚动到底部
                if (positionStart + itemCount == adapter.getItemCount() && isLoadingResponse) {
                    scrollToBottom();
                }
            }
        });
    }

    /**
     * Observes the LiveData stream of messages for the current conversation.
     * 观察当前对话的消息 LiveData 流。
     * @param conversationId The ID of the conversation to observe. / 要观察的对话 ID。
     */
    private void observeMessages(long conversationId) {
        messageDao.getMessagesForConversation(conversationId).observe(this, messages -> {
            adapter.submitList(messages);
            // Optional: Scroll to bottom after initial load or updates
            // 可选：初始加载或更新后滚动到底部
            // scrollToBottom(); // Might cause jumpiness if called too often
        });
    }

    /**
     * Handles sending a user message.
     * 处理发送用户消息。
     */
    private void sendMessage() {
        String messageText = binding.editTextMessageInput.getText().toString().trim();
        if (TextUtils.isEmpty(messageText)) {
            return; // Don't send empty messages / 不发送空消息
        }

        // Clear input field immediately / 立即清除输入框
        binding.editTextMessageInput.setText("");

        // Use background thread for database operations / 对数据库操作使用后台线程
        AppDatabase.databaseWriteExecutor.execute(() -> {
            long conversationId = currentConversationId.get();

            // 1. Create Conversation if it's new / 1. 如果是新对话，则创建 Conversation
            if (conversationId == -1L) {
                // 获取当前选择的模型
                String selectedModel = SettingsActivity.getSelectedModel(this);
                boolean defaultEnableSearch = getSharedPreferences(SettingsActivity.PREF_NAME, MODE_PRIVATE)
                        .getBoolean(SettingsActivity.PREF_ENABLE_SEARCH, false);
                // deepseek-v3模型下强制关闭enableSearch
                if ("deepseek-v3".equals(selectedModel)) {
                    defaultEnableSearch = false;
                }
                Log.i(TAG, "新建对话时enableSearch默认值: " + defaultEnableSearch);
                // 创建带有模型信息和enableSearch的新对话
                Conversation newConversation = new Conversation(System.currentTimeMillis(), selectedModel, defaultEnableSearch);
                long newId = conversationDao.insertConversation(newConversation);
                if (newId != -1L) { // Check if insert was successful / 检查插入是否成功
                    currentConversationId.set(newId); // Update the ID / 更新 ID
                    conversationId = newId;
                    Log.d(TAG, "Created new conversation with ID: " + conversationId + " using model: " + selectedModel);

                    // Start observing messages for the newly created conversation on the main thread
                    // 在主线程上开始观察新创建对话的消息
                    long finalConversationId = conversationId; // Need final variable for lambda
                    runOnUiThread(() -> observeMessages(finalConversationId));

                } else {
                    Log.e(TAG, "Failed to create new conversation in database.");
                    // Show error message on main thread / 在主线程显示错误消息
                    runOnUiThread(() -> Toast.makeText(ChatActivity.this, "Error starting chat", Toast.LENGTH_SHORT).show());
                    return; // Stop processing if conversation creation failed / 如果对话创建失败则停止处理
                }
            }

            // 2. Create and insert the user's message / 2. 创建并插入用户消息
            Message userMessage = new Message(conversationId, messageText, Sender.USER, System.currentTimeMillis());
            messageDao.insertMessage(userMessage);
            Log.d(TAG, "Inserted user message: " + messageText + " for convo ID: " + conversationId);

            // 3. Simulate LLM response / 3. 模拟 LLM 响应
            getLlmResponse(messageText, conversationId);
        });
    }


    /**
     * 使用DashScope API获取LLM响应
     * @param inputText 用户的输入文本
     * @param conversationId 当前对话的ID
     */
    private void getLlmResponse(String inputText, long conversationId) {
        // 标记正在加载
        isLoadingResponse = true;

        // 在后台线程中进行API调用
        AppDatabase.databaseWriteExecutor.execute(() -> {
            try {
                // 1. 获取API Key
                String apiKey = BuildConfig.DASHSCOPE_API_KEY;

                if (apiKey.isEmpty()) {
                    apiKey = ApiKeyManager.getDashScopeApiKey(ChatActivity.this);
                    if (apiKey.isEmpty()) {
                        runOnUiThread(() -> {
                            Toast.makeText(ChatActivity.this,
                                    "请设置DashScope API Key", Toast.LENGTH_LONG).show();
                            isLoadingResponse = false;
                        });
                        return;
                    }
                }

                // 2. 获取对话历史
                List<Message> historyMessages = messageDao.getRecentMessagesForConversation(
                        conversationId, MAX_HISTORY_MESSAGES);
                
                // 3. 获取当前对话使用的模型和联网搜索开关
                Conversation currentConversation = conversationDao.getConversationById(conversationId);
                // 如果对话没有指定模型，使用设置中的模型
                String modelToUse = (currentConversation != null && currentConversation.model != null) 
                        ? currentConversation.model 
                        : SettingsActivity.getSelectedModel(this);
                boolean enableSearch = (currentConversation != null) && currentConversation.isEnableSearch();
                // deepseek-v3模型下强制关闭enableSearch
                if ("deepseek-v3".equals(modelToUse)) {
                    enableSearch = false;
                }
                
                // 4. 转换消息格式
                List<com.alibaba.dashscope.common.Message> dashScopeMessages = DashScopeClient.convertToDashScopeMessages(historyMessages, enableSearch);

                // 5. 检查是否使用流式输出
                boolean useStreaming = SettingsActivity.useStreaming(this);
                
                // 打印请求内容到logcat
                Log.i(TAG, "LLM API Request: model=" + modelToUse + ", enableSearch=" + enableSearch + ", useStreaming=" + useStreaming);
                for (com.alibaba.dashscope.common.Message msg : dashScopeMessages) {
                    Log.i(TAG, "Message: role=" + msg.getRole() + ", content=" + msg.getContent());
                }

                // 6. 创建一个新线程来执行API调用，因为这可能会阻塞
                final String finalApiKey = apiKey;
                final List<com.alibaba.dashscope.common.Message> finalDashScopeMessages = dashScopeMessages;
                final boolean finalEnableSearch = enableSearch;
                final String finalModelToUse = modelToUse;
                
                new Thread(() -> {
                    try {
                        if (useStreaming) {
                            // 使用流式输出模式
                            handleStreamingResponse(finalModelToUse, finalApiKey, finalDashScopeMessages, finalEnableSearch, conversationId);
                        } else {
                            // 使用非流式输出模式
                            handleNonStreamingResponse(finalModelToUse, finalApiKey, finalDashScopeMessages, finalEnableSearch, conversationId);
                        }
                    } catch (NoApiKeyException | ApiException | InputRequiredException e) {
                        // 处理API调用异常
                        AppDatabase.databaseWriteExecutor.execute(() -> {
                            isLoadingResponse = false;
                            handleApiError("API调用失败: " + e.getMessage());
                        });
                    }
                }).start();

            } catch (Exception e) {
                // 处理一般异常
                Log.e(TAG, "Error in getLlmResponse", e);
                isLoadingResponse = false;
                handleApiError("发生错误: " + e.getMessage());
            }
        });
    }
    
    /**
     * 处理非流式响应
     */
    private void handleNonStreamingResponse(String modelToUse, String apiKey, 
                                          List<com.alibaba.dashscope.common.Message> dashScopeMessages,
                                          boolean enableSearch, long conversationId) 
            throws NoApiKeyException, ApiException, InputRequiredException {
        
        // 执行API调用
        GenerationResult result = DashScopeClient.callWithMessages(modelToUse, apiKey, dashScopeMessages, enableSearch);

        // 处理响应
        AppDatabase.databaseWriteExecutor.execute(() -> {
            isLoadingResponse = false;

            try {
                // 获取回复内容
                String llmResponseText = result.getOutput().getChoices().get(0).getMessage().getContent();

                // 创建并插入LLM消息到数据库
                Message llmMessage = new Message(
                        conversationId,
                        llmResponseText,
                        Sender.LLM,
                        System.currentTimeMillis()
                );
                messageDao.insertMessage(llmMessage);

                Log.d(TAG, "Inserted LLM response using model: " + modelToUse);

                // 更新UI
                runOnUiThread(this::scrollToBottom);

            } catch (Exception e) {
                handleApiError("解析响应失败: " + e.getMessage());
            }
        });
    }
    
    /**
     * 处理流式响应
     */
    private void handleStreamingResponse(String modelToUse, String apiKey, 
                                       List<com.alibaba.dashscope.common.Message> dashScopeMessages,
                                       boolean enableSearch, long conversationId) 
            throws NoApiKeyException, ApiException, InputRequiredException {
        
        // 创建初始的空LLM消息用于流式输出
        final Message initialStreamingMessage = new Message(
                conversationId,
                Sender.LLM,
                System.currentTimeMillis(),
                true
        );
        
        // 在UI线程上添加初始流式消息
        runOnUiThread(() -> {
            adapter.addOrUpdateStreamingMessage(initialStreamingMessage);
            scrollToBottom();
        });
        
        StringBuilder fullResponseBuilder = new StringBuilder();
        
        try {
            // 执行流式API调用
            Flowable<GenerationResult> resultFlowable = DashScopeClient.streamCallWithMessages(
                    modelToUse, apiKey, dashScopeMessages, enableSearch, 
                    generationResult -> {
                        try {
                            // 获取增量响应
                            String incrementalContent = generationResult.getOutput().getChoices().get(0).getMessage().getContent();
                            fullResponseBuilder.append(incrementalContent);
                            
                            // 更新UI上的流式消息
                            String currentFullText = fullResponseBuilder.toString();
                            runOnUiThread(() -> {
                                adapter.updateStreamingMessageText(currentFullText);
                                
                                // 延迟执行滚动，避免频繁滚动引起的性能问题
                                if (fullResponseBuilder.length() % 50 == 0) {  // 每累积约50个字符滚动一次
                                    scrollToBottom();
                                }
                            });
                            
                            Log.d(TAG, "Stream increment: " + incrementalContent);
                        } catch (Exception e) {
                            Log.e(TAG, "Error processing stream increment", e);
                        }
                    });
            
            // 等待流完成
            resultFlowable.blockingSubscribe();
            
            // 流式输出完成后，将完整响应保存到数据库
            AppDatabase.databaseWriteExecutor.execute(() -> {
                isLoadingResponse = false;
                try {
                    String finalResponse = fullResponseBuilder.toString();
                    // 创建最终消息
                    Message finalMessage = new Message(
                            conversationId,
                            finalResponse,
                            Sender.LLM,
                            System.currentTimeMillis()
                    );
                    
                    // 更新UI并完成流式消息
                    runOnUiThread(() -> {
                        adapter.finishStreamingMessage(finalMessage);
                        
                        // 最后一次滚动，确保显示完整内容
                        scrollToBottom();
                    });
                    
                    // 保存到数据库
                    messageDao.insertMessage(finalMessage);
                    Log.d(TAG, "Inserted streamed LLM response using model: " + modelToUse);
                    
                } catch (Exception e) {
                    handleApiError("处理流式响应失败: " + e.getMessage());
                }
            });
            
        } catch (Exception e) {
            isLoadingResponse = false;
            handleApiError("流式API调用失败: " + e.getMessage());
            
            // 尝试移除流式消息
            runOnUiThread(() -> {
                Message errorMessage = new Message(
                        conversationId,
                        "获取回复时出错",
                        Sender.LLM,
                        System.currentTimeMillis()
                );
                adapter.finishStreamingMessage(errorMessage);
            });
        }
    }

    /**
     * 处理API错误
     * @param errorMessage 错误消息
     */
    private void handleApiError(String errorMessage) {
        Log.e(TAG, errorMessage);

        runOnUiThread(() -> {
            Toast.makeText(ChatActivity.this,
                    "获取LLM响应失败", Toast.LENGTH_SHORT).show();

            // 可选：插入一条错误消息到对话中
            AppDatabase.databaseWriteExecutor.execute(() -> {
                Message errorMsg = new Message(
                        currentConversationId.get(),
                        "无法获取回复，请稍后再试。",
                        Sender.LLM,
                        System.currentTimeMillis()
                );
                messageDao.insertMessage(errorMsg);
            });
        });
    }
    /**
     * Scrolls the RecyclerView to the last item.
     * 将 RecyclerView 滚动到最后一项。
     */
    private void scrollToBottom() {
        if (adapter != null && adapter.getItemCount() > 0) {
            // Use post to ensure scroll happens after layout calculation
            // 使用 post 确保滚动在布局计算之后发生
            binding.recyclerViewMessages.post(() -> {
                try {
                    // 对于流式输出，使用带偏移的滚动以确保消息完全可见
                    if (isLoadingResponse) {
                        // 使用即时滚动避免卡顿
                        int lastPos = adapter.getItemCount() - 1;
                        // 设置偏移量为0，确保消息在最底部完全可见
                        layoutManager.scrollToPositionWithOffset(lastPos, 0);
                    } else {
                        // 非流式情况使用平滑滚动提供更好的用户体验
                        binding.recyclerViewMessages.smoothScrollToPosition(adapter.getItemCount() - 1);
                    }
                } catch (Exception e) {
                    Log.e(TAG, "滚动错误", e);
                    // 如果平滑滚动失败，尝试直接滚动
                    layoutManager.scrollToPosition(adapter.getItemCount() - 1);
                }
            });
        }
    }


}
