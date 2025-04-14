package com.example.myllmapp.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
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

    private final OnConversationClickListener clickListener;
    private static final int MAX_TITLE_LENGTH = 50; // Define max title length / 定义标题最大长度

    /**
     * Interface for handling clicks on conversation items.
     * 用于处理对话项点击事件的接口。
     */
    public interface OnConversationClickListener {
        void onConversationClick(Conversation conversation);
    }

    /**
     * Constructor for ChatHistoryAdapter.
     * ChatHistoryAdapter 的构造函数。
     * @param clickListener Listener for conversation item clicks. / 对话项点击事件的监听器。
     */
    public ChatHistoryAdapter(OnConversationClickListener clickListener) {
        super(DIFF_CALLBACK);
        this.clickListener = clickListener;
    }

    /**
     * DiffUtil.ItemCallback for calculating the difference between two Conversation lists.
     * 用于计算两个 Conversation 列表之间差异的 DiffUtil.ItemCallback。
     */
    private static final DiffUtil.ItemCallback<Conversation> DIFF_CALLBACK =
            new DiffUtil.ItemCallback<Conversation>() {
                @Override
                public boolean areItemsTheSame(@NonNull Conversation oldItem, @NonNull Conversation newItem) {
                    // Check if items represent the same entity (by ID)
                    // 检查项是否代表同一个实体（通过 ID）
                    return oldItem.getId() == newItem.getId();
                }

                @Override
                public boolean areContentsTheSame(@NonNull Conversation oldItem, @NonNull Conversation newItem) {
                    // Check if the content of the items is the same
                    // 检查项的内容是否相同
                    // We need to compare displayTitle as well now
                    // 现在我们也需要比较 displayTitle
                    // --- 修改：使用 getStartTime ---
                    return oldItem.getStartTime() == newItem.getStartTime() &&
                            (oldItem.getDisplayTitle() != null ? oldItem.getDisplayTitle().equals(newItem.getDisplayTitle()) : newItem.getDisplayTitle() == null);
                    // --- 结束修改 ---
                }
            };

    @NonNull
    @Override
    public ConversationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout using ViewBinding / 使用 ViewBinding 填充项布局
        ItemChatHistoryBinding binding = ItemChatHistoryBinding.inflate(
                LayoutInflater.from(parent.getContext()), parent, false);
        return new ConversationViewHolder(binding, clickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull ConversationViewHolder holder, int position) {
        // Get the conversation at the current position / 获取当前位置的对话
        Conversation currentConversation = getItem(position);
        // Bind the conversation data to the ViewHolder / 将对话数据绑定到 ViewHolder
        holder.bind(currentConversation);
    }

    /**
     * ViewHolder class for conversation items.
     * 对话项的 ViewHolder 类。
     */
    static class ConversationViewHolder extends RecyclerView.ViewHolder {
        private final ItemChatHistoryBinding binding; // ViewBinding instance / ViewBinding 实例
        private final SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());

        /**
         * Constructor for ConversationViewHolder.
         * ConversationViewHolder 的构造函数。
         * @param binding The ViewBinding instance for the item layout. / 项布局的 ViewBinding 实例。
         * @param listener Listener for click events. / 点击事件的监听器。
         */
        ConversationViewHolder(ItemChatHistoryBinding binding, OnConversationClickListener listener) {
            super(binding.getRoot());
            this.binding = binding;

            // Set click listener for the item view / 为项视图设置点击监听器
            binding.getRoot().setOnClickListener(v -> {
                int position = getAdapterPosition();
                if (listener != null && position != RecyclerView.NO_POSITION) {
                    // Get the adapter associated with this ViewHolder
                    // 获取与此 ViewHolder 关联的适配器
                    RecyclerView.Adapter<?> adapter = getBindingAdapter();
                    if (adapter instanceof ChatHistoryAdapter) {
                        // Pass the clicked conversation object to the listener
                        // 将被点击的对话对象传递给监听器
                        listener.onConversationClick(((ChatHistoryAdapter) adapter).getItem(position));
                    }
                }
            });
        }


        /**
         * Binds conversation data to the views in the item layout.
         * 将对话数据绑定到项布局中的视图。
         * @param conversation The conversation object to bind. / 要绑定的对话对象。
         */
        void bind(Conversation conversation) {
            // --- 修改标题显示逻辑 ---
            String title = conversation.getDisplayTitle();
            if (title != null && !title.trim().isEmpty()) {
                // Use the first message as title, truncate if necessary
                // 使用第一条消息作为标题，必要时截断
                if (title.length() > MAX_TITLE_LENGTH) {
                    title = title.substring(0, MAX_TITLE_LENGTH) + "...";
                }
                binding.textViewConversationTitle.setText(title);
            } else {
                // Fallback: Use timestamp if no first message or title is empty
                // 回退：如果没有第一条消息或标题为空，则使用时间戳
                // --- 修改：使用 getStartTime ---
                String formattedDate = dateFormat.format(new Date(conversation.getStartTime()));
                // --- 结束修改 ---
                // Consider using a string resource for the prefix
                // 考虑为前缀使用字符串资源
                binding.textViewConversationTitle.setText(itemView.getContext().getString(R.string.conversation_title_prefix) + " " + formattedDate);
            }
            // --- 结束修改 ---

            // You might want a subtitle or keep the date somewhere else
            // 你可能想要一个副标题或将日期显示在其他地方
            // binding.textViewConversationDate.setText(dateFormat.format(new Date(conversation.getStartTime()))); // 如果需要显示日期，也用 getStartTime
        }
    }
}
