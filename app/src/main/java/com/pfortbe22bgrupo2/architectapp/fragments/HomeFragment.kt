package com.pfortbe22bgrupo2.architectapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentHomeBinding
import com.pfortbe22bgrupo2.architectapp.viewModels.HomeViewModel

class HomeFragment: Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
    private lateinit var binding: FragmentHomeBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentHomeBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
/*        binding.firstSignUpButton.setOnClickListener() {
            val action = HomeFragmentDirections.actionHomeFragmentToSingUpFragment()
            findNavController().navigate(action)
        }
        binding.firstLoginButton.setOnClickListener() {
            val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
            findNavController().navigate(action)
        }*/

        binding.customButton.setOnClickListener {
            val x = view?.x
            val width = view?.width

            if (x!! < width!! / 2) {
                val action = HomeFragmentDirections.actionHomeFragmentToSingUpFragment()
                findNavController().navigate(action)
                // Clic en la parte izquierda del botón
                // Realiza la acción de registro aquí
            } else {
                val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
                findNavController().navigate(action)
                // Clic en la parte derecha del botón
                // Realiza la acción de inicio de sesión aquí
            }
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        // TODO: Use the ViewModel
    }

}