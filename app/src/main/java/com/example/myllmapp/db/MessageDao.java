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


    /**
     * 获取某个对话的最近N条消息 (同步)
     * Gets the N most recent messages for a specific conversation (synchronous).
     * Should be called from a background thread.
     * @param conversationId 对话ID / The ID of the conversation.
     * @param limit 消息数量限制 / The maximum number of messages to retrieve.
     * @return 消息列表，按时间升序排列 / A list of messages, ordered by timestamp ascending.
     */
    @Query("SELECT * FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp ASC LIMIT :limit")
    List<Message> getRecentMessagesForConversation(long conversationId, int limit);


    /**
     * Gets the text of the first message for a specific conversation (synchronous).
     * Should be called from a background thread.
     * 同步获取特定对话的第一条消息的文本。应在后台线程调用。
     * @param conversationId The ID of the conversation. / 对话的 ID。
     * @return The text of the first message, or null if no messages exist. / 第一条消息的文本，如果不存在消息则为 null。
     */
    @Query("SELECT text FROM messages WHERE conversation_id = :conversationId ORDER BY timestamp ASC LIMIT 1")
    String getFirstMessageTextSync(long conversationId);


    /**
     * Deletes all messages for a specific conversation.
     * 删除特定对话的所有消息。
     * Note: This operation should be performed on a background thread.
     * 注意：此操作应在后台线程执行。
     * @param conversationId The ID of the conversation to delete messages from. / 要删除消息的对话的 ID。
     */
    @Query("DELETE FROM messages WHERE conversation_id = :conversationId")
    void deleteMessagesForConversation(long conversationId); // 之前版本中存在，根据需要保留或移除


}
