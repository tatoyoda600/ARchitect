package com.pfortbe22bgrupo2.architectapp.types

import io.github.sceneview.math.Position

data class DesignSession(
    val designId: Int,
    val floorId: Int,
    val name: String,
    val originalCameraPosition: Position,
    val originalCameraRotation: Float,
    val savedFloorIndexes: MutableMap<Int, MutableList<Int>>,
    val savedProducts: MutableList<DesignSessionProduct>,
    var maxCount: Int
)
