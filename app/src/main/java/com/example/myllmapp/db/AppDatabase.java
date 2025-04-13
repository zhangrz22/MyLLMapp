package com.example.myllmapp.db;

import android.content.Context;

import androidx.room.Database;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.room.TypeConverters; // Import TypeConverters

import com.example.myllmapp.model.Conversation;
import com.example.myllmapp.model.Message;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * The Room database for the application.
 * 应用程序的 Room 数据库。
 */
@Database(entities = {Conversation.class, Message.class}, version = 1, exportSchema = false)
@TypeConverters({Converters.class}) // Register the Converters class / 注册 Converters 类
public abstract class AppDatabase extends RoomDatabase {

    // Define abstract methods for each DAO
    // 为每个 DAO 定义抽象方法
    public abstract ConversationDao conversationDao();
    public abstract MessageDao messageDao();

    private static volatile AppDatabase INSTANCE; // Singleton instance / 单例实例
    private static final int NUMBER_OF_THREADS = 4; // Number of threads for database operations / 数据库操作的线程数

    // Executor service for running database operations asynchronously on a background thread
    // 用于在后台线程上异步运行数据库操作的 Executor 服务
    public static final ExecutorService databaseWriteExecutor =
            Executors.newFixedThreadPool(NUMBER_OF_THREADS);

    /**
     * Gets the singleton instance of the AppDatabase.
     * 获取 AppDatabase 的单例实例。
     *
     * @param context The application context. / 应用程序上下文。
     * @return The singleton AppDatabase instance. / AppDatabase 单例实例。
     */
    public static AppDatabase getDatabase(final Context context) {
        if (INSTANCE == null) {
            synchronized (AppDatabase.class) {
                if (INSTANCE == null) {
                    INSTANCE = Room.databaseBuilder(context.getApplicationContext(),
                                    AppDatabase.class, "llm_app_database")
                            // Add migrations here if you change the schema later
                            // 如果以后更改架构，请在此处添加迁移
                            // .addMigrations(MIGRATION_1_2)
                            .build();
                }
            }
        }
        return INSTANCE;
    }
}
