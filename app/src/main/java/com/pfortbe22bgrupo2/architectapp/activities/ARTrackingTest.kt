package com.pfortbe22bgrupo2.architectapp.activities
//https://github.com/SceneView/sceneview-android/blob/main/samples/ar-model-viewer/src/main/java/io/github/sceneview/sample/armodelviewer/MainActivity.kt
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.databinding.ActivityArtrackingTestBinding
import com.pfortbe22bgrupo2.architectapp.utilities.ARTracking

class ARTrackingTest: AppCompatActivity() {

    lateinit var binding: ActivityArtrackingTestBinding
    lateinit var arTracking: ARTracking

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtrackingTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressIndicator.isVisible = false

        arTracking = ARTracking(5, binding.sceneView, binding.progressIndicator)
        arTracking.setup(
            arTracking::pointScanning,
            arTracking::onConfirmedPoint
        )
    }

}