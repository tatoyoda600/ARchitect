package com.pfortbe22bgrupo2.architectapp.viewModels

import android.content.ContentValues
import android.graphics.Bitmap
import android.net.Uri
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.FirebaseStorage
import com.pfortbe22bgrupo2.architectapp.entities.UserProfileData
import java.io.ByteArrayOutputStream

class EditProfileViewModel: ViewModel() {

    private val db = Firebase.firestore
    private val auth = Firebase.auth
    private val currentUser = auth.currentUser!!
    val currentUserData = MutableLiveData<UserProfileData?>()


    fun updateUserData(email: String, userName: String, address: String, phone: String) {
        val docRef = db.collection("users").document(currentUser.uid)
        val updates = hashMapOf<String, Any>(
            "email" to email,
            "userName" to userName,
            "address" to address,
            "phoneNumber" to phone
        )
        docRef.update(updates)
    }

    fun fetchUserData() {
        val docRef = db.collection("users").document(currentUser.uid)
        docRef.get()
            .addOnSuccessListener { document ->
                if (document != null) {
                    val userName = document.data?.get("userName") as? String
                    val email = document.data?.get("email") as? String
                    val address = document.data?.get("address") as? String
                    val phone = document.data?.get("phoneNumber") as? String
                    val uri = document.data?.get("profileImageUrl") as String
                    val isAdmin = document.data?.get("isAdmin") as? Boolean

                    val data = UserProfileData(userName,email,address,phone,uri,isAdmin)
                    currentUserData.postValue(data)
                    Log.d(ContentValues.TAG, "DocumentSnapshot data: ${document.data}")
                } else {
                    currentUserData.postValue(null)
                    Log.d(ContentValues.TAG, "No such document")
                }
            }
            .addOnFailureListener { exception ->
                Log.d(ContentValues.TAG, "get failed with ", exception)
            }
    }

    fun uploadImageFromGallery(imageUri: Uri) {
        val imageFileName = "${currentUser.uid}.jpg"
        val storageReference = FirebaseStorage.getInstance().reference.child("images/$imageFileName")
        storageReference.putFile(imageUri)
            .addOnSuccessListener { taskSnapshot ->
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    val docRef = db.collection("users").document(currentUser.uid)
                    docRef.update("profileImageUrl", imageUrl)
                    fetchUserData()
                }
            }
            .addOnFailureListener { e ->
                // Error al subir la imagen a Storage
            }

    }

    fun uploadImageFromCamara(imageBitmap: Bitmap) {
        val imageFileName = "${currentUser.uid}.jpg"
        val storageReference = FirebaseStorage.getInstance().reference.child("images/$imageFileName.jpg")
        // Comprime la imagen y convierte en un arreglo de bytes
        val byteArrayOutputStream = ByteArrayOutputStream()
        imageBitmap.compress(Bitmap.CompressFormat.JPEG, 100, byteArrayOutputStream)
        val data = byteArrayOutputStream.toByteArray()
        storageReference.putBytes(data)
            .addOnSuccessListener { taskSnapshot ->
                storageReference.downloadUrl.addOnSuccessListener { uri ->
                    val imageUrl = uri.toString()
                    val docRef = db.collection("users").document(currentUser.uid)
                    docRef.update("profileImageUrl", imageUrl)
                    fetchUserData()
                }
            }
            .addOnFailureListener { e ->
                // Error al subir la imagen a Storage
            }
    }


}