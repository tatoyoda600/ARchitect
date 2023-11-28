package com.pfortbe22bgrupo2.architectapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "floors", indices = [Index(value = ["name"], unique = true)])
class FloorEntity(
    id: Int,
    rotation: Float,
    name: String
) {
    @PrimaryKey(autoGenerate = true)
    val id: Int

    @ColumnInfo(name = "rotation")
    val rotation: Float

    @ColumnInfo(name = "name")
    val name: String

    init {
        this.id = id
        this.rotation = rotation
        this.name = name
    }
}
