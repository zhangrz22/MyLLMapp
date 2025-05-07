package com.example.myllmapp.db;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Delete; // If you want to delete by object

import com.example.myllmapp.model.Conversation;

import java.util.List;

/**
 * Data Access Object for the Conversation entity.
 * Conversation 实体的 数据访问对象 (DAO)。
 */
@Dao
public interface ConversationDao {

    /**
     * Inserts a new conversation into the database.
     * 将新对话插入数据库。
     * @param conversation The conversation to insert. / 要插入的对话。
     * @return The row ID of the newly inserted conversation. / 新插入对话的行 ID。
     */
    @Insert
    long insertConversation(Conversation conversation);

    /**
     * Retrieves all conversations, ordered by start time descending.
     * 检索所有对话，按开始时间降序排列。
     * @return A LiveData list of all conversations. / 所有对话的 LiveData 列表。
     */
    @Query("SELECT * FROM conversations ORDER BY startTime DESC")
    LiveData<List<Conversation>> getAllConversations();

    /**
     * Retrieves a specific conversation by its ID.
     * 根据 ID 检索特定对话。
     * @param id The ID of the conversation. / 对话的 ID。
     * @return The conversation object, or null if not found. / 对话对象，如果未找到则为 null。
     */
    @Query("SELECT * FROM conversations WHERE id = :id")
    Conversation getConversationById(long id); // Might need background thread / 可能需要后台线程

    /**
     * 更新对话使用的模型
     * @param id 对话ID
     * @param model 模型名称
     */
    @Query("UPDATE conversations SET model = :model WHERE id = :id")
    void updateConversationModel(long id, String model);

    // --- 新增方法 ---
    /**
     * Deletes a conversation by its ID.
     * Should be called from a background thread.
     * 根据 ID 删除对话。应在后台线程调用。
     * @param id The ID of the conversation to delete. / 要删除的对话的 ID。
     */
    @Query("DELETE FROM conversations WHERE id = :id")
    void deleteConversationById(long id);
    // --- 结束新增 ---

    // Optional: Delete by object instance
    // 可选：通过对象实例删除
    // @Delete
    // void deleteConversation(Conversation conversation);

}
