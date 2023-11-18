package com.pfortbe22bgrupo2.architectapp.utilities

import android.util.Log
import androidx.core.view.isVisible
import com.google.android.material.progressindicator.CircularProgressIndicator

import com.google.ar.core.Config
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode
import io.github.sceneview.ar.node.PlacementMode

import io.github.sceneview.math.Position
import io.github.sceneview.math.Rotation
import io.github.sceneview.math.Scale
import io.github.sceneview.node.ModelNode
import io.github.sceneview.node.Node


class Render3D(
    sceneView: ArSceneView,
    progressBar: CircularProgressIndicator
) {
    private var sceneView: ArSceneView
    private var progressBar: CircularProgressIndicator
    private val storage = FirebaseStorage.getInstance()

    init {
        this.sceneView = sceneView
        this.progressBar = progressBar
    }

    fun render(
        modelPath: String,
        position: Position,
        rotation: Rotation,
        scale: Scale = Scale(1f)
    ): Node {
        // Log.d("FunctionNames", "render")
        val modelNode: ModelNode = ModelNode(sceneView.engine).apply {
            // Log.d("FunctionNames", "renderPosition")
            this.position = position
            // Log.d("FunctionNames", "renderRotation")
            this.rotation = rotation
            // Log.d("FunctionNames", "renderScale")
            this.scale = scale
            // Log.d("FunctionNames", "renderLoadGLB")
            this.loadModelGlbAsync(
                glbFileLocation = modelPath,
                scaleToUnits = null,
                centerOrigin = Position(-0.5f)
            ) {}
            // Log.d("FunctionNames", "renderEnd")
        }

        return sceneView.addChild(modelNode)
    }

    fun createEmptyNode(position: Position, rotation: Rotation): Node {
        val modelNode: ModelNode = ModelNode(sceneView.engine).apply {
            this.position = position
            this.rotation = rotation
            this.scale = Scale(1f)
        }

        return sceneView.addChild(modelNode)
    }

    fun renderFromFirebase(
        modelPath: String,
        position: Position,
        rotation: Rotation,
        scale: Scale,
        onSuccess: (Node) -> Unit,
        onFailure: () -> Unit
    ) {
        storage.reference.child("/models/${modelPath}").downloadUrl
            .addOnSuccessListener {
                val modelNode = render(it.toString(), position, rotation, scale)
                onSuccess(modelNode)
            }.addOnFailureListener {
                onFailure()
            }
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
                progressBar.isVisible = false
                val materialInstance = it.materialInstances[0]
            }
        }

        sceneView.addChild(modelNode)

        return modelNode
    }
}