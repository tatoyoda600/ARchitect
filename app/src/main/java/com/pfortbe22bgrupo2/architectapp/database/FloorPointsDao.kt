package com.pfortbe22bgrupo2.architectapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pfortbe22bgrupo2.architectapp.entities.FloorPointsEntity

@Dao
interface FloorPointsDao {
    @Query("SELECT * FROM floor_points WHERE floor_id = :id")
    fun getFloorPointsByID(id: Int): List<FloorPointsEntity>

    @Query("SELECT MAX(count) FROM floor_points WHERE floor_id = :id")
    fun getFloorPointCountByID(id: Int): Int?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertFloorPoint(floorPoint: FloorPointsEntity)
}