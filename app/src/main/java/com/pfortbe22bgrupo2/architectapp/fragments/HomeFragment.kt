package com.pfortbe22bgrupo2.architectapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentHomeBinding
import com.pfortbe22bgrupo2.architectapp.viewModels.HomeViewModel

class HomeFragment: Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding
    //ahora esta harkcodeado a morir
    var isLogin: Boolean = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.firstSignUpButton.setOnClickListener() {
            val action = HomeFragmentDirections.actionHomeFragmentToSignUpFragment()
            findNavController().navigate(action)
        }
        binding.firstLoginButton.setOnClickListener() {
            val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
            findNavController().navigate(action)
        }

        //aca habria que guardar referencia al usuario, si esta logeado o no y en base a eso
        //direccionar al catalogo o al home
        if (isLogin) {
            val action = HomeFragmentDirections.actionHomeFragmentToCatalogoActivity()
            findNavController().navigate(action)
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        // TODO: Use the ViewModel
    }

}