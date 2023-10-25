package com.pfortbe22bgrupo2.architectapp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Button
import com.pfortbe22bgrupo2.architectapp.activities.ProductListActivity

class MainActivity : AppCompatActivity() {

    private lateinit var view : View
    private lateinit var loginButton : Button

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        setClickListener()
    }
    private fun setClickListener() {
        loginButton = findViewById(R.id.log_in_button);
        loginButton.setOnClickListener {
            val intent = Intent(this, ProductListActivity::class.java)
            startActivity(intent)
        }
    }

}