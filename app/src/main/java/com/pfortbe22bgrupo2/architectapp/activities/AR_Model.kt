package com.pfortbe22bgrupo2.architectapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.google.android.material.progressindicator.CircularProgressIndicator
import com.google.firebase.storage.FirebaseStorage

import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.Render3D
import com.pfortbe22bgrupo2.architectapp.databinding.ActivityArModelBinding
import io.github.sceneview.ar.ArSceneView


class AR_Model : AppCompatActivity(R.layout.activity_ar_model), Render3D {

    private lateinit var binding: ActivityArModelBinding
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var sceneView: ArSceneView
    private lateinit var progressBar: CircularProgressIndicator
    private lateinit var placeButton: ExtendedFloatingActionButton
    private lateinit var name: String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityArModelBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigationView = binding.bottomNav

        bottomNavigationView.selectedItemId = R.id.Item3D

        binding.bottomNav.setOnItemSelectedListener {

            when(it.itemId){
                R.id.ItemHome -> {
                    startActivity(Intent(applicationContext, MainActivity::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                }

                R.id.Item3D -> {}

            }
            true
        }

        sceneView = binding.arSceneView

        placeButton = binding.extendedFloatingActionButton

        progressBar = binding.loadingView

        val storage = FirebaseStorage.getInstance()

        //Variable para elegir por categoría
        val category = "sofas"

        val futureModels = storage.reference.child("/models/${category}").listAll()

        //Variable para elegir el modelo a mostrar
        name = "IKEA-Arild_2_Seat_Sofa-3D.glb"

        futureModels.addOnSuccessListener {

            val models = it.items

            if(models.isNotEmpty()){
                val model = models.find {
                    model ->
                    model.name == name
                }

                if(model != null){
                    model.downloadUrl.addOnSuccessListener {
                        render(sceneView, placeButton, it.toString() , progressBar)
                    }
                }
            }
        }
    }
}