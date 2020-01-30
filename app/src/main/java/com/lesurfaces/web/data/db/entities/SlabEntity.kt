package com.lesurfaces.web.data.db.entities

import androidx.room.*
import com.lesurfaces.web.data.db.entities.OperationEntity

@Entity(
    tableName = "operation_items",
    foreignKeys = [ForeignKey(entity = OperationEntity::class, parentColumns = ["id"], childColumns = ["operation_id"])],
    primaryKeys = ["code", "operation_id"]
)
data class SlabEntity(

    @ColumnInfo(name = "operation_id", index = true)
    val operationId: Long,

    val code: String,

    val number: Int = 0,

    @ColumnInfo(defaultValue = "")
    val name: String = ""

)
