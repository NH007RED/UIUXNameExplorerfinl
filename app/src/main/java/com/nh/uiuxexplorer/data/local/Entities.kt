package com.nh.uiuxexplorer.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteEntity(@PrimaryKey val designId: String, val addedAt: Long)

@Entity(tableName = "recent_views")
data class RecentViewEntity(@PrimaryKey val designId: String, val viewedAt: Long)

@Entity(tableName = "search_history")
data class SearchHistoryEntity(@PrimaryKey val query: String, val searchedAt: Long)

@Entity(tableName = "ai_responses")
data class AiResponseEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val designId: String?,
    val prompt: String,
    val response: String,
    val model: String,
    val createdAt: Long
)
