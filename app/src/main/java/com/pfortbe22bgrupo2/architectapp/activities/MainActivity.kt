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
     private var isNotDeletedUser: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        auth = Firebase.auth
        checkForCurrentUser()

    }

    private fun checkForLoadActivity() {
        val activityToLoad = this.intent?.getStringExtra("activityToLoad")
        if (activityToLoad == null){
            isNotDeletedUser = true
        }
    }

    private fun checkForCurrentUser() {
        checkForLoadActivity()
        val currentUser = auth.currentUser
        if (currentUser != null && isNotDeletedUser) {
            val intent = Intent(this, CatalogueActivity::class.java)
            startActivity(intent)
        }
    }
}