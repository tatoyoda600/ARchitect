package com.pfortbe22bgrupo2.architectapp.activities
//https://github.com/SceneView/sceneview-android/blob/main/samples/ar-model-viewer/src/main/java/io/github/sceneview/sample/armodelviewer/MainActivity.kt
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.databinding.ActivityArtrackingTestBinding
import com.pfortbe22bgrupo2.architectapp.utilities.DICT_COORD_ZOOM
import com.pfortbe22bgrupo2.architectapp.utilities.DatabaseHandler
import com.pfortbe22bgrupo2.architectapp.utilities.DefaultARTracking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch

class ARTrackingTest: AppCompatActivity() {
    val scope = CoroutineScope(Job() + Dispatchers.Main)
    lateinit var binding: ActivityArtrackingTestBinding
    lateinit var arTracking: DefaultARTracking

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtrackingTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.progressIndicator.isVisible = true

        arTracking = DefaultARTracking(5, binding.sceneView, binding.progressIndicator,
            onFloorDetectedFunction = fun(){
                binding.confirmBtn.isEnabled = true
                binding.loadBtn.isEnabled = true
                binding.progressIndicator.isVisible = false
            }
        )

        binding.rescanBtn.setOnClickListener {
            binding.progressIndicator.isVisible = true
            arTracking.reset()
        }

        binding.confirmBtn.setOnClickListener {
            arTracking.confirm()
        }

        binding.saveBtn.setOnClickListener {
            arTracking.saveFloor(binding.root.context)
        }

        binding.loadBtn.setOnClickListener {
            CoroutineScope(Dispatchers.IO).launch {
                val database = DatabaseHandler(binding.root.context)
                val ids = database.getFloorIDs()
                if (ids.size > 0) {
                    arTracking.loadFloor(binding.root.context, ids.first())
                }
            }
        }
    }
}