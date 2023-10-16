package com.pfortbe22bgrupo2.architectapp.activities

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pfortbe22bgrupo2.architectapp.R

import com.pfortbe22bgrupo2.architectapp.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity(R.layout.activity_main) {
    private lateinit var binding: ActivityMainBinding
    private lateinit var bottomNavigationView: BottomNavigationView
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        bottomNavigationView = binding.bottomNav

        bottomNavigationView.selectedItemId = R.id.ItemHome

        binding.bottomNav.setOnItemSelectedListener {

            when (it.itemId) {
                R.id.ItemHome -> {}

                R.id.Item3D -> {
                    startActivity(Intent(applicationContext, ARTrackingTest::class.java))
                    overridePendingTransition(R.anim.slide_in_right, R.anim.slide_out_left)
                    finish()
                }

            }
            true
        }

    }
}