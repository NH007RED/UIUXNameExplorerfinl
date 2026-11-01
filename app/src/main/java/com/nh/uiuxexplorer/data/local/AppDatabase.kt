package com.nh.uiuxexplorer.data.local

import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(
    entities = [
        FavoriteEntity::class,
        RecentViewEntity::class,
        SearchHistoryEntity::class,
        AiResponseEntity::class,
        SavedScreenEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoriteDao(): FavoriteDao
    abstract fun recentViewDao(): RecentViewDao
    abstract fun searchHistoryDao(): SearchHistoryDao
    abstract fun aiResponseDao(): AiResponseDao
    abstract fun savedScreenDao(): SavedScreenDao

    companion object {
        /** Adds the saved_screens table; keeps favorites and history from the earlier build. */
        val MIGRATION_1_2 = object : Migration(1, 2) {
            override fun migrate(db: SupportSQLiteDatabase) {
                db.execSQL(
                    "CREATE TABLE IF NOT EXISTS `saved_screens` (" +
                        "`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
                        "`name` TEXT NOT NULL, `styleId` TEXT NOT NULL, `elementsJson` TEXT NOT NULL, " +
                        "`createdAt` INTEGER NOT NULL, `updatedAt` INTEGER NOT NULL)"
                )
            }
        }
    }
}
