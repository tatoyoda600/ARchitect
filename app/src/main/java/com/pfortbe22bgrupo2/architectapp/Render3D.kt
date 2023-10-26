package com.pfortbe22bgrupo2.architectapp


import androidx.core.view.isGone
import androidx.core.view.isVisible

import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.ar.core.Config
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode
import io.github.sceneview.math.Position


interface Render3D {
    fun render(
        sceneView: ArSceneView,
        placeButton: ExtendedFloatingActionButton,
        model: String,
        progressBar: CircularProgressIndicator
    ) {
        lateinit var modelNode: ArModelNode

        sceneView.apply {
            this.lightEstimationMode = Config.LightEstimationMode.DISABLED
        }

        placeButton.setOnClickListener {
            place(modelNode, sceneView)
        }

        modelNode = ArModelNode(sceneView.engine, PlacementMode.INSTANT).apply {
            loadModelGlbAsync(
                glbFileLocation = model,
                scaleToUnits = 1f,
                centerOrigin = Position(-0.5f)
            )
            {
                sceneView.planeRenderer.isVisible = true
                progressBar.isVisible = false
                val materialInstance = it.materialInstances[0]
            }
            onAnchorChanged = {
                placeButton.isGone = it != null
            }

        }

        sceneView.addChild(modelNode)
    }
    private fun place(modelNode: ArModelNode, sceneView: ArSceneView) {
        modelNode.anchor()

        sceneView.planeRenderer.isVisible = false
    }
}