package com.pfortbe22bgrupo2.architectapp.activities
/*
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import com.google.ar.core.Config
import io.github.sceneview.ar.ArSceneView
*/

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.ar.core.Config
import com.pfortbe22bgrupo2.architectapp.R
import io.github.sceneview.ar.ArSceneView

class ARTrackingTest : AppCompatActivity(R.layout.activity_artracking_test) {
    lateinit var sceneView: ArSceneView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_artracking_test)

        sceneView = findViewById<ArSceneView?>(R.id.sceneView).apply {
            lightEstimationMode = Config.LightEstimationMode.ENVIRONMENTAL_HDR
            depthEnabled = true
            instantPlacementEnabled = true
            onArTrackingFailureChanged = { reason ->
                Log.w("TAG", reason.toString())
            }
        }
    }
}