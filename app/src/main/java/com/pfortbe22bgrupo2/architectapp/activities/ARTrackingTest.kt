package com.pfortbe22bgrupo2.architectapp.activities
//https://github.com/SceneView/sceneview-android/blob/main/samples/ar-model-viewer/src/main/java/io/github/sceneview/sample/armodelviewer/MainActivity.kt

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.pfortbe22bgrupo2.architectapp.databinding.ActivityArtrackingTestBinding
import com.pfortbe22bgrupo2.architectapp.utilities.ARTracking
import com.pfortbe22bgrupo2.architectapp.utilities.Floor
import com.pfortbe22bgrupo2.architectapp.utilities.Outline
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ARTrackingTest: AppCompatActivity() {
    val scope = CoroutineScope(Job() + Dispatchers.Main)
    lateinit var binding: ActivityArtrackingTestBinding
    lateinit var arTracking: ARTracking

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtrackingTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        arTracking = ARTracking(5, binding.sceneView)
        arTracking.setup(
            arTracking::pointScanning,
            arTracking::onConfirmedPoint,
            fun(){
                binding.confirmBtn.isEnabled = true
            }
        )

        binding.rescanBtn.setOnClickListener {
            arTracking.reset()
        }

        binding.confirmBtn.setOnClickListener {
            scope.launch {
                // TODO("FIGURE OUT HOW TO GET THIS TO TRULY RUN ASYNCHRONOUSLY")
                val outlines: Outline = arTracking.getFloorOutline()

                if (outlines.points.size == 1) {
                    // Completed outline
                    for (point in outlines.points[0]) {
                        arTracking.colorPoint(point)
                    }
                    val floor: Floor = arTracking.calculateFloorGrid(outlines)
                }
                else {
                    // Incomplete outline
                    for (outline in outlines.points) {
                        for (point in outline) {
                            arTracking.colorPoint(point)
                        }
                    }
                }
            }
        }
    }
}