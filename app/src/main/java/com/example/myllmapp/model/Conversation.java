package com.example.myllmapp.model;

import androidx.room.Entity;
import androidx.room.PrimaryKey;

/**
 * Entity class representing a single conversation session.
 * 代表一个对话会话的实体类。
 */
@Entity(tableName = "conversations")
public class Conversation {

    @PrimaryKey(autoGenerate = true)
    public long id; // Unique ID for the conversation / 对话的唯一ID

    public long startTime; // Timestamp when the conversation started / 对话开始的时间戳

    // Optional: You could add a title field if needed
    // 可选：如果需要，可以添加标题字段
    // public String title;

    // Constructor, getters, and setters can be added if needed,
    // Room can work with public fields directly.
    // 如果需要，可以添加构造函数、getter和setter方法，
    // Room 可以直接使用公共字段。

    public Conversation(long startTime) {
        this.startTime = startTime;
    }
}
