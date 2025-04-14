package com.example.myllmapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton; // 新增导入

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.DiffUtil;
import androidx.recyclerview.widget.ListAdapter;
import androidx.recyclerview.widget.RecyclerView;

import com.example.myllmapp.R; // Ensure R file is imported correctly / 确保 R 文件被正确导入
import com.example.myllmapp.databinding.ItemChatHistoryBinding;
import com.example.myllmapp.model.Conversation;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * Adapter for displaying chat history items in a RecyclerView.
 * 用于在 RecyclerView 中显示聊天历史记录项的 Adapter。
 */
public class ChatHistoryAdapter extends ListAdapter<Conversation, ChatHistoryAdapter.ConversationViewHolder> {

    // --- 修改：扩展监听器接口 ---
    private final OnConversationInteractionListener interactionListener;
    private static final int MAX_TITLE_LENGTH = 50; // Define max title length / 定义标题最大长度

    /**
     * Interface for handling clicks and other interactions on conversation items.
     * 用于处理对话项点击和其他交互事件的接口。
     */
    public interface OnConversationInteractionListener {
        void onConversationClick(Conversation conversation); // 保留原有点击
        void onDeleteClick(Conversation conversation);      // 新增删除点击
    }
    // --- 结束修改 ---

    /**
     * Constructor for ChatHistoryAdapter.
     * ChatHistoryAdapter 的构造函数。
     * @param listener Listener for conversation item interactions. / 对话项交互事件的监听器。
     */
    // --- 修改：构造函数参数类型 ---
    public ChatHistoryAdapter(OnConversationInteractionListener listener) {
        super(DIFF_CALLBACK);
        this.interactionListener = listener;
    }
    // --- 结束修改 ---


    /**
     * DiffUtil.ItemCallback for calculating the difference between two Conversation lists.
     * 用于计算两个 Conversation 列表之间差异的 DiffUtil.ItemCallback。
     */
    private static final DiffUtil.ItemCallback<Conversation> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Conversation>() {
                @Override
                public boolean areItemsTheSame(@NonNull Conversation oldItem, @NonNull Conversation newItem) {
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Conversation oldItem, @NonNull Conversation newItem) {
                    return oldItem.getStartTime() == newItem.getStartTime() &&
                            (oldItem.getDisplayTitle() != null ? oldItem.getDisplayTitle().equals(newItem.getDisplayTitle()) : newItem.getDisplayTitle() == null);
                }
            };

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemChatHistoryBinding binding = ItemChatHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        // --- 修改：传递新的监听器 ---
        return new ConversationViewHolder(binding, interactionListener);
        // --- 结束修改 ---
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        Conversation currentConversation = getItem(position);
        holder.bind(currentConversation);
    }

    /**
     * ViewHolder class for conversation items.
     * 对话项的 ViewHolder 类。
     */
    // --- 修改：静态内部类持有 Adapter 引用不安全，改为非静态或通过 getBindingAdapter 获取 ---
    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatHistoryBinding binding;
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        /**
         * Constructor for ConversationViewHolder.
         * ConversationViewHolder 的构造函数。
         * @param binding The ViewBinding instance for the item layout. / 项布局的 ViewBinding 实例。
         * @param listener Listener for interaction events. / 交互事件的监听器。
         */
        // --- 修改：构造函数参数类型和内部逻辑 ---
        ConversationViewHolder(ItemChatHistoryBinding binding, OnConversationInteractionListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            // Set click listener for the entire item view / 为整个项视图设置点击监听器
            binding.getRoot().setOnClickListener(v -> {
                int position = getBindingAdapterPosition(); // Use getBindingAdapterPosition()
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    RecyclerView.Adapter<?> adapter = getBindingAdapter();
                    if (adapter instanceof ChatHistoryAdapter) {
                        listener.onConversationClick(((ChatHistoryAdapter) adapter).getItem(position));
                    }
                }
            });

            // --- 新增：为删除按钮设置点击监听器 ---
            if (binding.buttonDeleteConversation != null) { // Check if button exists in layout
                binding.buttonDeleteConversation.setOnClickListener(v -> {
                    int position = getBindingAdapterPosition(); // Use getBindingAdapterPosition()
                    if (listener != null && position != RecyclerView.NO_POSITION) {
                        RecyclerView.Adapter<?> adapter = getBindingAdapter();
                        if (adapter instanceof ChatHistoryAdapter) {
                            listener.onDeleteClick(((ChatHistoryAdapter) adapter).getItem(position));
                        }
                    }
                });
            }
            // --- 结束新增 ---
        }
        // --- 结束修改 ---


        /**
         * Binds conversation data to the views in the item layout.
         * 将对话数据绑定到项布局中的视图。
         * @param conversation The conversation object to bind. / 要绑定的对话对象。
         */
        void bind(Conversation conversation) {
            String title = conversation.getDisplayTitle();
            if (title != null && !title.trim().isEmpty()) {
                if (title.length() > MAX_TITLE_LENGTH) {
                    title = title.substring(0, MAX_TITLE_LENGTH) + "...";
                }
                binding.textViewConversationTitle.setText(title);
            } else {
                String formattedDate = dateFormat.format(new Date(conversation.getStartTime()));
                binding.textViewConversationTitle.setText(itemView.getContext().getString(R.string.conversation_title_prefix) + " " + formattedDate);
            }
        }
    }
}
