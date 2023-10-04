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
        //Aca no va "binding" porque despues no lo puede castear a "NavHostFragment y da error
        //val navHost: NavHostFragment = binding.containerViewCatalogue as NavHostFragment
        //navController = navHost.navController
        val navHostFragment = supportFragmentManager.findFragmentById(R.id.containerView_catalogue) as NavHostFragment
        navController = navHostFragment.navController

        buttonBar = binding.catalogueBottomBar
        NavigationUI.setupWithNavController(buttonBar, navController)

        onBackPressedDispatcher.addCallback(this, callback)
    }

    private val callback = object: OnBackPressedCallback(true) {
        override fun handleOnBackPressed() {
            val currentFragment = navController.currentDestination
            if (currentFragment?.id == R.id.foroFragment) {
                if (isFiltering) {
                    setToolbarFiltering(false)
                    Toast.makeText(this@CatalogoActivity, "esta entrando al filter del foro, pero no carga la lista original", Toast.LENGTH_SHORT).show()
                    loadOriginalPotsList()
                }
            }

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

    private fun loadOriginalPotsList() {
        val bundle = Bundle()
        navController.navigate(R.id.foroFragment, bundle)
    }

    fun setToolbarFiltering(isFiltering: Boolean) {
        this.isFiltering = isFiltering
    }

}