package com.pfortbe22bgrupo2.architectapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "designs", indices = [Index(value = ["name"], unique = true)])
class DesignEntity(
    id: Int,
    name: String,
    floor_id: Int,
    rotation: Float
) {
    @PrimaryKey(autoGenerate = true)
    val id: Int

    @ColumnInfo(name = "name")
    val name: String

    @ColumnInfo(name = "floor_id")
    val floor_id: Int

    @ColumnInfo(name = "rotation")
    val rotation: Float

    init {
        this.id = id
        this.name = name
        this.floor_id = floor_id
        this.rotation = rotation
    }
}
