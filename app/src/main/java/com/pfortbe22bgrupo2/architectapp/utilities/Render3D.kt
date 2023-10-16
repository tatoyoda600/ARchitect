package com.pfortbe22bgrupo2.architectapp.utilities

import io.github.sceneview.utils.Color
import com.google.ar.core.Config
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.material.setBaseColor
import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node


class Render3D(
    sceneView: ArSceneView
) {
    private var sceneView: ArSceneView

    init {
        this.sceneView = sceneView
    }

    fun render(
        modelPath: String,
        position: Position,
        rotation: Rotation
    ): Node {
        var modelNode: ModelNode = ModelNode(sceneView.engine).apply {
            this.position = position
            this.rotation = rotation
            this.scale = Scale(1f)
            this.loadModelGlbAsync(
                glbFileLocation = modelPath,
                scaleToUnits = null,
                centerOrigin = Position(-0.5f)
            ) {}
        }

        return sceneView.addChild(modelNode)
    }

    fun arRender(
        modelPath: String
    ): ArModelNode {
        lateinit var modelNode: ArModelNode

        sceneView.apply {
            this.lightEstimationMode = Config.LightEstimationMode.DISABLED
        }

        modelNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = modelPath,
                scaleToUnits = 1f,
                centerOrigin = Position(-0.5f)
            )
            {
                sceneView.planeRenderer.isVisible = true
                val materialInstance = it.materialInstances[0]
            }
        }

        sceneView.addChild(modelNode)

        return modelNode
    }
}