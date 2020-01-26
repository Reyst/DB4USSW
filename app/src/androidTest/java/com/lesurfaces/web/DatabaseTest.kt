package com.lesurfaces.web

import android.content.Context
import android.util.Log
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.Observer
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import com.lesurfaces.web.data.db.OperationDB
import com.lesurfaces.web.data.db.dao.OperationDao
import com.lesurfaces.web.data.db.entities.*
import com.lesurfaces.web.data.db.results.OperationData
import com.lesurfaces.web.data.db.results.Slab
import kotlinx.coroutines.runBlocking
import org.junit.*
import org.junit.rules.TestRule
import java.io.IOException

class DatabaseTest {
    private lateinit var database: OperationDB

    @get:Rule
    val rule: TestRule = InstantTaskExecutorRule()


    @Before
    fun createDB() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, OperationDB::class.java).build()

        runBlocking { fillDB() }
    }

    @After
    @Throws(IOException::class)
    fun closeDb() {
        database.clearAllTables()
        database.close()
    }

    private val checkObserver = Observer<List<OperationData>> {
        Log.wtf("INSPECT", it.toString())
    }

    private suspend fun fillDB() {
        database.actionDao().also { dao ->
            dao.insert(ActionEntity("A001", "Polish line", true))
            dao.insert(ActionEntity("A002", "Action IN"))
            dao.insert(ActionEntity("A003", "Action OUT"))
        }

        database.locationDao().also { dao ->
            dao.insert(LocationEntity("L000001", "Location 1"))
            dao.insert(LocationEntity("L000002", "Location 2"))
        }
    }

    private fun addOperationWithThreeSlabs(dao: OperationDao): Long {
        val operId = dao.insertOperation(OperationEntity())
        dao.insertSlab(SlabEntity(operId, "slab1", dao.getLastOperationSlabIndex(operId) + 1))
        dao.insertSlab(SlabEntity(operId, "slab2", dao.getLastOperationSlabIndex(operId) + 1))
        dao.insertSlab(SlabEntity(operId, "slab2", dao.getLastOperationSlabIndex(operId) + 1))
        dao.insertSlab(SlabEntity(operId, "slab3", dao.getLastOperationSlabIndex(operId) + 1))
        return operId
    }

    @Test
    fun createCurrentOperationWithThreeSlabs() {

        val dao = database.operationDao()

        val operLiveData = dao.getOnScreenOperationData()

        operLiveData.observeForever(checkObserver)

        val operId = addOperationWithThreeSlabs(dao)

        val operList = operLiveData.value

        Assert.assertNotNull(operList)
        Assert.assertEquals(1, operList?.size)

        val operationData = operList?.firstOrNull()
        Assert.assertNotNull(operationData)

        Assert.assertEquals("", operationData?.actionId)
        Assert.assertEquals(operationData?.actionId, operationData?.actionName)
        Assert.assertEquals("", operationData?.locationId)
        Assert.assertEquals(operationData?.locationId, operationData?.locationName)
        Assert.assertEquals("", operationData?.date)
        Assert.assertEquals(operId, operationData?.id)
        Assert.assertEquals(0, operationData?.state)
        Assert.assertEquals(3, operationData?.slabs?.size)

        operationData?.slabs?.containsAll(
            listOf(
                Slab(1, "slab1", ""),
                Slab(2, "slab2", ""),
                Slab(3, "slab3", "")
            )
        )

        operLiveData.removeObserver(checkObserver)
    }

    @Test
    fun shouldShiftOperations() {
        val dao = database.operationDao()

        val operLiveData = dao.getOnScreenOperationData()

        operLiveData.observeForever(checkObserver)

        val operId1 = addOperationWithThreeSlabs(dao)
        val oper1 = operLiveData.value?.first()

        Assert.assertEquals(operId1, oper1?.id)
        dao.shiftOperationState()

        val operId2 = addOperationWithThreeSlabs(dao)
        val oper2 = operLiveData.value?.first()
        Assert.assertEquals(operId2, oper2?.id)

        dao.shiftOperationState()

        val operList = operLiveData.value

        Assert.assertEquals(oper2?.id, operList?.first()?.id)
        Assert.assertEquals(PREVIOUS_STATE, operList?.first()?.state)
        Assert.assertEquals(oper1?.id, operList?.last()?.id)
        Assert.assertEquals(PAST_STATE, operList?.last()?.state)


        operLiveData.removeObserver(checkObserver)

    }

    @Test
    fun shouldChangeActionAndLocation() {

        val dao = database.operationDao()

        val operLiveData = dao.getOnScreenOperationData()

        operLiveData.observeForever(checkObserver)

        val operId1 = addOperationWithThreeSlabs(dao)
        dao.getOperationById(operId1)
            .firstOrNull()
            ?.copy(actionId = "A001", locationId = "1", scannedId = "A0011", date = "2020-01-01T12:00:01")
            ?.let { dao.updateOperation(it) }

        dao.shiftOperationState()

        val operId2 = addOperationWithThreeSlabs(dao)
        dao.getOperationById(operId2)
            .firstOrNull()
            ?.copy(actionId = "A002", locationId = "L000001", scannedId = "A002L000001", date = "2020-01-01T12:00:02")
            ?.let { dao.updateOperation(it) }

        dao.shiftOperationState()

        operLiveData.value?.let { list ->

            val oper2 = list.first()
            Assert.assertEquals("Location 1", oper2.locationName)
            Assert.assertEquals("Action IN", oper2.actionName)

            val oper1 = list.last()
            Assert.assertEquals("1", oper1.locationName)
            Assert.assertEquals("Polish line", oper1.actionName)

        } ?: Assert.fail("No data")

        operLiveData.removeObserver(checkObserver)
    }

    @Test
    fun shouldGetOperationDataById() {

        val dao = database.operationDao()
        val operId = addOperationWithThreeSlabs(dao)

        dao.getOperationDataById(operId)
            ?.let { Assert.assertEquals(operId, it.id) }

        val nullValue = dao.getOperationDataById(-1L)
        Assert.assertNull(nullValue)
    }


    /*
            @Test
            @Throws(Exception::class)
            fun testGetNotSyncedSlabs() {
                updateDatabase()

                val operationItems =
                    runBlocking { database.operationItemDao().getNotSyncedOperationItems() }

                assertTrue(operationItems.isNotEmpty())
            }

            @Test
            @Throws(Exception::class)
            fun testGetOperations() {
                val operationDao = database.operationDao()
                val liveData = operationDao.getOperations()

                val items = updateDatabase()

                val checkObserver = Observer<List<OperationWithItems>> {
                    assertEquals(it.size, items.size)
                }

                liveData.observeForever(checkObserver)
                liveData.removeObserver(checkObserver)
            }

            private fun updateDatabase(): List<OperationItem> {
                val operationDao = database.operationDao()

                val action = Action("action_id", "action")
                val actionDao = database.actionDao()
                runBlocking { actionDao.insert(action) }

                val location = Location("location_id", "location")
                val locationDao = database.locationDao()
                runBlocking { locationDao.insert(location) }

                val operation = Operation(
                    actionId = action.id,
                    locationId = location.id,
                    scannedId = "akgjrkjhek"
                )
                runBlocking { operationDao.insert(operation) }

                val currentOperation = runBlocking { operationDao.getOperationByState(0).firstOrNull() }

                val items = (0..5).map {
                    OperationItem("S12421400$it", currentOperation?.id ?: 0, "A011")
                }
                val operationItemDao = database.operationItemDao()
                runBlocking { items.forEach { operationItemDao.insert(it) } }

                return items
            }
        */
}
