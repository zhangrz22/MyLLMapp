package com.example.myllmapp.db;

import androidx.room.TypeConverter;
import com.example.myllmapp.model.Sender;

/**
 * Type converters for Room database to handle custom types like Enums.
 * 用于 Room 数据库处理自定义类型（如枚举）的类型转换器。
 */
public class Converters {

    /**
     * Converts a Sender enum to its String representation for database storage.
     * 将 Sender 枚举转换为其字符串表示形式以便存入数据库。
     * @param sender The Sender enum value. / Sender 枚举值。
     * @return String representation (e.g., "USER", "LLM"). / 字符串表示形式（例如 "USER", "LLM"）。
     */
    @TypeConverter
    public static String fromSender(Sender sender) {
        return sender == null ? null : sender.name();
    }

    /**
     * Converts a String back to a Sender enum when reading from the database.
     * 从数据库读取时，将字符串转换回 Sender 枚举。
     * @param name The String representation stored in the database. / 数据库中存储的字符串表示形式。
     * @return The corresponding Sender enum value. / 对应的 Sender 枚举值。
     */
    @TypeConverter
    public static Sender toSender(String name) {
        return name == null ? null : Sender.valueOf(name);
    }

    // Add other converters here if needed (e.g., for Date objects)
    // 如果需要，在此处添加其他转换器（例如，用于 Date 对象）
}
