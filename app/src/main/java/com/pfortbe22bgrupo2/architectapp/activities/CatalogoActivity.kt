package com.pfortbe22bgrupo2.architectapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pfortbe22bgrupo2.architectapp.R

class CatalogoActivity : AppCompatActivity() {

    private lateinit var buttonBar : BottomNavigationView
    private lateinit var navHost : NavHostFragment
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_catalogo)


        navHost = supportFragmentManager.findFragmentById(R.id.containerView_catalogue) as NavHostFragment
        buttonBar = findViewById(R.id.catalogue_bottom_bar)
        NavigationUI.setupWithNavController(buttonBar, navHost.navController)
    }



}