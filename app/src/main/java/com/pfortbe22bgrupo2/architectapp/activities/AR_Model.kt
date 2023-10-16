package com.pfortbe22bgrupo2.architectapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.core.view.isGone
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.ExtendedFloatingActionButton
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.utilities.Render3D
import com.pfortbe22bgrupo2.architectapp.databinding.ActivityArModelBinding
import io.github.sceneview.ar.ArSceneView
import io.github.sceneview.ar.node.ArModelNode

class AR_Model: AppCompatActivity() {

    private lateinit var binding: ActivityArModelBinding
    private lateinit var bottomNavigationView: BottomNavigationView
    private lateinit var sceneView: ArSceneView
    private lateinit var placeButton: ExtendedFloatingActionButton
    private lateinit var render3D: Render3D

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
        render3D = Render3D(sceneView)

        val modelNode = render3D.arRender("models/gaming_chair_free_download.glb")
        placeButton.setOnClickListener {
            place(modelNode)
        }
    }

    private fun place(modelNode: ArModelNode) {
        modelNode.anchor()
        placeButton.isGone = true
        sceneView.planeRenderer.isVisible = false
    }
}