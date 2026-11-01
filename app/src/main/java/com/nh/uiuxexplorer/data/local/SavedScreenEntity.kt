package com.nh.uiuxexplorer.data.local

import androidx.room.Dao
import androidx.room.Entity
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.PrimaryKey
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Entity(tableName = "saved_screens")
data class SavedScreenEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val styleId: String,
    val elementsJson: String,
    val createdAt: Long,
    val updatedAt: Long
)

@Dao
interface SavedScreenDao {
    @Query("SELECT * FROM saved_screens ORDER BY updatedAt DESC")
    fun observeAll(): Flow<List<SavedScreenEntity>>

    @Query("SELECT * FROM saved_screens WHERE id = :id")
    suspend fun get(id: Long): SavedScreenEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: SavedScreenEntity): Long

    @Query("DELETE FROM saved_screens WHERE id = :id")
    suspend fun delete(id: Long)
}
