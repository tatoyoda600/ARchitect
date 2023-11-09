package com.pfortbe22bgrupo2.architectapp.types

import io.github.sceneview.math.Position

class PosDir(
    val position: Position,
    val prevDir: Int3,
    val nextDir: Int3
)