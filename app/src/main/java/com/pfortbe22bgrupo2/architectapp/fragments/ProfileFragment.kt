package com.pfortbe22bgrupo2.architectapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.navigation.ui.AppBarConfiguration
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentProfileBinding


class ProfileFragment: Fragment() {

    private lateinit var binding: FragmentProfileBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        binding = FragmentProfileBinding.inflate(inflater, container,false)
        //editButton = v.findViewById(R.id.edit_profile_button)
        return binding.root
    }

    override fun onStart() {
        super.onStart()

        initToolbar()
    }

    private fun initToolbar(){
        val toolbar: Toolbar = binding.profileToolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_toolbar, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_item_edit_profile -> {
                navToEditProfile()
                true
            }
            R.id.menu_item_designs -> {
                navToSavedDesigns()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun navToEditProfile(){
        val action = ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment()
        findNavController().navigate(action)
    }
    private fun navToSavedDesigns(){
        //val action = ProfileFragmentDirections.actionProfileFragmentToSavedDesignsFragment()
        val action = ProfileFragmentDirections.actionProfileFragmentToDesignWallContainerFragment()
        findNavController().navigate(action)
    }
}