package com.pfortbe22bgrupo2.architectapp.activities

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.databinding.ActivityMainBinding

class MainActivity: AppCompatActivity() {

     private lateinit var binding: ActivityMainBinding

    private lateinit var auth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
    }

    override fun onStart() {
        super.onStart()
        checkForCurrentUser()
    }

    private fun checkForCurrentUser() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val intent = Intent(this, CatalogoActivity::class.java)
            startActivity(intent)
        }
    }
}