package com.pfortbe22bgrupo2.architectapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageButton
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentLoginBinding

class LoginFragment: Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel
    private lateinit var binding: FragmentLoginBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentLoginBinding.inflate(inflater, container,false)
        //loginButton = v.findViewById(R.id.second_login_button)
        //imagenButton = v.findViewById(R.id.login_imagen_button)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.secondLoginButton.setOnClickListener() {
            val action = LoginFragmentDirections.actionLoginFragmentToCatalogoActivity()
            findNavController().navigate(action)
        }

        binding.loginImagenButton.setOnClickListener() {
            findNavController().navigateUp()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        // TODO: Use the ViewModel
    }
}