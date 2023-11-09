package com.pfortbe22bgrupo2.architectapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Button
import androidx.core.view.isGone
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.storage.FirebaseStorage

import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.utilities.Render3D
import com.pfortbe22bgrupo2.architectapp.databinding.ActivityArModelBinding
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode

//Variable para elegir por categoría (TEMPORAL)
private const val NAME_CATEGORY = "sofas"
//Variable para elegir el modelo a mostrar (TEMPORAL)
private const val NAME_MODEL = "IKEA-Arild_2_Seat_Sofa-3D.glb"

class AR_Model : AppCompatActivity(R.layout.activity_ar_model) {

    private lateinit var binding: ActivityArModelBinding
    private lateinit var sceneView: ArSceneView
    private lateinit var progressBar: CircularProgressIndicator
    //private lateinit var placeButton: ExtendedFloatingActionButton
    private lateinit var placeButton: Button
    private lateinit var render3D: Render3D
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityArModelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        sceneView = binding.arSceneView
        //placeButton = binding.extendedFloatingActionButton
        placeButton = binding.extendedFloatingActionButton
        progressBar = binding.loadingView
        render3D = Render3D(sceneView, progressBar)

        val storage = FirebaseStorage.getInstance()

        val futureModels = storage.reference.child("/models/${NAME_CATEGORY}").listAll()

        futureModels.addOnSuccessListener {

            val models = it.items

            if(models.isNotEmpty()){
                val model = models.find {
                    model ->
                    model.name == NAME_MODEL
                }

                if(model != null){
                    model.downloadUrl.addOnSuccessListener {
                        val modelNode = render3D.arRender(it.toString())
                        placeButton.setOnClickListener {
                            place(modelNode)
                        }
                    }
                }
            }
        }
    }

    private fun place(modelNode: ArModelNode){
        modelNode.anchor()
        placeButton.isGone = true
        sceneView.planeRenderer.isVisible = false
    }
}