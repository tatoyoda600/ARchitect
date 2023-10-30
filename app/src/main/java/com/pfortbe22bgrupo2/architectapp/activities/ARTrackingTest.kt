package com.pfortbe22bgrupo2.architectapp.activities
//https://github.com/SceneView/sceneview-android/blob/main/samples/ar-model-viewer/src/main/java/io/github/sceneview/sample/armodelviewer/MainActivity.kt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.pfortbe22bgrupo2.architectapp.databinding.ActivityArtrackingTestBinding
import com.pfortbe22bgrupo2.architectapp.utilities.DefaultARTracking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job

class ARTrackingTest: AppCompatActivity() {
    val scope = CoroutineScope(Job() + Dispatchers.Main)
    lateinit var binding: ActivityArtrackingTestBinding
    lateinit var arTracking: DefaultARTracking

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtrackingTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        arTracking = DefaultARTracking(5, binding.sceneView, fun(){
            binding.confirmBtn.isEnabled = true
        })

        binding.rescanBtn.setOnClickListener {
            arTracking.reset()
        }

        binding.confirmBtn.setOnClickListener {
            arTracking.confirm()
        }
    }
}