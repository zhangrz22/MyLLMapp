package com.example.myllmapp.db;

import androidx.lifecycle.LiveData; // Use LiveData for observing changes
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.myllmapp.model.Conversation;

import java.util.List;

/**
 * Data Access Object (DAO) for the Conversation entity.
 * Conversation 实体的 数据访问对象 (DAO)。
 */
@Dao
public interface ConversationDao {

    /**
     * Inserts a new conversation into the database. If the conversation already exists,
     * it replaces it. Returns the row ID of the newly inserted conversation.
     * 将新对话插入数据库。如果对话已存在，则替换它。返回新插入对话的行 ID。
     * Note: This operation should be performed on a background thread.
     * 注意：此操作应在后台线程执行。
     * @param conversation The conversation to insert. / 要插入的对话。
     * @return The row ID of the inserted conversation. / 插入对话的行 ID。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    long insertConversation(Conversation conversation);

    /**
     * Retrieves all conversations from the database, ordered by start time descending.
     * Uses LiveData to automatically update the UI when data changes.
     * 从数据库检索所有对话，按开始时间降序排列。
     * 使用 LiveData 在数据更改时自动更新 UI。
     * @return A LiveData list of all conversations. / 所有对话的 LiveData 列表。
     */
    @Query("SELECT * FROM conversations ORDER BY startTime DESC")
    LiveData<List<Conversation>> getAllConversations();

    /**
     * Retrieves a specific conversation by its ID.
     * 根据 ID 检索特定对话。
     * Note: This operation should be performed on a background thread.
     * 注意：此操作应在后台线程执行。
     * @param id The ID of the conversation. / 对话的 ID。
     * @return The Conversation object, or null if not found. / Conversation 对象，如果未找到则为 null。
     */
    @Query("SELECT * FROM conversations WHERE id = :id")
    Conversation getConversationById(long id); // Consider returning LiveData<Conversation> if needed for observation

    // Add other methods like update or delete if needed
    // 如果需要，添加其他方法，如更新或删除
}
