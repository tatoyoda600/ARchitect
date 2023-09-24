package com.pfortbe22bgrupo2.architectapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.pfortbe22bgrupo2.architectapp.R

class SingUpFragment : Fragment() {

    companion object {
        fun newInstance() = SingUpFragment()
    }

    private lateinit var viewModel: SingUpViewModel
    lateinit var v : View
    lateinit var singUpButton : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_sing_up, container, false)
        singUpButton = v.findViewById(R.id.second_singUpbutton)
        return v
    }

    override fun onStart() {
        super.onStart()
        singUpButton.setOnClickListener(){
            val action = SingUpFragmentDirections.actionSingUpFragmentToLoginFragment()
            v.findNavController().navigate(action)
        }
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SingUpViewModel::class.java)
        // TODO: Use the ViewModel
    }

}