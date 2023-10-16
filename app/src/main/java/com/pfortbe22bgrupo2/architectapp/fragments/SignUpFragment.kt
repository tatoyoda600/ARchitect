package com.pfortbe22bgrupo2.architectapp.fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentSignUpBinding
import com.pfortbe22bgrupo2.architectapp.viewModels.SignUpViewModel

class SignUpFragment: Fragment() {

    companion object {
        fun newInstance() = SignUpFragment()
    }

    private lateinit var viewModel: SignUpViewModel
    private lateinit var binding: FragmentSignUpBinding

    private lateinit var auth: FirebaseAuth
    val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSignUpBinding.inflate(inflater,container,false)
        auth = Firebase.auth
        return binding.root
    }

    override fun onStart() {
        super.onStart()
       binding.secondSignUpButton.setOnClickListener() {
           validateFields()
        }

        binding.resgisterPasswordTextInputLayout.setOnClickListener{
            binding.resgisterPasswordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }


    }

    private fun validateFields() {
        val email = binding.registerEmailEditText.text.toString()
        val userName = binding.registerUserNameEditText.text.toString()
        val password = binding.resgisterPasswordEditText.text.toString()
        if (!email.isEmpty() || !password.isEmpty() || !userName.isEmpty()) {
            if (userName.length >= 6) {
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (password.length >= 6){
                        registerUser()
                    }else{
                        Toast.makeText(requireActivity(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                    }
                }else{
                    Toast.makeText(requireContext(), "Correo electrónico inválido", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(requireContext(), "El nombre de usuario debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(requireActivity(), "Los capos son obligatorios", Toast.LENGTH_SHORT).show()
        }
    }

    private fun registerUser() {
        val email = binding.registerEmailEditText.text.toString()
        val password = binding.resgisterPasswordEditText.text.toString()
        val userName = binding.registerUserNameEditText.text.toString()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(ContentValues.TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    val userUid: String = user?.uid ?: ""
                    addUserToFirestore(email, userName, userUid)
                    goToLoginFragment()
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                    //Toast.makeText(requireActivity(), "Authentication failed.", Toast.LENGTH_SHORT,).show()
                    //updateUI(null)
                }
            }
    }

    private fun goToLoginFragment() {
        val action = SignUpFragmentDirections.actionSingUpFragmentToLoginFragment()
        findNavController().navigate(action)
    }

    private fun addUserToFirestore(email: String, userName: String, userUid: String) {
        val user = hashMapOf(
            "email" to email,
            "userName" to userName,
            "id" to userUid
        )
        val newUserRef = db.collection("users").document(userUid).set(user)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)
        // TODO: Use the ViewModel
    }
}