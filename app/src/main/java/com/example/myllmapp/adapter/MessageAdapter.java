package com.example.myllmapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewbinding.ViewBinding; // Generic ViewBinding

import com.example.myllmapp.databinding.ItemMessageLlmBinding; // LLM message binding
import com.example.myllmapp.databinding.ItemMessageUserBinding; // User message binding
import com.example.myllmapp.model.Message;
import com.example.myllmapp.model.Sender;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Adapter for displaying chat messages in a RecyclerView.
 * Handles different view types for user messages and LLM messages.
 * Uses ListAdapter for efficient updates.
 * 用于在 RecyclerView 中显示聊天消息的 Adapter。
 * 处理用户消息和 LLM 消息的不同视图类型。
 * 使用 ListAdapter 实现高效更新。
 */
public class MessageAdapter extends ListAdapter<Message, RecyclerView.ViewHolder> {

    private static final int VIEW_TYPE_USER = 1;
    private static final int VIEW_TYPE_LLM = 2;

    private final SimpleDateFormat timeFormat; // For formatting timestamp / 用于格式化时间戳

    /**
     * Constructor for MessageAdapter.
     * MessageAdapter 的构造函数。
     */
    public MessageAdapter() {
        super(DIFF_CALLBACK);
        // Initialize time formatter / 初始化时间格式化器
        this.timeFormat = new SimpleDateFormat("HH:mm", Locale.getDefault());
    }

    /**
     * Returns the view type of the item at the given position.
     * 返回给定位置项的视图类型。
     * @param position Position of the item. / 项的位置。
     * @return Integer value representing the view type (VIEW_TYPE_USER or VIEW_TYPE_LLM). / 代表视图类型的整数值。
     */
    @Override
    public int getItemViewType(int position) {
        Message message = getItem(position);
        if (message.sender == Sender.USER) {
            return VIEW_TYPE_USER;
        } else {
            return VIEW_TYPE_LLM;
        }
    }

    /**
     * Creates new views (invoked by the layout manager).
     * 创建新的视图（由布局管理器调用）。
     */
    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        // Inflate the correct layout based on the view type / 根据视图类型填充正确的布局
        if (viewType == VIEW_TYPE_USER) {
            ItemMessageUserBinding binding = ItemMessageUserBinding.inflate(inflater, parent, false);
            return new UserMessageViewHolder(binding);
        } else { // VIEW_TYPE_LLM
            ItemMessageLlmBinding binding = ItemMessageLlmBinding.inflate(inflater, parent, false);
            return new LlmMessageViewHolder(binding);
        }
    }

    /**
     * Replaces the contents of a view (invoked by the layout manager).
     * 替换视图的内容（由布局管理器调用）。
     */
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Message message = getItem(position);
        String formattedTime = timeFormat.format(new Date(message.timestamp));

        // Bind data based on the ViewHolder type / 根据 ViewHolder 类型绑定数据
        if (holder.getItemViewType() == VIEW_TYPE_USER) {
            ((UserMessageViewHolder) holder).bind(message, formattedTime);
        } else { // VIEW_TYPE_LLM
            ((LlmMessageViewHolder) holder).bind(message, formattedTime);
        }
    }

    /**
     * ViewHolder for user messages.
     * 用户消息的 ViewHolder。
     */
    static class UserMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageUserBinding binding;

        UserMessageViewHolder(@NonNull ItemMessageUserBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Message message, String formattedTime) {
            binding.textViewMessageText.setText(message.text);
            binding.textViewMessageTimestamp.setText(formattedTime);
        }
    }

    /**
     * ViewHolder for LLM messages.
     * LLM 消息的 ViewHolder。
     */
    static class LlmMessageViewHolder extends RecyclerView.ViewHolder {
        private final ItemMessageLlmBinding binding;

        LlmMessageViewHolder(@NonNull ItemMessageLlmBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Message message, String formattedTime) {
            binding.textViewMessageText.setText(message.text);
            binding.textViewMessageTimestamp.setText(formattedTime);
        }
    }

    /**
     * DiffUtil.ItemCallback for calculating the difference between two non-null messages.
     * 用于计算两个非空消息之间差异的 DiffUtil.ItemCallback。
     */
    private static final DiffUtil.ItemCallback<Message> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Message>() {
                @Override
                public boolean areItemsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
                    // Compare relevant fields / 比较相关字段
                    return oldItem.text.equals(newItem.text) &&
                            oldItem.timestamp == newItem.timestamp &&
                            oldItem.sender.equals(newItem.sender);
                }
            };
}
