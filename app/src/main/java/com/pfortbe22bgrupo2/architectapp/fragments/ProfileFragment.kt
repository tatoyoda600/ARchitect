package com.pfortbe22bgrupo2.architectapp.fragments

import android.content.ContentValues.TAG
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.activities.MainActivity
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentProfileBinding


class ProfileFragment: Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container,false)
        auth = Firebase.auth
        return binding.root
    }



    override fun onStart() {
        super.onStart()
        initToolbar()
        updateProfile()
        binding.editProfileButton.setOnClickListener {
            navToEditProfile()
        }
        binding.changePasswordTextView.setOnClickListener{
            //navToChangePassword()
        }
    }

    private fun updateProfile() {
        val currentUser = auth.currentUser
        if (currentUser != null) {
            val docRef = db.collection("users").document(currentUser.uid)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val userName = document.data?.get("userName") as? String
                        binding.profileUserNameTextView.text = userName
                        binding.profileEmailTextView.text = currentUser.email
                        binding.profilePhoneNumberTextView.text = "11111111"
                        binding.profileDirectionTextView.text = "calle 123"
                        binding.profileUserPicImageView.setImageResource(R.drawable.profile_pic)
                        //binding.profileDirectionTextView.text = currentUser.direction
                        //binding.profilePhoneNumberTextView.text = currentUser.phoneNumber
                        //binding.profileUserPicImageView.setImageResource(currentUser.profilePic)
                        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
        }

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
            R.id.logout_item -> {
                auth.signOut()
                val intent = Intent(requireActivity(), MainActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                startActivity(intent)
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
        val action = ProfileFragmentDirections.actionProfileFragmentToDesignWallContainerFragment()
        findNavController().navigate(action)
    }
}