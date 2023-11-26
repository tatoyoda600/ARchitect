package com.pfortbe22bgrupo2.architectapp.activities
//https://github.com/SceneView/sceneview-android/blob/main/samples/ar-model-viewer/src/main/java/io/github/sceneview/sample/armodelviewer/MainActivity.kt

import android.os.Bundle
import android.text.InputType
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.adapters.LoadMenuAdapter
import com.pfortbe22bgrupo2.architectapp.adapters.ProductHotbarAdapter
import com.pfortbe22bgrupo2.architectapp.data.HotBarSingleton
import com.pfortbe22bgrupo2.architectapp.databinding.ActivityArtrackingTestBinding
import com.pfortbe22bgrupo2.architectapp.databinding.LoadMenuBinding
import com.pfortbe22bgrupo2.architectapp.entities.Product
import com.pfortbe22bgrupo2.architectapp.utilities.DatabaseHandler
import com.pfortbe22bgrupo2.architectapp.utilities.DefaultARTracking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class ARTrackingTest: AppCompatActivity() {
    lateinit var binding: ActivityArtrackingTestBinding
    lateinit var arTracking: DefaultARTracking
    lateinit var database: DatabaseHandler
    lateinit var loadMenu: ViewGroup
    lateinit var loadMenuRecycler: RecyclerView
    val floorList: MutableMap<String, Int> = mutableMapOf()
    val designList: MutableMap<String, Int> = mutableMapOf()
    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        // Log.d("FunctionNames", "onCreate")
        super.onCreate(savedInstanceState)
        binding = ActivityArtrackingTestBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth

        val hotbar = mainScreenSetup()

        arTracking = DefaultARTracking(5, binding.sceneView, binding.progressIndicator,
            switchToDefaultLayout = fun() {
                // Log.d("FunctionNames", "switchToDefaultLayout")
                binding.defaultLayout.isVisible = true
                binding.placementLayout.isVisible = false
            },
            switchToPlacementLayout = fun() {
                // Log.d("FunctionNames", "switchToPlacementLayout")
                binding.defaultLayout.isVisible = false
                binding.placementLayout.isVisible = true
            },
            onFloorDetectedFunction = fun() {
                // Log.d("FunctionNames", "onFloorDetectedFunction")
                binding.rescanBtn.isEnabled = true
                binding.confirmBtn.isEnabled = true
                binding.saveBtn.isEnabled = true
                binding.loadBtn.isEnabled = true
                binding.postBtn.isEnabled = true
            }
        )

        loadMenuSetup()

        recyclerSetup(hotbar)
    }

    private fun mainScreenSetup(): RecyclerView {
        binding.rescanBtn.setOnClickListener(rescan)
        binding.confirmBtn.setOnClickListener(confirm)
        binding.saveBtn.setOnClickListener(saveFloor)
        binding.loadBtn.setOnClickListener(openLoadMenu)
        binding.cancelBtn.setOnClickListener(cancelPlace)
        binding.placeBtn.setOnClickListener(place)
        binding.backBtn.setOnClickListener { onBackPressedDispatcher.onBackPressed() }
        binding.postBtn.setOnClickListener(post)

        val recycler = binding.productHotbar
        recycler.setHasFixedSize(true)
        recycler.layoutManager = LinearLayoutManager(this, RecyclerView.HORIZONTAL, false)
        return recycler
    }

    private fun loadMenuSetup() {
        val loadMenuBinding = LoadMenuBinding.inflate(layoutInflater)
        val matchParent = ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT)
        binding.root.addView(loadMenuBinding.root, matchParent)
        loadMenu = loadMenuBinding.root
        loadMenu.isVisible = false

        loadMenuBinding.floorListBtn.setOnClickListener {
            it.isEnabled = false
            loadMenuBinding.designListBtn.isEnabled = true
            loadFloorList()
        }

        loadMenuBinding.designListBtn.setOnClickListener {
            it.isEnabled = false
            loadMenuBinding.floorListBtn.isEnabled = true
            loadDesignList()
        }

        loadMenuRecycler = loadMenuBinding.loadList
        loadMenuRecycler.setHasFixedSize(true)
        loadMenuRecycler.layoutManager = LinearLayoutManager(this, RecyclerView.VERTICAL, false)
    }

    private fun recyclerSetup(hotbar: RecyclerView) {
        CoroutineScope(Dispatchers.IO).launch {
            database = DatabaseHandler(binding.root.context)

            if (HotBarSingleton.hotBarItems.size > 0) {
                var productCount = 0
                val productList: MutableList<Product> = mutableListOf()

                val productProcessing = { p: Product? ->
                    p?.let { productList.add(it) }
                    productCount++

                    if (productCount == HotBarSingleton.hotBarItems.size) {
                        hotbar.adapter = ProductHotbarAdapter(productList) { product ->
                            arTracking.renderModel(product.category, product.name, product.scale, product.allowWalls)
                        }
                        hotbar.hasPendingAdapterUpdates()
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

            floorList.putAll(database.getAllFloors())
            loadFloorList()
            designList.putAll(database.getAllDesigns())
            database.getRemoteDesigns { name, id ->
                designList.putIfAbsent(name, id)
            }
        }
    }

    private val rescan: (View) -> Unit = {
        // Log.d("FunctionNames", "rescanBtn")
        binding.progressIndicator.isVisible = true
        arTracking.reset()
    }

    private val confirm: (View) -> Unit = {
        // Log.d("FunctionNames", "confirmBtn")
        arTracking.confirm { arTracking.paintFloor() }
    }

    private val saveFloor: (View) -> Unit = {
        // Log.d("FunctionNames", "saveBtn")
        val input = EditText(this)
        input.inputType = InputType.TYPE_CLASS_TEXT

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.save_floor_popup_title)
            .setView(input)
            .setPositiveButton(R.string.save_floor_popup_yes, null)
            .setNegativeButton(R.string.save_floor_popup_no) { dialog, which -> dialog.cancel() }
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val text = input.text.toString()
            if (text.isNotBlank()) {
                dialog.dismiss()
                arTracking.saveFloor(binding.root.context, text)
            }
        }
    }

    private val post: (View) -> Unit = {
        val inflater = layoutInflater
        val dialogLayout = inflater.inflate(R.layout.post_creating_dialogue, binding.root)

        val titleText = dialogLayout.findViewById<EditText>(R.id.title)
        val descriptionText = dialogLayout.findViewById<EditText>(R.id.description)

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.post_title)
            .setView(dialogLayout)
            .setPositiveButton(R.string.save_floor_popup_yes, null)
            .setNegativeButton(R.string.save_floor_popup_no) { dialog, which -> dialog.cancel() }
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val title = titleText.text.toString()
            val description = descriptionText.text.toString()
            val currentUser = auth.currentUser
            val userName = if(currentUser != null) currentUser.displayName!! else ""
            if (description.isNotBlank() && title.isNotBlank()) {
                arTracking.post(binding.root, title, description, userName)
                dialog.dismiss()
            }
        }

    }

    private val openLoadMenu: (View) -> Unit = {
        // Log.d("FunctionNames", "loadBtn")
        loadMenu.isVisible = true
        arTracking.setPaused(true)
    }

    private val place: (View) -> Unit = {
        // Log.d("FunctionNames", "placeBtn")
        arTracking.place()
    }

    private val cancelPlace: (View) -> Unit = {
        // Log.d("FunctionNames", "cancelPlaceBtn")
        arTracking.placementNode?.let {
            it.destroy()
            arTracking.placementNode = null
        }
        arTracking.defaultScreenLayout()
    }

    private fun loadFloorList() {
        loadMenuRecycler.adapter = LoadMenuAdapter(floorList.toList(), LoadMenuAdapter.TabType.FLOORS) { id: Int ->
            //Load floor
            arTracking.loadFloor(id)
            loadMenu.isVisible = false
        }
    }

    private fun loadDesignList() {
        loadMenuRecycler.adapter = LoadMenuAdapter(designList.toList(), LoadMenuAdapter.TabType.DESIGNS) { id: Int ->
            //Load design
            arTracking.loadDesign(id)
            loadMenu.isVisible = false
        }
    }

    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        val output = super.dispatchTouchEvent(ev)
        CoroutineScope(Dispatchers.IO).launch {
            delay(100)
            CoroutineScope(Dispatchers.Main).launch {
                arTracking.hideActionsPopup()
            }
        }
        return output
    }
}