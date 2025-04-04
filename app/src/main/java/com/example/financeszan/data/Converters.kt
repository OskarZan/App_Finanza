package com.example.financeszan.data

import androidx.room.TypeConverter
import com.example.financeszan.data.entities.TransactionCategory
import com.example.financeszan.data.entities.TransactionType
import java.util.Date

class Converters {
    @TypeConverter
    fun fromTimestamp(value: Long?): Date? {
        return value?.let { Date(it) }
    }

    @TypeConverter
    fun dateToTimestamp(date: Date?): Long? {
        return date?.time
    }

    @TypeConverter
    fun fromTransactionType(value: TransactionType): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionType(value: String): TransactionType {
        return TransactionType.valueOf(value)
    }

    @TypeConverter
    fun fromTransactionCategory(value: TransactionCategory): String {
        return value.name
    }

    @TypeConverter
    fun toTransactionCategory(value: String): TransactionCategory {
        return TransactionCategory.valueOf(value)
    }
} 