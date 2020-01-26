package com.lesurfaces.web.data.db.dao

import androidx.room.*
import com.lesurfaces.web.data.db.entities.ActionEntity

@Dao
interface ActionDao {

    @Query("SELECT * FROM actions WHERE id = :id")
    suspend fun getById(id: String): List<ActionEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(action: ActionEntity)

    @Update
    suspend fun update(action: ActionEntity)

    @Delete
    suspend fun delete(action: ActionEntity)
}