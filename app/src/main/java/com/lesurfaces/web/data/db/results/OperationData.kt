package com.lesurfaces.web.data.db.results

import androidx.room.Relation
import com.lesurfaces.web.data.db.entities.SlabEntity

data class OperationData(
    val id: Long,
    val scannedCode: String,
    val date: String,
    val state: Int,
    val actionId: String,
    val actionName: String,
    val isNeedNetwork: Boolean,
    val locationId: String,
    val locationName: String,
    val isSynced: Boolean,

    @Relation(
        parentColumn = "id",
        entityColumn = "operation_id",
        entity = SlabEntity::class
    )
    val slabs: MutableList<Slab>
)