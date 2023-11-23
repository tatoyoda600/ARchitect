package com.pfortbe22bgrupo2.architectapp.database

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.pfortbe22bgrupo2.architectapp.entities.DesignEntity

@Dao
interface DesignDao {
    @Query("SELECT id FROM designs")
    fun getDesignIDs(): List<Int>

    @Query("SELECT * FROM designs WHERE id = :id")
    fun getDesignByID(id: Int): DesignEntity?

    @Insert(onConflict = OnConflictStrategy.ABORT)
    fun insertDesign(design: DesignEntity): Long
}