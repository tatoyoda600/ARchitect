package com.pfortbe22bgrupo2.architectapp.activities
//https://github.com/SceneView/sceneview-android/blob/main/samples/ar-model-viewer/src/main/java/io/github/sceneview/sample/armodelviewer/MainActivity.kt

import android.graphics.Bitmap
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
import com.pfortbe22bgrupo2.architectapp.databinding.PostCreatingDialogueBinding
import com.pfortbe22bgrupo2.architectapp.entities.Product
import com.pfortbe22bgrupo2.architectapp.utilities.DatabaseHandler
import com.pfortbe22bgrupo2.architectapp.utilities.DefaultARTracking
import com.pfortbe22bgrupo2.architectapp.utilities.Storage_ref
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.io.ByteArrayOutputStream


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
                arTracking.saveFloor(binding.root.context, text)
                dialog.dismiss()
            }
        }
    }

    private val post: (View) -> Unit = {
        val dialogBinding = PostCreatingDialogueBinding.inflate(layoutInflater)

        val titleText = dialogBinding.title
        val descriptionText = dialogBinding.description

        val dialog = AlertDialog.Builder(this)
            .setTitle(R.string.post_title)
            .setView(dialogBinding.root)
            .setPositiveButton(R.string.save_floor_popup_yes, null)
            .setNegativeButton(R.string.save_floor_popup_no) { dialog, which -> dialog.cancel() }
            .show()

        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val title = titleText.text.toString()
            val description = descriptionText.text.toString()
            val userNameProcessing = { u: String? ->
                var userName = ""
                u?.let { userName = it}
                if (description.isNotBlank() && title.isNotBlank()) {
                    dialog.dismiss()
                    postear(binding.root, title, description, userName)
                }
            }
            database.getUserName(auth.currentUser!!.uid, userNameProcessing, {userNameProcessing(null)})
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

    /** Creates a Post and sends it to the Firestore Database. */
    fun postear(
        view: View,
        title: String,
        description: String,
        userName: String
    ) {
//        setPaused(true)
        CoroutineScope(Dispatchers.IO).launch {
            // The camera's -X rotation is its yaw rotation
            hacerScreenshot(view, userName, description, title)

//            setPaused(false)
        }
    }

    private fun hacerScreenshot(
        view: View,
        userName: String,
        description: String,
        title: String
    ) {
        CoroutineScope(Dispatchers.IO).launch {
            var imagePath = ""
            var i = 1
            var searching = true
            while (searching) {
                imagePath = "postPictures/${title}_${i}.jpg"
                try {
                    Storage_ref.child(imagePath).downloadUrl.await()
                    i++
                }
                catch (e: Exception) {
                    searching = false
                }
            }

            val imageRef = Storage_ref.child(imagePath)
            takeScreenshot(view) { byteArray ->
                // Subir la imagen a Storage
                imageRef.putBytes(byteArray)
                    .addOnSuccessListener { taskSnapshot ->
                        // La imagen se ha cargado con éxito, obtén la URL de descarga
                        imageRef.downloadUrl.addOnSuccessListener { uri ->
                            val downloadUrl = uri.toString()

                            //Creo y envio post a Firestore Database
                            database.crearPost(downloadUrl, description, title, userName)
                        }
                    }
                    .addOnFailureListener { exception ->
                        // Handle errors
                    }
            }
        }
    }

    /**
     * Muestra solamente el ArSceneView para que se pueda tomar una buena imagen.*/
    private suspend fun takeScreenshot(view: View, callback: (ByteArray) -> Unit) {
        CoroutineScope(Dispatchers.Main).launch {
            binding.linearLayout2.isVisible = false
            binding.productHotbar.isVisible = false
            binding.linearLayout.isVisible = false
            binding.linearLayout3.isVisible = false

            CoroutineScope(Dispatchers.IO).launch {

                delay(1000)

                // Obtener la Uri de la imagen local en tu dispositivo
                view.isDrawingCacheEnabled = true
                view.buildDrawingCache(true)
                val bitMapImagen = Bitmap.createBitmap(view.drawingCache)
                view.isDrawingCacheEnabled = false

                // Convertir el bitMapImagen a un ByteArrayOutputStream
                val byteArrayOutputStream = ByteArrayOutputStream()

                // Comprimir el Bitmap en formato JPEG con calidad del 100% (puedes ajustar la calidad según tus necesidades)
                bitMapImagen.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)

                // Convertir el ByteArrayOutputStream a un arreglo de bytes
                val byteArray = byteArrayOutputStream.toByteArray()
                CoroutineScope(Dispatchers.Main).launch {
                    binding.linearLayout2.isVisible = true
                    binding.productHotbar.isVisible = true
                    binding.linearLayout.isVisible = true
                    binding.linearLayout3.isVisible = true
                }
                callback(byteArray)
            }
        }
    }


    /**
     * Verifica que el path que se esta usando no este ya usado y sino le agrega un numero mas al final.
     * */
    private suspend fun doesFileExist(nameFile: String): String {

        var i = 1
        var path = ""

        while (true) {
            path = "postPictures/${nameFile}_${i}.jpg"
            val storageRef = Storage_ref.child(path)

            try {
                storageRef.downloadUrl.await()
                // El archivo existe, incrementa el índice y continúa iterando
                i++
            } catch (e: Exception) {
                // El archivo no existe, se rompe el bucle
                break
            }
        }

        return path
    }
}