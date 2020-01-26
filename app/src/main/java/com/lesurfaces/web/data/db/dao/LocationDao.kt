package com.lesurfaces.web.data.db.dao

import androidx.room.*
import com.lesurfaces.web.data.db.entities.LocationEntity

@Dao
interface LocationDao {
    @Query("SELECT * FROM locations WHERE id = :id")
    suspend fun getById(id: String): List<LocationEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(location: LocationEntity)

    @Update
    suspend fun update(location: LocationEntity)

    @Delete
    suspend fun delete(location: LocationEntity)
}