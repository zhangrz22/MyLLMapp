package com.example.myllmapp;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.os.Handler;
import android.os.Looper;
import android.widget.Toast; // 新增导入
import android.widget.ImageButton;
import android.content.res.Configuration;

import androidx.appcompat.app.AlertDialog; // 新增导入
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.TooltipCompat;
import androidx.lifecycle.ViewModelProvider; // Consider using ViewModel later
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.myllmapp.adapter.ChatHistoryAdapter;
import com.example.myllmapp.databinding.ActivityMainBinding; // Import ViewBinding
import com.example.myllmapp.db.AppDatabase;
import com.example.myllmapp.db.ConversationDao;
import com.example.myllmapp.db.MessageDao;
import com.example.myllmapp.model.Conversation;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 * MainActivity displays the list of past conversations.
 * 主 Activity，显示过往对话列表。
 */
// --- 修改：实现新的监听器接口 ---
public class MainActivity extends AppCompatActivity implements ChatHistoryAdapter.OnConversationInteractionListener {
// --- 结束修改 ---

    private ActivityMainBinding binding;
    private AppDatabase db;
    private ConversationDao conversationDao;
    private MessageDao messageDao;
    private ChatHistoryAdapter adapter;
    private ImageButton btnToggleNightMode;

    private static final String PREF_NIGHT_MODE = "night_mode_pref";
    private static final String PREF_NIGHT_MODE_VALUE = "night_mode_value";

    private final ExecutorService databaseExecutor = Executors.newSingleThreadExecutor();
    private final Handler mainThreadHandler = new Handler(Looper.getMainLooper());


    public static final String EXTRA_CONVERSATION_ID = "com.example.myllmapp.CONVERSATION_ID";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        setTitle(getString(R.string.chat_history));

        db = AppDatabase.getDatabase(getApplicationContext());
        conversationDao = db.conversationDao();
        messageDao = db.messageDao();

        setupRecyclerView();
        observeConversations();
        setupNightModeToggle();

        binding.fabNewChat.setOnClickListener(v -> {
            Intent intent = new Intent(MainActivity.this, ChatActivity.class);
            intent.putExtra(EXTRA_CONVERSATION_ID, -1L);
            startActivity(intent);
        });

        TooltipCompat.setTooltipText(binding.fabNewChat, getString(R.string.new_chat_tooltip));
    }

    /**
     * 设置夜间模式切换按钮
     */
    private void setupNightModeToggle() {
        btnToggleNightMode = binding.toolbar.findViewById(R.id.btnToggleNightMode);
        
        // 根据当前模式设置合适的图标
        updateNightModeButtonIcon();
        
        // 设置按钮点击事件
        btnToggleNightMode.setOnClickListener(v -> {
            // 获取当前主题模式
            int currentNightMode = getResources().getConfiguration().uiMode 
                    & Configuration.UI_MODE_NIGHT_MASK;
            
            if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
                // 当前是夜间模式，切换到日间模式
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                Toast.makeText(this, R.string.night_mode_off, Toast.LENGTH_SHORT).show();
                saveNightModePreference(AppCompatDelegate.MODE_NIGHT_NO);
            } else {
                // 当前是日间模式，切换到夜间模式
                AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                Toast.makeText(this, R.string.night_mode_on, Toast.LENGTH_SHORT).show();
                saveNightModePreference(AppCompatDelegate.MODE_NIGHT_YES);
            }
        });
    }
    
    /**
     * 保存夜间模式设置到SharedPreferences
     */
    private void saveNightModePreference(int nightMode) {
        SharedPreferences preferences = getSharedPreferences(PREF_NIGHT_MODE, MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt(PREF_NIGHT_MODE_VALUE, nightMode);
        editor.apply();
    }
    
    /**
     * 根据当前主题模式更新切换按钮图标
     */
    private void updateNightModeButtonIcon() {
        int currentNightMode = getResources().getConfiguration().uiMode 
                & Configuration.UI_MODE_NIGHT_MASK;
        
        if (currentNightMode == Configuration.UI_MODE_NIGHT_YES) {
            // 当前是夜间模式，显示太阳图标
            btnToggleNightMode.setImageResource(R.drawable.ic_sun);
        } else {
            // 当前是日间模式，显示月亮图标
            btnToggleNightMode.setImageResource(R.drawable.ic_moon);
        }
    }

    /**
     * Sets up the RecyclerView with its adapter and layout manager.
     * 设置 RecyclerView 及其 Adapter 和 LayoutManager。
     */
    private void setupRecyclerView() {
        // --- 修改：传递 this 作为新的监听器类型 ---
        adapter = new ChatHistoryAdapter(this);
        // --- 结束修改 ---
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
                adapter.submitList(null);
                binding.textViewEmptyHistory.setVisibility(View.VISIBLE);
                binding.recyclerViewChatHistory.setVisibility(View.GONE);
                return;
            }

            if (conversations.isEmpty()) {
                adapter.submitList(conversations);
                binding.textViewEmptyHistory.setVisibility(View.VISIBLE);
                binding.recyclerViewChatHistory.setVisibility(View.GONE);
            } else {
                binding.textViewEmptyHistory.setVisibility(View.GONE);
                binding.recyclerViewChatHistory.setVisibility(View.VISIBLE);
                databaseExecutor.execute(() -> {
                    for (Conversation conversation : conversations) {
                        String firstMessage = messageDao.getFirstMessageTextSync(conversation.getId());
                        conversation.setDisplayTitle(firstMessage);
                    }
                    mainThreadHandler.post(() -> {
                        adapter.submitList(conversations);
                    });
                });
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // 检查并更新夜间模式按钮图标
        updateNightModeButtonIcon();
    }

    /**
     * Callback method from ChatHistoryAdapter.OnConversationInteractionListener.
     * Called when a conversation item is clicked.
     * 来自 ChatHistoryAdapter.OnConversationInteractionListener 的回调方法。
     * 当对话项被点击时调用。
     * @param conversation The conversation that was clicked. / 被点击的对话。
     */
    @Override
    public void onConversationClick(Conversation conversation) {
        Intent intent = new Intent(MainActivity.this, ChatActivity.class);
        intent.putExtra(EXTRA_CONVERSATION_ID, conversation.id);
        startActivity(intent);
    }

    /**
     * Callback method from ChatHistoryAdapter.OnConversationInteractionListener.
     * Called when the delete button on a conversation item is clicked.
     * Shows a confirmation dialog before proceeding with deletion.
     * 来自 ChatHistoryAdapter.OnConversationInteractionListener 的回调方法。
     * 当对话项上的删除按钮被点击时调用。
     * 在执行删除前显示确认对话框。
     * @param conversation The conversation to be deleted. / 要被删除的对话。
     */
    @Override
    public void onDeleteClick(Conversation conversation) {
        new AlertDialog.Builder(this)
                .setTitle(R.string.delete_conversation_title) // Use existing string resource
                .setMessage(R.string.delete_conversation_message) // Use existing string resource
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    // Call method to delete conversation and its messages
                    // 调用方法删除对话及其消息
                    deleteConversationAndMessages(conversation.getId());
                })
                .setNegativeButton(R.string.cancel, null) // Use existing string resource
                .setIcon(android.R.drawable.ic_dialog_alert) // Optional: add an icon
                .show();
    }
    /**
     * Deletes a conversation and all its associated messages from the database.
     * This operation is performed on a background thread.
     * 从数据库中删除一个对话及其所有关联的消息。
     * 此操作在后台线程执行。
     * @param conversationId The ID of the conversation to delete. / 要删除的对话的 ID。
     */
    private void deleteConversationAndMessages(long conversationId) {
        databaseExecutor.execute(() -> {
            // First delete messages associated with the conversation
            // 首先删除与对话关联的消息
            messageDao.deleteMessagesForConversation(conversationId); // Ensure this method exists and works

            // Then delete the conversation itself
            // 然后删除对话本身
            conversationDao.deleteConversationById(conversationId);

            // Optionally, show a confirmation toast on the main thread
            // 可选：在主线程显示确认 Toast
            mainThreadHandler.post(() -> {
                Toast.makeText(MainActivity.this, "Conversation deleted", Toast.LENGTH_SHORT).show();
            });
            // The LiveData observer for conversations will automatically update the list
            // 对话的 LiveData 观察者将自动更新列表
        });
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        databaseExecutor.shutdown();
    }
}
