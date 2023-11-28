package com.pfortbe22bgrupo2.architectapp.fragments

import android.content.Intent
import android.os.Bundle
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
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.activities.MainActivity
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentProfileBinding
import com.pfortbe22bgrupo2.architectapp.viewModels.ProfileViewModel


class ProfileFragment: Fragment() {

    private lateinit var binding: FragmentProfileBinding
    private lateinit var profileViewModel: ProfileViewModel
    private lateinit var toolbar: Toolbar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentProfileBinding.inflate(inflater, container,false)
        profileViewModel = ViewModelProvider(this).get(ProfileViewModel::class.java)

        //auth = Firebase.auth
        //currentUser = auth.currentUser!!
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
        profileViewModel.fetchUserData()
        profileViewModel.currentUserData.observe(this, Observer {
            binding.profileUserNameTextView.text = "Usuario: ${it?.userName}"
            binding.profileEmailTextView.text = "E-mail: ${it?.email}"
            var phone = it?.phone
            if (phone.isNullOrEmpty()) phone = ""
            binding.profilePhoneNumberTextView.text = "Telefono: ${phone}"
            var address = it?.address
            if (address.isNullOrEmpty()) address = ""
            binding.profileDirectionTextView.text = "Direccion: ${address}"
            Glide.with(requireContext())
            .load(it?.photoURL)
            .apply(RequestOptions().placeholder(R.drawable.profile_pic).transform(CircleCrop()))
            .into(binding.profileUserPicImageView)
        })
    }


    private fun initToolbar(){
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)
    }


    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.profile_toolbar, menu)
        profileViewModel.currentUserData.observe(this, Observer {
            val deleteUserByAdminMenu = menu.findItem(R.id.item_admin_options)
            var isAdmin = it?.isAdmin
            if (isAdmin == null) isAdmin = false
            deleteUserByAdminMenu?.isVisible = isAdmin

        })
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
                profileViewModel.signOut()
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
            profileViewModel.deleteUser()
            navToMainActivity()
        }
        builder.setNegativeButton("No") { dialog, which ->}
        val dialog = builder.create()
        dialog.show()
    }

    private fun navToMainActivity() {
        val intent = Intent(requireActivity(), MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        intent.putExtra("activityToLoad", "MainActivity")
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