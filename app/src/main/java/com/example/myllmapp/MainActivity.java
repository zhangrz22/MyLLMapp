package com.example.myllmapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.os.Handler; // 新增导入
import android.os.Looper;  // 新增导入

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.TooltipCompat;
import androidx.lifecycle.ViewModelProvider; // Consider using ViewModel later
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myllmapp.adapter.ChatHistoryAdapter;
import com.example.myllmapp.databinding.ActivityMainBinding; // Import ViewBinding
import com.example.myllmapp.db.AppDatabase;
import com.example.myllmapp.db.ConversationDao;
import com.example.myllmapp.db.MessageDao; // 新增导入
import com.example.myllmapp.model.Conversation;

import java.util.List; // 新增导入
import java.util.concurrent.ExecutorService; // 新增导入
import java.util.concurrent.Executors; // 新增导入


/**
 * MainActivity displays the list of past conversations.
 * 主 Activity，显示过往对话列表。
 */
public class MainActivity extends AppCompatActivity implements ChatHistoryAdapter.OnConversationClickListener {

    private ActivityMainBinding binding; // ViewBinding instance / ViewBinding 实例
    private AppDatabase db;
    private ConversationDao conversationDao;
    private MessageDao messageDao; // 新增：MessageDao 实例
    private ChatHistoryAdapter adapter;

    // 新增：用于后台任务的 ExecutorService 和用于主线程的 Handler
    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());


    public static final String EXTRA_CONVERSATION_ID = "com.example.myllmapp.CONVERSATION_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate layout using ViewBinding / 使用 ViewBinding 填充布局
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set the title / 设置标题
        setTitle(getString(R.string.chat_history));

        // Get database instance and DAOs / 获取数据库实例和 DAO
        db = AppDatabase.getDatabase(getApplicationContext());
        conversationDao = db.conversationDao();
        messageDao = db.messageDao(); // 初始化 messageDao

        // Setup RecyclerView / 设置 RecyclerView
        setupRecyclerView();

        // Observe conversation history from database / 观察来自数据库的对话历史
        observeConversations();

        // Setup FAB click listener / 设置 FAB 点击监听器
        binding.fabNewChat.setOnClickListener(v -> {
            // Start ChatActivity for a new conversation / 为新对话启动 ChatActivity
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            // Pass -1 or don't pass extra to indicate new chat / 传递 -1 或不传递 extra 来表示新对话
            intent.putExtra(EXTRA_CONVERSATION_ID, -1L);
            startActivity(intent);
        });

        // 为 FAB 设置 Tooltip
        TooltipCompat.setTooltipText(binding.fabNewChat, getString(R.string.new_chat_tooltip));
    }

    /**
     * Sets up the RecyclerView with its adapter and layout manager.
     * 设置 RecyclerView 及其 Adapter 和 LayoutManager。
     */
    private void setupRecyclerView() {
        adapter = new ChatHistoryAdapter(this); // Pass 'this' as the listener / 将 'this' 作为监听器传递
        binding.recyclerViewChatHistory.setAdapter(adapter);
        binding.recyclerViewChatHistory.setLayoutManager(new LinearLayoutManager(this));
    }

    /**
     * Observes the LiveData stream of conversations from the DAO.
     * Fetches the first message for each conversation on a background thread
     * to use as the title, then updates the adapter on the main thread.
     * 观察来自 DAO 的对话 LiveData 流。
     * 在后台线程为每个对话获取第一条消息作为标题，然后在主线程更新 Adapter。
     */
    private void observeConversations() {
        conversationDao.getAllConversations().observe(this, conversations -> {
            if (conversations == null) {
                // Handle null case if necessary, maybe clear adapter
                // 如果需要，处理 null 情况，可能需要清除 adapter
                adapter.submitList(null); // 清除列表
                binding.textViewEmptyHistory.setVisibility(View.VISIBLE);
                binding.recyclerViewChatHistory.setVisibility(View.GONE);
                return;
            }

            // Show empty state text immediately if the list is empty
            // 如果列表为空，立即显示空状态文本
            if (conversations.isEmpty()) {
                adapter.submitList(conversations); // 提交空列表以清除旧数据
                binding.textViewEmptyHistory.setVisibility(View.VISIBLE);
                binding.recyclerViewChatHistory.setVisibility(View.GONE);
            } else {
                binding.textViewEmptyHistory.setVisibility(View.GONE);
                binding.recyclerViewChatHistory.setVisibility(View.VISIBLE);
                // Process titles in the background only if list is not empty
                // 仅当列表不为空时才在后台处理标题
                databaseExecutor.execute(() -> {
                    // Fetch first message text for each conversation
                    // 为每个对话获取第一条消息文本
                    for (Conversation conversation : conversations) {
                        String firstMessage = messageDao.getFirstMessageTextSync(conversation.getId());
                        conversation.setDisplayTitle(firstMessage); // Store it in the temporary field / 将其存储在临时字段中
                    }

                    // Update the RecyclerView on the main thread
                    // 在主线程更新 RecyclerView
                    mainThreadHandler.post(() -> {
                        adapter.submitList(conversations);
                    });
                });
            }
        });
    }


    /**
     * Callback method from ChatHistoryAdapter.OnConversationClickListener.
     * Called when a conversation item is clicked.
     * 来自 ChatHistoryAdapter.OnConversationClickListener 的回调方法。
     * 当对话项被点击时调用。
     * @param conversation The conversation that was clicked. / 被点击的对话。
     */
    @Override
    public void onConversationClick(Conversation conversation) {
        // Start ChatActivity and pass the clicked conversation's ID / 启动 ChatActivity 并传递被点击对话的 ID
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra(EXTRA_CONVERSATION_ID, conversation.id);
        startActivity(intent);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        // Shutdown the executor service to prevent leaks
        // 关闭 executor service 防止内存泄漏
        databaseExecutor.shutdown();
    }
}
