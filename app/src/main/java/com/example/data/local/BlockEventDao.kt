package com.example.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface BlockEventDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(event: BlockEventEntity): Long

    @Query("SELECT * FROM block_events ORDER BY timestamp DESC")
    fun getAllEvents(): Flow<List<BlockEventEntity>>

    @Query("SELECT * FROM block_events ORDER BY timestamp DESC LIMIT :limit")
    fun getRecentEvents(limit: Int): Flow<List<BlockEventEntity>>

    @Query("SELECT * FROM block_events WHERE timestamp >= :sinceTimestamp ORDER BY timestamp DESC")
    fun getEventsSince(sinceTimestamp: Long): Flow<List<BlockEventEntity>>

    @Query("SELECT COUNT(*) FROM block_events WHERE timestamp >= :sinceTimestamp")
    fun getEventCountSince(sinceTimestamp: Long): Flow<Int>

    @Query("SELECT COUNT(*) FROM block_events")
    fun getTotalEventCount(): Flow<Int>

    @Query("SELECT appId, appName, COUNT(*) as count FROM block_events GROUP BY appId ORDER BY count DESC")
    fun getBlockCountsByApp(): Flow<List<AppBlockCount>>

    @Query("SELECT MAX(timestamp) FROM block_events WHERE appId = :appId")
    suspend fun getLastDetectedTimestamp(appId: String): Long?

    @Query("DELETE FROM block_events")
    suspend fun clearAll()
}
