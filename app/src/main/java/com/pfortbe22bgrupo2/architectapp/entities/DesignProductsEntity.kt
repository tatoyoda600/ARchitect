package com.pfortbe22bgrupo2.architectapp.entities

import androidx.room.ColumnInfo
import androidx.room.Entity
import com.pfortbe22bgrupo2.architectapp.types.DesignSessionProduct

@Entity(tableName = "design_products", primaryKeys = ["design_id", "count"])
class DesignProductsEntity(
    design_id: Int,
    count: Int,
    model_category: String,
    model_name: String,
    x_pos: Float,
    z_pos: Float,
    rotation: Float,
    scale: Float,
    allow_walls: Boolean
) {
    @ColumnInfo(name = "design_id")
    val design_id: Int

    @ColumnInfo(name = "count")
    val count: Int

    @ColumnInfo(name = "model_category")
    val model_category: String

    @ColumnInfo(name = "model_name")
    val model_name: String

    @ColumnInfo(name = "x_pos")
    val x_pos: Float

    @ColumnInfo(name = "z_pos")
    val z_pos: Float

    @ColumnInfo(name = "rotation")
    val rotation: Float

    @ColumnInfo(name = "scale")
    val scale: Float

    @ColumnInfo(name = "allow_walls")
    val allow_walls: Boolean

    init {
        this.design_id = design_id
        this.count = count
        this.model_category = model_category
        this.model_name = model_name
        this.x_pos = x_pos
        this.z_pos = z_pos
        this.rotation = rotation
        this.scale = scale
        this.allow_walls = allow_walls
    }

    constructor(design_id: Int, product: DesignSessionProduct): this(
        design_id,
        product.count,
        product.category,
        product.name,
        product.position.x,
        product.position.z,
        product.rotation,
        product.scale,
        product.allowWalls
    )
}