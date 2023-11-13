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
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.activities.MainActivity
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentProfileBinding


class ProfileFragment: Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private lateinit var currentUser: FirebaseUser
    private lateinit var toolbar: Toolbar
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container,false)
        auth = Firebase.auth
        currentUser = auth.currentUser!!
        toolbar = binding.profileToolbar
        toolbar.overflowIcon = ContextCompat.getDrawable(requireContext(), R.drawable.menu_icon)

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
            navToChangePassword()
        }

    }

    private fun updateProfile() {
            val docRef = db.collection("users").document(currentUser.uid)
            docRef.get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        //isAdmin = document.data?.get("isAdmin") as? Boolean == true
                        val userName = document.data?.get("userName") as? String
                        binding.profileUserNameTextView.text = "Usuario: ${userName}"
                        binding.profileEmailTextView.text = "E-mail: ${currentUser.email}"
                        var address = document.data?.get("address") as? String
                        if (address.isNullOrEmpty()) address = ""
                        binding.profileDirectionTextView.text = "Direccion: ${address}"
                        var phone = document.data?.get("phoneNumber") as? String
                        if (phone.isNullOrEmpty()) phone = ""
                        binding.profilePhoneNumberTextView.text = "Telefono: ${phone}"
                        val uri = document.data?.get("profileImageUrl")
                        Glide.with(requireContext())
                            .load(uri)
                            .apply(RequestOptions().placeholder(R.drawable.profile_pic).transform(
                                CircleCrop()
                            ))
                            .into(binding.profileUserPicImageView)
                        Log.d(TAG, "DocumentSnapshot data: ${document.data}")
                    } else {
                        Log.d(TAG, "No such document")
                    }
                }
                .addOnFailureListener { exception ->
                    Log.d(TAG, "get failed with ", exception)
                }
    }


    private fun initToolbar(){
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_toolbar, menu)
        val docRef = db.collection("users").document(currentUser.uid)
        var isAdmin = false
        docRef.get().addOnCompleteListener {
            if (it.isSuccessful){
                isAdmin = it.result.data?.get("isAdmin") as Boolean
            }
            val deleteUserByAdminMenu = menu.findItem(R.id.item_admin_options)
            deleteUserByAdminMenu.isVisible = isAdmin
        }
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
                navToMainActivity()
                true
            }
            R.id.delete_user_item -> {
                showConfirmationDialog()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    private fun showConfirmationDialog() {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmación")
        builder.setMessage("¿Estás seguro de que deseas eliminar su cuenta?")

        builder.setPositiveButton("Sí") { dialog, which ->
            deleteUserFromFirestore()
            deleteUserFromAuth()
            navToMainActivity()
        }

        builder.setNegativeButton("No") { dialog, which ->}
        val dialog = builder.create()
        dialog.show()

    }

    private fun deleteUserFromAuth() {
        currentUser.delete()
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "User account deleted.")
                }
            }
    }

    private fun deleteUserFromFirestore() {
        db.collection("users").document(currentUser.uid)
            .delete()
            .addOnSuccessListener { Log.d(TAG, "DocumentSnapshot successfully deleted!") }
            .addOnFailureListener { e -> Log.w(TAG, "Error deleting document", e) }
    }

    private fun navToMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }

    private fun navToEditProfile(){
        val action = ProfileFragmentDirections.actionProfileFragmentToEditProfileFragment()
        findNavController().navigate(action)
    }
    private fun navToSavedDesigns(){
        val action = ProfileFragmentDirections.actionProfileFragmentToDesignWallContainerFragment()
        findNavController().navigate(action)
    }

    private fun navToChangePassword() {
        val action =ProfileFragmentDirections.actionProfileFragmentToChangePasswordFragment()
        findNavController().navigate(action)
    }

}