package com.example.myllmapp.model;

import androidx.room.Entity;
import androidx.room.Ignore; // 新增：导入 @Ignore
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

    // 对话使用的模型
    public String model;

    // --- 新增字段 ---
    /**
     * Temporary field to hold the title to be displayed in the history list.
     * Not stored in the database.
     * 用于在历史记录列表中显示的临时标题字段。不存储在数据库中。
     */
    @Ignore
    public String displayTitle;
    // --- 结束新增 ---


    // Optional: You could add a title field if needed
    // 可选：如果需要，可以添加标题字段
    // public String title;

    // --- 新增：Room 需要一个无参构造函数 ---
    /**
     * Default constructor required by Room.
     * Room 需要的默认构造函数。
     */
    public Conversation() {}
    // --- 结束新增 ---


    /**
     * Constructor used potentially outside of Room.
     * 可能在 Room 之外使用的构造函数。
     * @param startTime Timestamp when the conversation started. / 对话开始的时间戳。
     */
    @Ignore // Mark this constructor as ignored for Room to avoid conflicts / 标记此构造函数以便 Room 忽略，避免冲突
    public Conversation(long startTime) {
        this.startTime = startTime;
    }

    /**
     * Constructor with model parameter.
     * 带有model参数的构造函数。
     * @param startTime Timestamp when the conversation started. / 对话开始的时间戳。
     * @param model The model used for this conversation. / 此对话使用的模型。
     */
    @Ignore
    public Conversation(long startTime, String model) {
        this.startTime = startTime;
        this.model = model;
    }

    // --- 新增 Getter/Setter for displayTitle ---
    public String getDisplayTitle() {
        return displayTitle;
    }

    public void setDisplayTitle(String displayTitle) {
        this.displayTitle = displayTitle;
    }
    // --- 结束新增 ---

    // --- 保留原有的 Getter/Setter (如果需要) ---
    // Room可以直接访问公共字段，但保留 Getter/Setter 也是好习惯
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public long getStartTime() {
        return startTime;
    }

    public void setStartTime(long startTime) {
        this.startTime = startTime;
    }
    
    public String getModel() {
        return model;
    }
    
    public void setModel(String model) {
        this.model = model;
    }
    // --- 结束保留 ---
}
