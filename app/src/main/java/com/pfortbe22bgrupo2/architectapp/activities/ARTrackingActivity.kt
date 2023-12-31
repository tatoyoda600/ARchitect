package com.pfortbe22bgrupo2.architectapp.activities
//https://github.com/SceneView/sceneview-android/blob/main/samples/ar-model-viewer/src/main/java/io/github/sceneview/sample/armodelviewer/MainActivity.kt

import android.graphics.Bitmap
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.text.InputType
import android.util.Log
import android.view.MotionEvent
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.isVisible
import androidx.lifecycle.coroutineScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.google.firebase.storage.StorageReference
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.adapters.LoadMenuAdapter
import com.pfortbe22bgrupo2.architectapp.adapters.ProductHotbarAdapter
import com.pfortbe22bgrupo2.architectapp.data.HotBarSingleton
import com.pfortbe22bgrupo2.architectapp.databinding.ActivityArtrackingBinding
import com.pfortbe22bgrupo2.architectapp.databinding.LoadMenuBinding
import com.pfortbe22bgrupo2.architectapp.databinding.PostCreatingDialogueBinding
import com.pfortbe22bgrupo2.architectapp.entities.Product
import com.pfortbe22bgrupo2.architectapp.utilities.DatabaseHandler
import com.pfortbe22bgrupo2.architectapp.utilities.DefaultARTracking
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream

class ARTrackingActivity: AppCompatActivity() {
    lateinit var binding: ActivityArtrackingBinding
    lateinit var arTracking: DefaultARTracking
    lateinit var database: DatabaseHandler
    lateinit var loadMenu: ViewGroup
    lateinit var loadMenuRecycler: RecyclerView
    val floorList: MutableMap<String, Int> = mutableMapOf()
    val designList: MutableMap<String, Int> = mutableMapOf()
    private val storage_ref: StorageReference = FirebaseStorage.getInstance().reference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityArtrackingBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val hotbar = mainScreenSetup()

        arTracking = DefaultARTracking(5, binding.sceneView, binding.progressIndicator,
            switchToDefaultLayout = fun() {
                binding.defaultLayout.isVisible = true
                binding.placementLayout.isVisible = false
            },
            switchToPlacementLayout = fun() {
                binding.defaultLayout.isVisible = false
                binding.placementLayout.isVisible = true
            },
            onFloorDetectedFunction = fun() {
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
        binding.progressIndicator.isVisible = true
        arTracking.reset()
    }

    private val confirm: (View) -> Unit = {
        arTracking.confirm { arTracking.paintFloor() }
    }

    private val saveFloor: (View) -> Unit = {
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
                arTracking.saveFloor(text)
                dialog.dismiss()
            }
        }
    }

    private val post: (View) -> Unit = {
        arTracking.setPaused(true)
        val dialogBinding = PostCreatingDialogueBinding.inflate(layoutInflater)

        val titleText = dialogBinding.title
        titleText.setText(arTracking.designSession?.name?: "")
        val descriptionText = dialogBinding.description

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.post_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save_floor_popup_yes, null)
            .setNegativeButton(R.string.save_floor_popup_no) { dialog, which ->
                arTracking.setPaused(false)
                dialog.cancel()
            }
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val title = titleText.text.toString()
            val description = descriptionText.text.toString()
            val userNameProcessing = { u: String? ->
                var userName = ""
                u?.let { userName = it}
                if (description.isNotBlank() && title.isNotBlank()) {
                    dialog.dismiss()
                    postear(title, description, userName)
                }
            }
            database.getUserName(database.userId, userNameProcessing, {userNameProcessing(null)})
        }
    }


    private val openLoadMenu: (View) -> Unit = {
        loadMenu.isVisible = true
        arTracking.setPaused(true)
    }

    private val place: (View) -> Unit = {
        arTracking.place()
    }

    private val cancelPlace: (View) -> Unit = {
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

    /** Creates a Post and sends it to the Firestore Database. */
    private fun postear(
        title: String,
        description: String,
        userName: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            var imagePath = ""
            var i = 1
            var searching = true
            while (searching) {
                imagePath = "postPictures/${title}_${i}.jpg"
                try {
                    storage_ref.child(imagePath).downloadUrl.await()
                    i++
                }
                catch (e: Exception) {
                    searching = false
                }
            }

            val imageRef = storage_ref.child(imagePath)
            takeScreenshot() { byteArray ->
                if(byteArray != null){
                    // Subir la imagen a Storage
                    imageRef.putBytes(byteArray)
                        .addOnSuccessListener { taskSnapshot ->
                            // La imagen se ha cargado con éxito, obtén la URL de descarga
                            imageRef.downloadUrl.addOnSuccessListener { uri ->
                                val downloadUrl = uri.toString()

                                //Creo y envio post a Firestore Database
                                database.crearPost(downloadUrl, description, title, userName)
                            }
                            arTracking.setPaused(false)
                        }
                        .addOnFailureListener { exception ->
                            // Handle errors
                            arTracking.setPaused(false)
                        }
                }
                else{
                    Log.e("IMAGEN", "takeScreenshot() devuelve null")
                    arTracking.setPaused(false)
                }
            }
        }
    }

    /**
     * Muestra solamente el ArSceneView para que se pueda tomar una buena imagen.*/
    private fun takeScreenshot(callback: (ByteArray?) -> Unit) {

        CoroutineScope(Dispatchers.IO).launch {
            delay(1000)

            // Crear un bitmap con el tamaño de la vista sceneView
            val bitmap = Bitmap.createBitmap(
                binding.sceneView.width,
                binding.sceneView.height,
                Bitmap.Config.ARGB_8888
            )

            // Utilizar PixelCopy para copiar la vista sceneView al bitmap
            PixelCopy.request(
                binding.sceneView, bitmap, { result ->
                    if (result == PixelCopy.SUCCESS) {
                        // Devolver el bitmap a través del callback
                        // Convertir el bitmap a un arreglo de bytes después de que PixelCopy ha terminado
                        val stream = ByteArrayOutputStream()
                        bitmap.compress(Bitmap.CompressFormat.JPEG, 100, stream)
                        callback(stream.toByteArray())
                    } else {
                        // Si hay algún error, devolver null a través del callback
                        callback(null)
                    }
                },
                Handler(Looper.getMainLooper())
            )
        }
    }

}