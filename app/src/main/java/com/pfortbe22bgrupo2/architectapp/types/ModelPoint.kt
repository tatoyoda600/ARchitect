package com.pfortbe22bgrupo2.architectapp.types

import io.github.sceneview.math.Position
import io.github.sceneview.node.Node

class ModelPoint(
    var model: Node,
    id: Int,
    position: Position
): Point(id, position)