package com.pfortbe22bgrupo2.architectapp.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.databinding.ActivityCatalogoBinding

class CatalogoActivity: AppCompatActivity() {

    private lateinit var buttonBar: BottomNavigationView
    private lateinit var navController: NavController
    lateinit var binding: ActivityCatalogoBinding
    private var isFiltering: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatalogoBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initBottomNavView()
        onBackPressedDispatcher.addCallback(this, callback)
    }

    private fun initBottomNavView(){
        val navHost: NavHostFragment = binding.containerViewCatalogue.getFragment() as NavHostFragment
        navController = navHost.navController
        buttonBar = binding.catalogueBottomBar
        NavigationUI.setupWithNavController(buttonBar, navController)
    }

    private val callback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val currentFragment = navController.currentDestination
            if (currentFragment?.id == R.id.catalogueFragment) {
                if (isFiltering) {
                    setToolbarFiltering(false)
                    loadOriginalCatalogue()
                } else {
                    finishAffinity()
                }
            }
            else {
                navController.navigateUp()
            }
        }
    }

     private fun loadOriginalCatalogue() {
        val bundle = Bundle()
        navController.navigate(R.id.catalogueFragment, bundle)
     }

    fun setToolbarFiltering(isFiltering: Boolean) {
        this.isFiltering = isFiltering
    }

}