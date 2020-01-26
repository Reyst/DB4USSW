package com.lesurfaces.web.data.db.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

const val CURRENT_STATE = 0
const val PREVIOUS_STATE = 1
const val PAST_STATE = 2
const val OTHER = 3

@Entity(tableName = "operations")
data class OperationEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val scannedId: String = "",
    val actionId: String = "",
    val locationId: String = "",
    @ColumnInfo(name = "creation_date", typeAffinity = ColumnInfo.TEXT, defaultValue = "")
    val date: String = "",
    val isSynced: Boolean = false,
    val state: Int = CURRENT_STATE // 0 - New, 1 - previous, 2 - past, 3 - other
)
