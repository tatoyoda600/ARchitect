package com.pfortbe22bgrupo2.architectapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pfortbe22bgrupo2.architectapp.entities.FloorEntity

@Dao
interface FloorDao {
    @Query("SELECT id FROM floors")
    fun getFloorIDs(): List<Int>

    @Query("SELECT * FROM floors WHERE id = :id")
    fun getFloorByID(id: Int): FloorEntity?

    @Query("SELECT * FROM floors WHERE id IN (SELECT floor_id FROM floor_points)")
    fun getAllFloors(): List<FloorEntity>

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertFloor(floor: FloorEntity): Long
}