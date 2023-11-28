package com.pfortbe22bgrupo2.architectapp.types

import io.github.sceneview.math.Position

data class DesignSessionProduct(
    var count: Int,
    val category: String,
    val name: String,
    var position: Position,
    var rotation: Float,
    val scale: Float,
    val allowWalls: Boolean
)
