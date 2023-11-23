package com.pfortbe22bgrupo2.architectapp.database

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Query
import androidx.room.Upsert
import com.pfortbe22bgrupo2.architectapp.entities.DesignProductsEntity
import com.pfortbe22bgrupo2.architectapp.entities.FloorPointsEntity

@Dao
interface DesignProductsDao {
    @Query("SELECT * FROM design_products WHERE design_id = :id")
    fun getDesignProductsByID(id: Int): List<DesignProductsEntity>

    @Query("SELECT MAX(count) FROM design_products WHERE design_id = :id")
    fun getDesignProductsCountByID(id: Int): Int?

    @Upsert
    fun insertDesignProduct(designProduct: DesignProductsEntity)

    @Delete
    fun removeDesignProduct(designProduct: DesignProductsEntity)
}