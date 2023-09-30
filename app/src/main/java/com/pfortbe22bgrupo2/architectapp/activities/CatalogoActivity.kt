package com.pfortbe22bgrupo2.architectapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.Menu
import android.widget.EditText
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
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
    private var isFiltering: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatalogoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        navHost = supportFragmentManager.findFragmentById(R.id.containerView_catalogue) as NavHostFragment
        buttonBar = findViewById(R.id.catalogue_bottom_bar)
        NavigationUI.setupWithNavController(buttonBar, navHost.navController)

        onBackPressedDispatcher.addCallback(this, callback)

    }
    private val callback = object  : OnBackPressedCallback(true){
        override fun handleOnBackPressed() {
            val currentFragment = navHost.navController.currentDestination
            if (currentFragment?.id == R.id.catalogueFragment){
                if (isFiltering) {
                    setToolbarFiltering(false)
                    loadOriginalCatalogue()
                } else {
                    finishAffinity()
                }
            }else{
                navHost.navController.navigateUp()
            }
        }
    }

     private fun loadOriginalCatalogue() {
        val bundle = Bundle()
        navHost.navController.navigate(R.id.catalogueFragment, bundle)
    }

    fun setToolbarFiltering(isFiltering: Boolean) {
        this.isFiltering = isFiltering
    }

}