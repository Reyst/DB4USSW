package com.lesurfaces.web.data.db.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "actions")
data class ActionEntity(
    @PrimaryKey val id: String,
    val name: String,
    val isNeedNetwork: Boolean = false,
    val isSyncAfterComplete: Boolean = false
)