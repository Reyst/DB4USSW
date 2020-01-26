package com.lesurfaces.web.data.db.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.lesurfaces.web.data.db.entities.CURRENT_STATE
import com.lesurfaces.web.data.db.entities.OperationEntity
import com.lesurfaces.web.data.db.entities.PAST_STATE
import com.lesurfaces.web.data.db.entities.SlabEntity
import com.lesurfaces.web.data.db.results.OperationData
import com.lesurfaces.web.data.db.results.Slab

@Dao
abstract class OperationDao {

    companion object {

        private const val SELECT = """
            select 
                o.id as id,
                o.scannedId as scannedCode,
                o.creation_date as date,
                o.state as state,
                o.actionId as actionId,
                case 
                    when a.name is null then o.actionId
                    else a.name
                end as actionName,
                case 
                    when a.isNeedNetwork is null then 0
                    else a.isNeedNetwork
                end as isNeedNetwork,
                o.locationId as locationId,
                case 
                    when l.name is null then o.locationId
                    else l.name
                end as locationName,
                o.isSynced as isSynced
            
        """

        private const val FROM = """
            from operations o 
                left join actions a on o.actionId = a.id
                left join locations l on o.locationId = l.id
        """

        private const val VISIBLE_WHERE = """
            where (o.state < 3) or (o.state > 2 and o.isSynced = 0)
        """

        private const val INVISIBLE_WHERE = """
            where o.state > 0 and o.isSynced = 0
        """

        private const val VISIBLE_ORDER = """
            order by o.state asc, o.id desc            
        """
    }


    @Transaction
    @Query(SELECT + "\n" + FROM + "\n" + VISIBLE_WHERE + "\n" + VISIBLE_ORDER)
    abstract fun getOnScreenOperationData(): LiveData<List<OperationData>>


    @Transaction
    @Query(SELECT + "\n" + FROM + "\n" + INVISIBLE_WHERE)
    abstract fun getNotSynchronizedOperationData(): List<OperationData>

    @Transaction
    @Query("$SELECT\n$FROM\nwhere o.id = :operationId")
    abstract fun getOperationDataById(operationId: Long): OperationData?


    @Query("""
        select 
            oi.number as number,
            oi.code as code,
            oi.name as name
        from operation_items oi 
        where oi.operation_id = :operationId 
    """)
    abstract fun getOperationSlabs(operationId: Long) : List<Slab>

    @Query("""
        select max(number)
        from operation_items
        where operation_id = :operationId
    """)
    abstract fun getLastOperationSlabIndex(operationId: Long): Int

    @Query("""
        select *
        from operations
        where id = :operationId
    """)
    abstract fun getOperationById(operationId: Long): List<OperationEntity>

    @Query("""
        select *
        from operations
        where state = :state
    """)
    abstract fun getOperationsByState(state: Int): List<OperationEntity>



    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateOperation(operation: OperationEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateOperations(operations: Collection<OperationEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    abstract fun insertOperation(operation: OperationEntity): Long

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateSlab(slab: SlabEntity)

    @Update(onConflict = OnConflictStrategy.REPLACE)
    abstract fun updateSlabs(slabs: Collection<SlabEntity>)

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    abstract fun insertSlab(slab: SlabEntity)

    @Transaction
    open fun shiftOperationState() {
        for (state in PAST_STATE downTo CURRENT_STATE) {
            getOperationsByState(state)
                .map { it.copy(state = (it.state + 1)) }
                .let { updateOperations(it) }
        }
    }

}