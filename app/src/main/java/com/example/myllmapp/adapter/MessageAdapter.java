package com.example.myllmapp.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.constraintlayout.widget.ConstraintLayout;
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
import java.util.List;
import java.util.ArrayList;
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

    public static final int VIEW_TYPE_USER = 1;
    public static final int VIEW_TYPE_LLM = 2;

    private final SimpleDateFormat timeFormat; // For formatting timestamp / 用于格式化时间戳
    private List<Message> currentList = new ArrayList<>(); // 当前的消息列表
    private Message streamingMessage; // 用于追踪当前正在流式输出的消息

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
     * 更新列表数据
     */
    @Override
    public void submitList(List<Message> list) {
        this.currentList = new ArrayList<>(list != null ? list : new ArrayList<>());
        super.submitList(list);
    }
    
    /**
     * 添加流式输出消息，如果不存在则创建
     * @param message 初始消息对象
     * @return 添加的消息在列表中的位置
     */
    public int addOrUpdateStreamingMessage(Message message) {
        List<Message> newList = new ArrayList<>(currentList);
        
        // 如果有流式消息，先更新它
        if (streamingMessage != null) {
            for (int i = 0; i < newList.size(); i++) {
                if (newList.get(i).isStreaming) {
                    newList.set(i, message);
                    streamingMessage = message;
                    super.submitList(newList);
                    notifyItemChanged(i);
                    return i;
                }
            }
        }
        
        // 如果没有找到流式消息，添加新的
        message.isStreaming = true;
        streamingMessage = message;
        newList.add(message);
        super.submitList(newList);
        currentList = newList;
        return newList.size() - 1;
    }
    
    /**
     * 更新流式消息的文本
     * @param newText 新的消息文本
     */
    public void updateStreamingMessageText(String newText) {
        if (streamingMessage != null) {
            for (int i = 0; i < currentList.size(); i++) {
                Message current = currentList.get(i);
                if (current.isStreaming) {
                    // 仅当文本实际变化时才更新，减少不必要的刷新
                    if (!current.text.equals(newText)) {
                        current.text = newText;
                        streamingMessage = current;
                        
                        // 使用部分更新模式，只更新文本内容而不是整个视图
                        // 这减少了重绘开销，提高了流式输出的性能
                        notifyItemChanged(i, newText);
                    }
                    return;
                }
            }
        }
    }
    
    /**
     * 完成流式输出，将消息标记为非流式
     * @param finalMessage 最终消息对象
     */
    public void finishStreamingMessage(Message finalMessage) {
        if (streamingMessage != null) {
            List<Message> newList = new ArrayList<>(currentList);
            for (int i = 0; i < newList.size(); i++) {
                if (newList.get(i).isStreaming) {
                    finalMessage.isStreaming = false;
                    newList.set(i, finalMessage);
                    // 使用DiffUtil更新列表，确保动画平滑
                    final int updatePosition = i;  // 创建一个final变量来在匿名内部类中使用
                    DiffUtil.DiffResult diffResult = DiffUtil.calculateDiff(new DiffUtil.Callback() {
                        @Override
                        public int getOldListSize() {
                            return currentList.size();
                        }

                        @Override
                        public int getNewListSize() {
                            return newList.size();
                        }

                        @Override
                        public boolean areItemsTheSame(int oldItemPosition, int newItemPosition) {
                            return oldItemPosition == newItemPosition;
                        }

                        @Override
                        public boolean areContentsTheSame(int oldItemPosition, int newItemPosition) {
                            Message oldItem = currentList.get(oldItemPosition);
                            Message newItem = newList.get(newItemPosition);
                            if (oldItemPosition == updatePosition) {  // 使用final变量而不是i
                                return false; // 强制更新此项
                            }
                            return oldItem.text.equals(newItem.text) &&
                                    oldItem.timestamp == newItem.timestamp &&
                                    oldItem.sender.equals(newItem.sender) &&
                                    oldItem.isStreaming == newItem.isStreaming;
                        }
                    });
                    
                    currentList = newList;
                    streamingMessage = null;
                    
                    // 使用计算出的差异更新UI
                    diffResult.dispatchUpdatesTo(this);
                    return;
                }
            }
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
            
            // 等待布局完成后检查文本行数
            binding.textViewMessageText.post(() -> {
                int lineCount = binding.textViewMessageText.getLineCount();
                
                if (lineCount == 1 && message.text.length() < 30) {
                    // 短消息使用自适应宽度
                    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    // 保持原有约束
                    params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.horizontalBias = 1.0f; // 右对齐
                    binding.textViewMessageText.setLayoutParams(params);
                } else {
                    // 长消息使用最大宽度限制
                    // 计算父容器宽度的80%作为最大宽度
                    int maxWidth = (int)(binding.getRoot().getWidth() * 0.8);
                    binding.textViewMessageText.setMaxWidth(maxWidth);
                }
            });
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
            
            // 等待布局完成后检查文本行数
            binding.textViewMessageText.post(() -> {
                int lineCount = binding.textViewMessageText.getLineCount();
                
                if (lineCount == 1 && message.text.length() < 30) {
                    // 短消息使用自适应宽度
                    ConstraintLayout.LayoutParams params = new ConstraintLayout.LayoutParams(
                            ViewGroup.LayoutParams.WRAP_CONTENT,
                            ViewGroup.LayoutParams.WRAP_CONTENT
                    );
                    // 保持原有约束
                    params.topToTop = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.endToEnd = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.startToStart = ConstraintLayout.LayoutParams.PARENT_ID;
                    params.horizontalBias = 0.0f; // 左对齐
                    binding.textViewMessageText.setLayoutParams(params);
                } else {
                    // 长消息使用最大宽度限制
                    // 计算父容器宽度的80%作为最大宽度
                    int maxWidth = (int)(binding.getRoot().getWidth() * 0.8);
                    binding.textViewMessageText.setMaxWidth(maxWidth);
                }
            });
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
                    // 如果是流式消息，总是认为是相同的项
                    if (oldItem.isStreaming && newItem.isStreaming) {
                        return true;
                    }
                    return oldItem.id == newItem.id;
                }

                @Override
                public boolean areContentsTheSame(@NonNull Message oldItem, @NonNull Message newItem) {
                    // 如果是流式消息且文本不同，则认为内容不同
                    if ((oldItem.isStreaming || newItem.isStreaming) && !oldItem.text.equals(newItem.text)) {
                        return false;
                    }
                    
                    // 比较相关字段
                    return oldItem.text.equals(newItem.text) &&
                            oldItem.timestamp == newItem.timestamp &&
                            oldItem.sender.equals(newItem.sender);
                }
            };
}
