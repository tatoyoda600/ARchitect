package com.pfortbe22bgrupo2.architectapp.fragments

import android.content.ContentValues.TAG
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
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentLoginBinding
import com.pfortbe22bgrupo2.architectapp.viewModels.LoginViewModel

class LoginFragment: Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: FragmentLoginBinding
    private lateinit var auth: FirebaseAuth

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container,false)
        auth = Firebase.auth
        return binding.root
    }

    /*private fun valinfo() {
        binding.logingEmailEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                TODO("Not yet implemented")
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Realizar validaciones en tiempo real para el campo de correo electrónico
                val email = s.toString()
                if (!Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                    binding.logingEmailEditText.error = "Correo electrónico inválido"
                } else {
                    binding.logingEmailEditText.error = null
                }
            }

            override fun afterTextChanged(s: Editable?) {
                TODO("Not yet implemented")
            }
    }*/

    override fun onStart() {
        super.onStart()
        binding.secondLoginButton.setOnClickListener() {
            validateFields()
        }
        binding.loginPasswordTextInputLayout.setOnClickListener{
            binding.loginPasswordEditText.inputType = android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

    }


    private fun validateFields() {
        val email = binding.logingEmailEditText.text.toString()
        val password = binding.loginPasswordEditText.text.toString()
        if (!email.isEmpty() || !password.isEmpty()) {
            if (Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                if (password.length >= 6) {
                    loginUser()
                }else{
                    Toast.makeText(requireContext(), "La contraseña debe tener al menos 6 caracteres", Toast.LENGTH_SHORT).show()
                }
            }else{
                Toast.makeText(requireContext(), "Correo electrónico inválido", Toast.LENGTH_SHORT).show()
            }
        }else{
            Toast.makeText(requireActivity(), "El campo Email y Contaseña son necesarios", Toast.LENGTH_SHORT).show()
        }
    }

    private fun loginUser() {
        val email = binding.logingEmailEditText.text.toString()
        val password = binding.loginPasswordEditText.text.toString()
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(requireActivity()) { task ->
                if (task.isSuccessful) {
                    // Sign in success, update UI with the signed-in user's information
                    Log.d(TAG, "signInWithEmail:success")
                    val user = auth.currentUser
                    goToCatalogoActivity()
                    //updateUI(user)
                } else {
                    // If sign in fails, display a message to the user.
                    Log.w(TAG, "signInWithEmail:failure", task.exception)
                    Toast.makeText(requireActivity(), "Email o Contraseñas incorrectas", Toast.LENGTH_SHORT).show()
                    //updateUI(null)
                }
            }
    }

    private fun goToCatalogoActivity() {
        val action = LoginFragmentDirections.actionLoginFragmentToCatalogoActivity()
        findNavController().navigate(action)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        // TODO: Use the ViewModel
    }
}