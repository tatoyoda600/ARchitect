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
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentSingUpBinding

class SingUpFragment : Fragment() {

    companion object {
        fun newInstance() = SingUpFragment()
    }

    private lateinit var viewModel: SingUpViewModel

    private lateinit var binding: FragmentSingUpBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSingUpBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.secondSingUpbutton.setOnClickListener(){
            val action = SingUpFragmentDirections.actionSingUpFragmentToLoginFragment()
            findNavController().navigate(action)
        }

        binding.singupImagenButton.setOnClickListener(){
            findNavController().navigateUp()
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SingUpViewModel::class.java)
        // TODO: Use the ViewModel
    }

}