package com.pfortbe22bgrupo2.architectapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.EditText
import androidx.appcompat.widget.Toolbar
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.databinding.ActivityCatalogoBinding
import com.pfortbe22bgrupo2.architectapp.databinding.ActivityMainBinding
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentCatalogueBinding

class CatalogoActivity : AppCompatActivity() {

    private lateinit var buttonBar : BottomNavigationView
    private lateinit var navHost : NavHostFragment

    lateinit var binding : ActivityCatalogoBinding

    //private var _binding: FragmentCatalogueBinding? = null
    //private val binding get() = _binding!!
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        //setContentView(R.layout.activity_catalogo)
        binding = ActivityCatalogoBinding.inflate(layoutInflater)
        setContentView(binding.root)

        //val toolbar: Toolbar = findViewById(R.id.catalogue_toolbar)
       // val toolbar : Toolbar = binding.catalogueToolbar
       // setSupportActionBar(toolbar)


        navHost = supportFragmentManager.findFragmentById(R.id.containerView_catalogue) as NavHostFragment
        buttonBar = findViewById(R.id.catalogue_bottom_bar)
        NavigationUI.setupWithNavController(buttonBar, navHost.navController)


        //val editTextSearch: EditText = binding.editTextSearch

        //editTextSearch.setOnClickListener(){text ->
         //   binding.
        //}

        //editTextSearch.setOnTextChangedListener { text ->
          //  adapter.filter(text)
        //}
    }
/*
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }*/



}