package com.lesurfaces.web.data.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lesurfaces.web.data.db.dao.ActionDao
import com.lesurfaces.web.data.db.dao.LocationDao
import com.lesurfaces.web.data.db.dao.OperationDao
import com.lesurfaces.web.data.db.entities.ActionEntity
import com.lesurfaces.web.data.db.entities.LocationEntity
import com.lesurfaces.web.data.db.entities.OperationEntity
import com.lesurfaces.web.data.db.entities.SlabEntity

@Database(
    entities = [
        ActionEntity::class,
        LocationEntity::class,
        OperationEntity::class,
        SlabEntity::class
    ],
    version = 1
)
abstract class OperationDB : RoomDatabase() {
    abstract fun actionDao(): ActionDao
    abstract fun locationDao(): LocationDao
    abstract fun operationDao(): OperationDao
}