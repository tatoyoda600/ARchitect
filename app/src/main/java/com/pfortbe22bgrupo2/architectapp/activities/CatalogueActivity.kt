package com.pfortbe22bgrupo2.architectapp.activities

import android.os.Bundle
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.navigation.NavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.databinding.ActivityCatalogueBinding
import com.pfortbe22bgrupo2.architectapp.fragments.CatalogueFragment
import com.pfortbe22bgrupo2.architectapp.fragments.DetailsFragment
import com.pfortbe22bgrupo2.architectapp.viewModels.CatalogueViewModel

class CatalogueActivity: AppCompatActivity() {

    private lateinit var buttonBar: BottomNavigationView
    private lateinit var navController: NavController
    lateinit var binding: ActivityCatalogueBinding
    private var isFiltering: Boolean = false
    private lateinit var auth: FirebaseAuth
    private lateinit var navHost: NavHostFragment
    val tuViewModel by viewModels<CatalogueViewModel>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCatalogueBinding.inflate(layoutInflater)
        setContentView(binding.root)
        initBottomNavView()
        onBackPressedDispatcher.addCallback(this, callback)
        auth = Firebase.auth

        // Aquí puedes agregar o reemplazar fragmentos según sea necesario
        val fragmentA = CatalogueFragment()
        val fragmentB = DetailsFragment()

        // Pasar la instancia del ViewModel a ambos fragmentos
        fragmentA.viewModel = tuViewModel
        fragmentB.viewModel = tuViewModel
    }

    private fun initBottomNavView(){
        navHost = binding.containerViewCatalogue.getFragment() as NavHostFragment
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