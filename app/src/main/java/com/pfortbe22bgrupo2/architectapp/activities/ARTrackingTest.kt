package com.pfortbe22bgrupo2.architectapp.activities
//https://github.com/SceneView/sceneview-android/blob/main/samples/ar-model-viewer/src/main/java/io/github/sceneview/sample/armodelviewer/MainActivity.kt

import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.adapters.ProductHotbarAdapter
import com.pfortbe22bgrupo2.architectapp.data.HotBarSingleton
import com.pfortbe22bgrupo2.architectapp.databinding.ActivityArtrackingTestBinding
import com.pfortbe22bgrupo2.architectapp.entities.Product
import com.pfortbe22bgrupo2.architectapp.utilities.DefaultARTracking
import com.pfortbe22bgrupo2.architectapp.utilities.DatabaseHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ARTrackingTest: AppCompatActivity() {
    lateinit var binding: ActivityArtrackingTestBinding
    lateinit var arTracking: DefaultARTracking
    lateinit var database: DatabaseHandler

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

        val recycler = binding.productHotbar
        recycler.setHasFixedSize(true)
        recycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)

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
                val ids = database.getFloorIDs()
                if (ids.size > 0) {
                    arTracking.loadFloor(binding.root.context, ids.first())
                }
            }
        }

        binding.sofaBtn.setOnClickListener {
            // Log.d("FunctionNames", "sofaBtn")
            val modelCategory: String = "chairs"
            val modelName: String = "ADDE_Chair"

            database.getProductData(
                modelCategory,
                modelName,
                { product: Product ->
                    arTracking.renderModel(modelCategory, modelName, product.scale, product.allowWalls)
                },
                {
                    Log.e("ARTrackingTest", "Failed to get product data (${modelCategory} > ${modelName})")
                }
            )
        }

        CoroutineScope(Dispatchers.IO).launch {
            database = DatabaseHandler(binding.root.context)

            if (HotBarSingleton.hotBarItems.size > 0) {
                var productCount = 0
                val productList: MutableList<Product> = mutableListOf()

                val productProcessing = { p: Product? ->
                    p?.let { productList.add(it) }
                    productCount++

                    if (productCount == HotBarSingleton.hotBarItems.size) {
                        recycler.adapter = ProductHotbarAdapter(productList) { product ->
                            arTracking.renderModel(product.category, product.name, product.scale, product.allowWalls)
                        }
                        recycler.hasPendingAdapterUpdates()
                    }
                }

                for (product in HotBarSingleton.hotBarItems) {
                    database.getProductData(
                        product.first,
                        product.second,
                        productProcessing,
                        { productProcessing(null) }
                    )
                }

            }
            else {
                binding.productHotbar.isVisible = false
            }
        }
    }
}