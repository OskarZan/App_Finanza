package com.example.financeszan.data

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.example.financeszan.data.dao.TransactionDao
import com.example.financeszan.data.entities.Transaction

@Database(entities = [Transaction::class], version = 1)
@TypeConverters(Converters::class)
abstract class FinancesDatabase : RoomDatabase() {
    abstract fun transactionDao(): TransactionDao

    companion object {
        @Volatile
        private var INSTANCE: FinancesDatabase? = null

        fun getDatabase(context: Context): FinancesDatabase {
            return INSTANCE ?: synchronized(this) {
                val instance = Room.databaseBuilder(
                    context.applicationContext,
                    FinancesDatabase::class.java,
                    "finances_database"
                ).build()
                INSTANCE = instance
                instance
            }
        }
    }
} 