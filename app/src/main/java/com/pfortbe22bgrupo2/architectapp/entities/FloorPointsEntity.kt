package com.pfortbe22bgrupo2.architectapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "floor_points", primaryKeys = ["floor_id", "count"])
class FloorPointsEntity(
    floor_id: Int,
    count: Int,
    x_pos: Float,
    z_pos: Float
) {
    @ColumnInfo(name = "floor_id")
    val floor_id: Int

    @ColumnInfo(name = "count")
    val count: Int

    @ColumnInfo(name = "x_pos")
    val x_pos: Float

    @ColumnInfo(name = "z_pos")
    val z_pos: Float

    init {
        this.floor_id = floor_id
        this.count = count
        this.x_pos = x_pos
        this.z_pos = z_pos
    }
}
