package com.example.myllmapp;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider; // Consider using ViewModel later
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myllmapp.adapter.ChatHistoryAdapter;
import com.example.myllmapp.databinding.ActivityMainBinding; // Import ViewBinding
import com.example.myllmapp.db.AppDatabase;
import com.example.myllmapp.db.ConversationDao;
import com.example.myllmapp.model.Conversation;

/**
 * MainActivity displays the list of past conversations.
 * 主 Activity，显示过往对话列表。
 */
public class MainActivity extends AppCompatActivity implements ChatHistoryAdapter.OnConversationClickListener {

    private ActivityMainBinding binding; // ViewBinding instance / ViewBinding 实例
    private AppDatabase db;
    private ConversationDao conversationDao;
    private ChatHistoryAdapter adapter;

    public static final String EXTRA_CONVERSATION_ID = "com.example.myllmapp.CONVERSATION_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        // Inflate layout using ViewBinding / 使用 ViewBinding 填充布局
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        // Set the title / 设置标题
        setTitle(getString(R.string.chat_history));

        // Get database instance and DAO / 获取数据库实例和 DAO
        db = AppDatabase.getDatabase(getApplicationContext());
        conversationDao = db.conversationDao();

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
     * Updates the adapter and empty state view when data changes.
     * 观察来自 DAO 的对话 LiveData 流。
     * 当数据更改时更新 Adapter 和空状态视图。
     */
    private void observeConversations() {
        // Use LiveData provided by Room to observe changes / 使用 Room 提供的 LiveData 观察更改
        conversationDao.getAllConversations().observe(this, conversations -> {
            // Update the cached copy of the words in the adapter. / 更新 Adapter 中的缓存副本。
            adapter.submitList(conversations);

            // Show empty state text if the list is empty / 如果列表为空，显示空状态文本
            if (conversations == null || conversations.isEmpty()) {
                binding.textViewEmptyHistory.setVisibility(View.VISIBLE);
                binding.recyclerViewChatHistory.setVisibility(View.GONE);
            } else {
                binding.textViewEmptyHistory.setVisibility(View.GONE);
                binding.recyclerViewChatHistory.setVisibility(View.VISIBLE);
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
}
