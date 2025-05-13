package com.example.myllmapp.model;

import androidx.room.ColumnInfo;
import androidx.room.Entity;
import androidx.room.ForeignKey;
import androidx.room.Index;
import androidx.room.PrimaryKey;
import androidx.room.TypeConverters; // Import if using TypeConverters
import androidx.room.Ignore;

import com.example.myllmapp.db.Converters;


/**
 * Entity class representing a single message within a conversation.
 * 代表对话中单条消息的实体类。
 */
@Entity(tableName = "messages",
        foreignKeys = @ForeignKey(entity = Conversation.class,
                parentColumns = "id",
                childColumns = "conversation_id",
                onDelete = ForeignKey.CASCADE), // Delete messages if conversation is deleted / 如果对话被删除，则删除消息
        indices = {@Index(value = "conversation_id")}) // Index for faster queries by conversation / 为按对话快速查询创建索引
@TypeConverters(Converters.class) // Add this if you have custom TypeConverters (e.g., for Sender Enum)
public class Message {

    @PrimaryKey(autoGenerate = true)
    public long id; // Unique ID for the message / 消息的唯一ID

    @ColumnInfo(name = "conversation_id")
    public long conversationId; // Foreign key linking to the Conversation / 关联到 Conversation 的外键

    public String text; // Content of the message / 消息内容

    public Sender sender; // Who sent the message (USER or LLM) / 消息发送者 (用户或大模型)

    public long timestamp; // When the message was sent/received / 消息发送/接收的时间戳

    // 用于流式输出的临时标志，不存入数据库
    @Ignore
    public boolean isStreaming = false;
    
    // 标准构造函数
    public Message(long conversationId, String text, Sender sender, long timestamp) {
        this.conversationId = conversationId;
        this.text = text;
        this.sender = sender;
        this.timestamp = timestamp;
        this.isStreaming = false;
    }
    
    // 创建空的LLM消息用于流式输出
    @Ignore
    public Message(long conversationId, Sender sender, long timestamp, boolean isStreaming) {
        this.conversationId = conversationId;
        this.text = "";
        this.sender = sender;
        this.timestamp = timestamp;
        this.isStreaming = isStreaming;
    }
}
