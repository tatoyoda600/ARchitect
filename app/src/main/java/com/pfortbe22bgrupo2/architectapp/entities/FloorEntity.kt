package com.pfortbe22bgrupo2.architectapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import io.github.sceneview.math.Rotation

@Entity(tableName = "floors")
class FloorEntity(
    id: Int,
    rotation: Float
) {
    @PrimaryKey(autoGenerate = true)
    val id: Int

    @ColumnInfo(name = "rotation")
    val rotation: Float

    init {
        this.id = id
        this.rotation = rotation
    }
}
