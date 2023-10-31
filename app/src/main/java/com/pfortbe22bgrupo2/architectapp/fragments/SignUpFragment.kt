package com.pfortbe22bgrupo2.architectapp.fragments

import android.content.ContentValues
import android.os.Bundle
import android.util.Log
import android.util.Patterns
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
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
        val repeatPassword = binding.resgisterRepeatPasswordEditText.text.toString()
        if (!email.isEmpty() || !password.isEmpty() || !userName.isEmpty()) {
            if (userName.length >= 6) {
                if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    if (password.length >= 6){
                        if(password == repeatPassword){
                            registerUser()
                        }else{
                            binding.resgisterPasswordEditText.error = "Contraseñas Diferentes"
                            binding.resgisterRepeatPasswordEditText.error = "Contraseñas Diferentes"
                        }
                    }else{
                        binding.resgisterPasswordEditText.error = "6 Caracteres como Minimo"
                    }
                }else{
                    binding.registerEmailEditText.error = "Correo electrónico inválido"
                }
            }else{
                binding.registerUserNameEditText.error = "6 Caracteres como Minimo"
            }
        }else{
            binding.registerUserNameEditText.error = "Campo Obligatorio"
            binding.registerEmailEditText.error = "Campo Obligatorio"
            binding.resgisterPasswordEditText.error = "Campo Obligatorio"
            binding.resgisterRepeatPasswordEditText.error = "Campo Obligatorio"
        }
    }

    private fun registerUser() {
        val email = binding.registerEmailEditText.text.toString()
        val password = binding.resgisterPasswordEditText.text.toString()
        val userName = binding.registerUserNameEditText.text.toString()
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    Log.d(ContentValues.TAG, "createUserWithEmail:success")
                    val user = auth.currentUser
                    val userUid: String = user?.uid ?: ""
                    addUserToFirestore(email, userName, userUid)
                    goToLoginFragment()
                } else {
                    Log.w(ContentValues.TAG, "createUserWithEmail:failure", task.exception)
                }
            }
    }

    private fun goToLoginFragment() {
        val action = SignUpFragmentDirections.actionSignUpFragmentToCatalogoActivity()
        findNavController().navigate(action)
    }

    private fun addUserToFirestore(email: String, userName: String, userUid: String) {
        val user = hashMapOf(
            "email" to email,
            "userName" to userName,
            "id" to userUid,
            "isAdmin" to false
        )
        db.collection("users").document(userUid).set(user)
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SignUpViewModel::class.java)
        // TODO: Use the ViewModel
    }
}