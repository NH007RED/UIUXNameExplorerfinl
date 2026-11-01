package com.nh.uiuxexplorer.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoriteDao {
    @Query("SELECT designId FROM favorites ORDER BY addedAt DESC")
    fun observeIds(): Flow<List<String>>

    @Query("SELECT COUNT(*) FROM favorites WHERE designId = :id")
    fun observeCount(id: String): Flow<Int>

    @Query("SELECT COUNT(*) FROM favorites WHERE designId = :id")
    suspend fun count(id: String): Int

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: FavoriteEntity)

    @Query("DELETE FROM favorites WHERE designId = :id")
    suspend fun delete(id: String)

    @Query("DELETE FROM favorites")
    suspend fun clear()
}

@Dao
interface RecentViewDao {
    @Query("SELECT designId FROM recent_views ORDER BY viewedAt DESC LIMIT 20")
    fun observeIds(): Flow<List<String>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: RecentViewEntity)

    @Query("DELETE FROM recent_views WHERE designId NOT IN (SELECT designId FROM recent_views ORDER BY viewedAt DESC LIMIT 50)")
    suspend fun trim()

    @Query("DELETE FROM recent_views")
    suspend fun clear()
}

@Dao
interface SearchHistoryDao {
    @Query("SELECT * FROM search_history ORDER BY searchedAt DESC LIMIT 30")
    fun observe(): Flow<List<SearchHistoryEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SearchHistoryEntity)

    @Query("DELETE FROM search_history WHERE `query` = :query")
    suspend fun delete(query: String)

    @Query("DELETE FROM search_history")
    suspend fun clear()
}

@Dao
interface AiResponseDao {
    @Query("SELECT * FROM ai_responses ORDER BY createdAt DESC LIMIT 100")
    fun observeAll(): Flow<List<AiResponseEntity>>

    @Insert
    suspend fun insert(entity: AiResponseEntity)

    @Query("DELETE FROM ai_responses WHERE id = :id")
    suspend fun delete(id: Long)

    @Query("DELETE FROM ai_responses")
    suspend fun clear()
}
