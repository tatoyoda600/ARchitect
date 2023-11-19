package com.pfortbe22bgrupo2.architectapp.activities
//https://github.com/SceneView/sceneview-android/blob/main/samples/ar-model-viewer/src/main/java/io/github/sceneview/sample/armodelviewer/MainActivity.kt

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.pfortbe22bgrupo2.architectapp.databinding.ActivityArtrackingTestBinding
import com.pfortbe22bgrupo2.architectapp.utilities.DefaultARTracking
import com.pfortbe22bgrupo2.architectapp.utilities.DatabaseHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ARTrackingTest: AppCompatActivity() {
    lateinit var binding: ActivityArtrackingTestBinding
    lateinit var arTracking: DefaultARTracking

    override fun onCreate(savedInstanceState: Bundle?) {
        // Log.d("FunctionNames", "onCreate")
        super.onCreate(savedInstanceState)
        binding = ActivityArtrackingTestBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val parameters: Bundle? = intent.extras
        parameters?.let {
            val models = it.getStringArray("models")
            models?.let {
                //TODO("Get the preview images and load them into a hotbar-type recycler")
                //TODO("Get the scales of the models (Or the real sizes and turn them into scales)")
                //TODO("Start downloading the models maybe?")
            }
        }

        arTracking = DefaultARTracking(5, binding.sceneView, binding.progressIndicator,
            onFloorDetectedFunction = fun(){
                // Log.d("FunctionNames", "onFloorDetectedFunction")
                binding.confirmBtn.isEnabled = true
                binding.loadBtn.isEnabled = true
            }
        )

        binding.rescanBtn.setOnClickListener {
            // Log.d("FunctionNames", "rescanBtn")
            binding.progressIndicator.isVisible = true
            arTracking.reset()
        }

        binding.confirmBtn.setOnClickListener {
            // Log.d("FunctionNames", "confirmBtn")
            arTracking.confirm { arTracking.paintFloor() }
        }

        binding.saveBtn.setOnClickListener {
            // Log.d("FunctionNames", "saveBtn")
            arTracking.saveFloor(binding.root.context)
        }

        binding.loadBtn.setOnClickListener {
            // Log.d("FunctionNames", "loadBtn")
            CoroutineScope(Dispatchers.IO).launch {
                val database = DatabaseHandler(binding.root.context)
                val ids = database.getFloorIDs()
                if (ids.size > 0) {
                    arTracking.loadFloor(binding.root.context, ids.first())
                }
            }
        }

        binding.sofaBtn.setOnClickListener {
            // Log.d("FunctionNames", "sofaBtn")
            arTracking.renderModel("chairs", "ADDE_Chair.glb", 1f)
        }
    }
}