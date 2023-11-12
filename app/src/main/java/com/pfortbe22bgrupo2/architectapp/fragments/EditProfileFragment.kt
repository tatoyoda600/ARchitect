package com.pfortbe22bgrupo2.architectapp.fragments

import android.app.Activity
import android.app.AlertDialog
import android.content.ContentValues
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.net.Uri
import android.os.Bundle
import android.provider.MediaStore
import android.text.Editable
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentEditProfileBinding
import java.io.ByteArrayOutputStream


class EditProfileFragment: Fragment() {

    private lateinit var binding: FragmentEditProfileBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore
    private lateinit var currentUser: FirebaseUser
    private val GALLERY_REQUEST_CODE = 1
    private val CAMERA_REQUEST_CODE = 2

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentEditProfileBinding.inflate(inflater, container,false)
        auth = Firebase.auth
        currentUser = auth.currentUser!!
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        updateProfile()
        binding.editButton.setOnClickListener() {
            updateUser()
            findNavController().navigateUp()
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
            // Genera un nombre de archivo único para la imagen (por ejemplo, usando el ID del usuario)
            val imageFileName = "${currentUser.uid}.jpg"
            val storageReference = FirebaseStorage.getInstance().reference.child("images/$imageFileName")
            // Sube la imagen a Firebase Storage
            storageReference.putFile(imageUri)
                .addOnSuccessListener { taskSnapshot ->
                    // La imagen se subió exitosamente
                    // Ahora obtén la URL de la imagen
                    storageReference.downloadUrl.addOnSuccessListener { uri ->
                        val imageUrl = uri.toString()
                        Glide.with(requireContext()).load(imageUrl).into(binding.editUserPicImageView)
                        // Guarda la URL de la imagen en Firebase Firestore
                        val docRef = db.collection("users").document(currentUser.uid)
                        docRef.update("profileImageUrl", imageUrl)
                            .addOnSuccessListener {
                                // La URL de la imagen se ha guardado en Firestore
                            }
                            .addOnFailureListener { e ->
                                // Error al guardar la URL en Firestore
                            }
                    }
                }
                .addOnFailureListener { e ->
                    // Error al subir la imagen a Storage
                }
        }
    }

    private fun uploadToFirebaseCamara(imageBitmap: Bitmap) {
        val imageFileName = "${currentUser.uid}.jpg"
        val storageReference = FirebaseStorage.getInstance().reference.child("images/$imageFileName.jpg")
        // Comprime la imagen y conviértela en un arreglo de bytes
        val byteArrayOutputStream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()
        // Sube la imagen a Firebase Storage
        storageReference.putBytes(data)
            .addOnSuccessListener { taskSnapshot ->
                // La imagen se subió exitosamente
                // Ahora obtén la URL de la imagen
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    Glide.with(requireContext()).load(imageUrl).apply(RequestOptions().placeholder(R.drawable.profile_pic)).into(binding.editUserPicImageView)
                    // Guarda la URL de la imagen en Firebase Firestore
                    val docRef = db.collection("users").document(currentUser.uid)
                    docRef.update("profileImageUrl", imageUrl)
                        .addOnSuccessListener {
                            // La URL de la imagen se ha guardado en Firestore
                        }
                        .addOnFailureListener { e ->
                            // Error al guardar la URL en Firestore
                        }
                }
            }
            .addOnFailureListener { e ->
                // Error al subir la imagen a Storage
            }
    }

    private fun updateUser() {
        val docRef = db.collection("users").document(currentUser.uid)
        val updates = hashMapOf<String, Any>(
            "email" to binding.emailEditEditText.text.toString(),
            "userName" to binding.nameEditEditText.text.toString(),
            "address" to binding.addressEditEditText.text.toString(),
            "phoneNumber" to binding.phoneEditEditText.text.toString()
        )
        docRef.update(updates)
    }

    private fun updateProfile() {
        val docRef = db.collection("users").document(currentUser.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val userName = document.data?.get("userName") as? String
                    binding.nameEditEditText.text = Editable.Factory.getInstance().newEditable(userName)
                    binding.emailEditEditText.text = Editable.Factory.getInstance().newEditable(currentUser.email)
                    var phoneNumber = document.data?.get("phoneNumber") as? String
                    if (phoneNumber.isNullOrEmpty()) phoneNumber = ""
                    binding.phoneEditEditText.text = Editable.Factory.getInstance().newEditable(phoneNumber)
                    var address = document.data?.get("address") as? String
                    if (address.isNullOrEmpty()) address = ""
                    binding.addressEditEditText.text = Editable.Factory.getInstance().newEditable(address)
                    Log.d(ContentValues.TAG, "DocumentSnapshot data: ${document.data}")
                    val uri = document.data?.get("profileImageUrl")
                    Glide.with(requireContext())
                        .load(uri)
                        .apply(RequestOptions.circleCropTransform())
                        .into(binding.editUserPicImageView)
                } else {
                    Log.d(ContentValues.TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }
    }
}

