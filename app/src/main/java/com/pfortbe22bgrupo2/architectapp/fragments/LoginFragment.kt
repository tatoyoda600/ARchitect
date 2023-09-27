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

class LoginFragment : Fragment() {

    companion object {
        fun newInstance() = LoginFragment()
    }

    private lateinit var viewModel: LoginViewModel

    lateinit var v : View
    lateinit var loginButton : Button
    lateinit var imagenButton : ImageButton

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_login, container, false)
        loginButton = v.findViewById(R.id.second_login_button)
        imagenButton = v.findViewById(R.id.login_imagen_button)
        return v
    }

    override fun onStart() {
        super.onStart()
        loginButton.setOnClickListener(){
            val action = LoginFragmentDirections.actionLoginFragmentToCatalogoActivity()
            v.findNavController().navigate(action)
        }

        imagenButton.setOnClickListener(){
            v.findNavController().navigateUp()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(LoginViewModel::class.java)
        // TODO: Use the ViewModel
    }



}