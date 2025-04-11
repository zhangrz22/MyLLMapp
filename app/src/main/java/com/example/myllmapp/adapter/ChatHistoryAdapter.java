package com.example.myllmapp.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myllmapp.R; // Import R class
import com.example.myllmapp.databinding.ItemChatHistoryBinding; // Import ViewBinding class
import com.example.myllmapp.model.Conversation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Adapter for displaying the list of conversations in a RecyclerView.
 * Uses ListAdapter for efficient updates.
 * 用于在 RecyclerView 中显示对话列表的 Adapter。
 * 使用 ListAdapter 实现高效更新。
 */
public class ChatHistoryAdapter extends ListAdapter<Conversation, ChatHistoryAdapter.ChatHistoryViewHolder> {

    private final OnConversationClickListener listener;
    private final SimpleDateFormat dateFormat; // For formatting timestamp / 用于格式化时间戳

    /**
     * Interface definition for a callback to be invoked when a conversation item is clicked.
     * 当对话项被点击时调用的回调接口定义。
     */
    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    /**
     * Constructor for ChatHistoryAdapter.
     * ChatHistoryAdapter 的构造函数。
     * @param listener Listener for item clicks. / 列表项点击的监听器。
     */
    public ChatHistoryAdapter(@NonNull OnConversationClickListener listener) {
        super(DIFF_CALLBACK);
        this.listener = listener;
        // Initialize date formatter / 初始化日期格式化器
        this.dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
    }

    /**
     * Creates new views (invoked by the layout manager).
     * 创建新的视图（由布局管理器调用）。
     */
    @NonNull
    @Override
    public ChatHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the layout using ViewBinding / 使用 ViewBinding 填充布局
        ItemChatHistoryBinding binding = ItemChatHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        // Pass the listener to the ViewHolder constructor
        // 将 listener 传递给 ViewHolder 构造函数
        return new ChatHistoryViewHolder(binding, listener);
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager).
     * 替换视图的内容（由布局管理器调用）。
     */
    @Override
    public void onBindViewHolder(@NonNull ChatHistoryViewHolder holder, int position) {
        Conversation currentConversation = getItem(position);
        // Pass the specific conversation to the bind method
        // 将特定的 conversation 传递给 bind 方法
        holder.bind(currentConversation, dateFormat);
    }

    /**
     * Provides a reference to the views for each data item.
     * ViewHolder should be static if it doesn't need direct access to the adapter's non-static members.
     * 提供对每个数据项视图的引用。
     * 如果 ViewHolder 不需要直接访问 adapter 的非静态成员，则应为 static。
     */
    static class ChatHistoryViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatHistoryBinding binding; // ViewBinding instance / ViewBinding 实例
        private final OnConversationClickListener listener; // Store the listener instance / 存储 listener 实例

        /**
         * Constructor for the ViewHolder.
         * ViewHolder 的构造函数。
         * @param binding The ViewBinding instance for the item layout. / 列表项布局的 ViewBinding 实例。
         * @param listener Listener for item clicks passed from the adapter. / 从 adapter 传递过来的列表项点击监听器。
         */
        ChatHistoryViewHolder(@NonNull ItemChatHistoryBinding binding, OnConversationClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;
            this.listener = listener; // Store the listener passed from the adapter / 存储从 adapter 传来的 listener
            // DO NOT set listener here if it needs the specific item data (like conversation)
            // 如果监听器需要特定的项目数据（如 conversation），不要在此处设置
        }

        /**
         * Binds conversation data to the views and sets the click listener.
         * 将对话数据绑定到视图并设置点击监听器。
         * @param conversation The conversation data object. / 对话数据对象。
         * @param dateFormat Formatter for the timestamp. / 用于时间戳的格式化器。
         */
        void bind(final Conversation conversation, SimpleDateFormat dateFormat) {
            // Format the start time and set it to the TextView
            // 格式化开始时间并设置到 TextView
            String formattedTime = dateFormat.format(new Date(conversation.startTime));
            // Ensure R.string.conversation_title_prefix exists! / 确保 R.string.conversation_title_prefix 存在！
            String title = itemView.getContext().getString(R.string.conversation_title_prefix) + " " + formattedTime;
            binding.textViewConversationTitle.setText(title);

            // Set click listener on the root view of the item HERE, using the specific 'conversation'
            // 在此处为列表项的根视图设置点击监听器，使用特定的 'conversation'
            binding.getRoot().setOnClickListener(v -> {
                if (listener != null) {
                    // Pass the specific conversation object for this item
                    // 传递此项特定的 conversation 对象
                    listener.onConversationClick(conversation);
                }
            });
        }
    }

    /**
     * DiffUtil.ItemCallback for calculating the difference between two non-null items in a list.
     * Used by ListAdapter to detect changes.
     * This should be static.
     * 用于计算列表中两个非空项之间差异的 DiffUtil.ItemCallback。
     * ListAdapter 使用它来检测更改。
     * 这应该是 static 的。
     */
    private static final DiffUtil.ItemCallback<Conversation> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Conversation>() {
                @Override
                public boolean areItemsTheSame(@NonNull Conversation oldItem, @NonNull Conversation newItem) {
                    // Check if items represent the same entity (e.g., by ID)
                    // 检查项是否代表同一个实体（例如，通过 ID）
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Conversation oldItem, @NonNull Conversation newItem) {
                    // Check if the content of the items is the same
                    // 检查项的内容是否相同
                    return oldItem.startTime == newItem.startTime;
                }
            };
}
