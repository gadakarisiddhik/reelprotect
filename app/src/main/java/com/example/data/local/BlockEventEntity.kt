package com.example.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "block_events")
data class BlockEventEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val timestamp: Long = System.currentTimeMillis(),
    val appId: String,
    val appName: String,
    val packageName: String,
    val feedType: String,
    val actionTaken: String = "REDIRECT_BACK",
    val wasFocusSession: Boolean = false
)

data class AppBlockCount(
    val appId: String,
    val appName: String,
    val count: Int
)
