package com.pfortbe22bgrupo2.architectapp.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.bitmap.CircleCrop
import com.bumptech.glide.request.RequestOptions
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentEditProfileBinding
import com.pfortbe22bgrupo2.architectapp.viewModels.EditProfileViewModel


class EditProfileFragment: Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var editProfileViewModel: EditProfileViewModel
    private val GALLERY_REQUEST_CODE = 1
    private val CAMERA_REQUEST_CODE = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProfileBinding.inflate(inflater, container,false)
        editProfileViewModel = ViewModelProvider(this).get(EditProfileViewModel::class.java)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        updateProfile()
        binding.editButton.setOnClickListener() {
            if (updateUser()) findNavController().navigateUp()
        }
        binding.floatingActionButton.setOnClickListener(){
            showImagePickerDialog(requireContext())
        }
    }

    fun showImagePickerDialog(context: Context) {
        val builder = AlertDialog.Builder(context)
        builder.setTitle("Seleccionar imagen")
        val options = arrayOf("Seleccionar de la galería", "Tomar foto con la cámara")
        builder.setItems(options) { dialog, item ->
            when (item) {
                0 -> {
                    val galleryIntent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
                    startActivityForResult(galleryIntent, GALLERY_REQUEST_CODE)
                }
                1 -> {
                    val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
                }
            }
        }
        builder.show()
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (resultCode == Activity.RESULT_OK) {
            if (requestCode == GALLERY_REQUEST_CODE) {
                // Imagen seleccionada desde la galería
                val imageUri = data?.data
                binding.editUserPicImageView.setImageURI(imageUri)
                uploadToFirebaseGallery(imageUri)
            } else if (requestCode == CAMERA_REQUEST_CODE) {
                // Imagen capturada desde la cámara (data contiene la imagen)
                val imageBitmap = data?.extras?.get("data") as Bitmap
                binding.editUserPicImageView.setImageBitmap(imageBitmap)
                uploadToFirebaseCamara(imageBitmap)
            }
        }

    }

    private fun uploadToFirebaseGallery(imageUri: Uri?) {
        if (imageUri != null) {
            editProfileViewModel.uploadImageFromGallery(imageUri)
            editProfileViewModel.currentUserData.observe(this, Observer {
                //VER QUE SE CARGUE MAS RAPIDO O EL PROGRES BAR
                binding.progressBar.visibility = View.VISIBLE
                Glide.with(requireContext()).load(it?.profileImageUrl).into(binding.editUserPicImageView)
                binding.progressBar.visibility = View.GONE
            })

        }
    }

    private fun uploadToFirebaseCamara(imageBitmap: Bitmap) {

        editProfileViewModel.uploadImageFromCamara(imageBitmap)
        editProfileViewModel.currentUserData.observe(this, Observer {
            Glide.with(requireContext()).load(it?.profileImageUrl).apply(RequestOptions().placeholder(R.drawable.profile_pic)).into(binding.editUserPicImageView)
        })

    }

    private fun updateUser(): Boolean {
        val isValid = validateFields()
        if (isValid){
            val email = binding.emailEditEditText.text.toString()
            val userName = binding.nameEditEditText.text.toString()
            val address = binding.addressEditEditText.text.toString()
            val phone = binding.phoneEditEditText.text.toString()
            editProfileViewModel.updateUserData(email, userName, address, phone)
        }
        return isValid
    }

    private fun validateFields(): Boolean {
        var isValid = true
        val name: Int = 6
        val phone: Int = 10
        val address: Int = 8
        if (binding.nameEditEditText.text.toString().length < name){
            binding.nameEditEditText.error = "Nombre de usuario muy corto"
            isValid = false
        }
        if (!Patterns.EMAIL_ADDRESS.matcher(binding.emailEditEditText.text.toString()).matches()){
            binding.emailEditEditText.error = "Correo electrónico inválido"
            isValid = false
        }
        if (binding.phoneEditEditText.text.toString().length != phone) {
            binding.phoneEditEditText.error = "Telefono invalido"
            isValid = false
        }
        if (binding.addressEditEditText.text.toString().length < address) {
            binding.addressEditEditText.error = "La direccion debe contener 8 caracteres minimo"
            isValid = false
        }
        return isValid
    }

    private fun updateProfile() {
        editProfileViewModel.fetchUserData()
        editProfileViewModel.currentUserData.observe(this, Observer {
            binding.nameEditEditText.text = Editable.Factory.getInstance().newEditable(it?.userName)
            binding.emailEditEditText.text = Editable.Factory.getInstance().newEditable(it?.email)
            var phone = it?.phone
            if (phone.isNullOrEmpty()) phone = ""
            binding.phoneEditEditText.text = Editable.Factory.getInstance().newEditable(phone)
            var address = it?.address
            if (address.isNullOrEmpty()) address = ""
            binding.addressEditEditText.text = Editable.Factory.getInstance().newEditable(address)
            Glide.with(requireContext())
                .load(it?.profileImageUrl)
                .apply(RequestOptions().placeholder(R.drawable.profile_pic).transform(CircleCrop()))
                .into(binding.editUserPicImageView)
        })
    }
}

