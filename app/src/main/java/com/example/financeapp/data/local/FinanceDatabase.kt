package com.example.financeapp.data.local

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [TransactionEntity::class, CategoryEntity::class], version = 1)
abstract class FinanceDatabase : RoomDatabase() {
    abstract fun dao(): FinanceDao
}
