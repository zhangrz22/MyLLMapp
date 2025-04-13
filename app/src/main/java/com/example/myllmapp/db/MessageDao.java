package com.example.myllmapp.db;

import androidx.lifecycle.LiveData; // Use LiveData for observing changes
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.example.myllmapp.model.Message;

import java.util.List;

/**
 * Data Access Object (DAO) for the Message entity.
 * Message 实体的 数据访问对象 (DAO)。
 */
@Dao
public interface MessageDao {

    /**
     * Inserts a new message into the database.
     * 将新消息插入数据库。
     * Note: This operation should be performed on a background thread.
     * 注意：此操作应在后台线程执行。
     * @param message The message to insert. / 要插入的消息。
     */
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    void insertMessage(Message message);

    /**
     * Retrieves all messages for a specific conversation, ordered by timestamp ascending.
     * Uses LiveData to automatically update the UI when data changes.
     * 检索特定对话的所有消息，按时间戳升序排列。
     * 使用 LiveData 在数据更改时自动更新 UI。
     * @param conversationId The ID of the conversation. / 对话的 ID。
     * @return A LiveData list of messages for the conversation. / 该对话的消息 LiveData 列表。
     */
    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp ASC")
    LiveData<List<Message>> getMessagesForConversation(long conversationId);

    // Add other methods like update or delete if needed
    // 如果需要，添加其他方法，如更新或删除
}
