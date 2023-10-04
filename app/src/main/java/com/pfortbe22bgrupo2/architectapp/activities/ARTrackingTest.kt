package com.pfortbe22bgrupo2.architectapp.activities
//https://github.com/SceneView/sceneview-android/blob/main/samples/ar-model-viewer/src/main/java/io/github/sceneview/sample/armodelviewer/MainActivity.kt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pfortbe22bgrupo2.architectapp.databinding.ActivityArtrackingTestBinding
import com.pfortbe22bgrupo2.architectapp.utilities.ARTracking

class ARTrackingTest: AppCompatActivity() {

    lateinit var binding: ActivityArtrackingTestBinding
    lateinit var arTracking: ARTracking

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtrackingTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        arTracking = ARTracking(5)
        arTracking.setup(
            binding.sceneView,
            arTracking::pointScanning,
            arTracking::onConfirmedPoint
        )
    }
}