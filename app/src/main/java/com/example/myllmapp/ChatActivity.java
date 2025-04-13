package com.example.myllmapp;

import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log; // For logging / 用于日志记录
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
     * Sets up the RecyclerView with its adapter and layout manager.
     * 设置 RecyclerView 及其 Adapter 和 LayoutManager。
     */
    private void setupRecyclerView() {
        adapter = new MessageAdapter();
        layoutManager = new LinearLayoutManager(this);
        layoutManager.setStackFromEnd(true); // Start scrolled to the bottom / 从底部开始滚动
        binding.recyclerViewMessages.setAdapter(adapter);
        binding.recyclerViewMessages.setLayoutManager(layoutManager);

        // Scroll to bottom when new items are added / 当新项目添加时滚动到底部
        adapter.registerAdapterDataObserver(new RecyclerView.AdapterDataObserver() {
            @Override
            public void onItemRangeInserted(int positionStart, int itemCount) {
                super.onItemRangeInserted(positionStart, itemCount);
                scrollToBottom();
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
                Conversation newConversation = new Conversation(System.currentTimeMillis());
                long newId = conversationDao.insertConversation(newConversation);
                if (newId != -1L) { // Check if insert was successful / 检查插入是否成功
                    currentConversationId.set(newId); // Update the ID / 更新 ID
                    conversationId = newId;
                    Log.d(TAG, "Created new conversation with ID: " + conversationId);

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
     * Simulates getting a response from the LLM and saves it to the database.
     * **Placeholder:** Replace this with actual API calls.
     * 模拟从 LLM 获取响应并将其保存到数据库。
     * **占位符：** 请将此替换为实际的 API 调用。
     *
     * @param inputText The user's input text. / 用户的输入文本。
     * @param conversationId The ID of the current conversation. / 当前对话的 ID。
     */
    private void getLlmResponse(String inputText, long conversationId) {
        // Simulate network delay or processing time / 模拟网络延迟或处理时间
        try {
            Thread.sleep(500); // Simulate 0.5 second delay / 模拟 0.5 秒延迟
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
        }

        // **Placeholder Response Logic** / **占位符响应逻辑**
        String llmResponseText = "收到: \"" + inputText + "\" (这是模拟回复)";

        // Create and insert the LLM's message (still on background thread)
        // 创建并插入 LLM 的消息（仍在后台线程）
        Message llmMessage = new Message(conversationId, llmResponseText, Sender.LLM, System.currentTimeMillis());
        messageDao.insertMessage(llmMessage);
        Log.d(TAG, "Inserted LLM response: " + llmResponseText + " for convo ID: " + conversationId);

        // Note: The LiveData observer in observeMessages will automatically pick up this new message
        // and update the UI on the main thread.
        // 注意：observeMessages 中的 LiveData 观察者将自动获取此新消息并在主线程上更新 UI。
        // We might still need to explicitly scroll down after the LLM response appears.
        // LLM 响应出现后，我们可能仍需要显式向下滚动。
        // runOnUiThread(this::scrollToBottom); // Call scroll after LLM message is likely displayed
    }

    /**
     * Scrolls the RecyclerView to the last item.
     * 将 RecyclerView 滚动到最后一项。
     */
    private void scrollToBottom() {
        if (adapter != null && adapter.getItemCount() > 0) {
            // Use post to ensure scroll happens after layout calculation
            // 使用 post 确保滚动在布局计算之后发生
            binding.recyclerViewMessages.post(() ->
                    layoutManager.smoothScrollToPosition(binding.recyclerViewMessages, null, adapter.getItemCount() - 1)
            );
        }
    }
}
