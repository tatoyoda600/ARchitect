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

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: HomeViewModel
    lateinit var v : View
    lateinit var signUpButton : Button
    lateinit var loginButton : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_home, container, false)
        signUpButton = v.findViewById(R.id.firt_signUp_button)
        loginButton = v.findViewById(R.id.firt_login_button)
        return v
    }

    override fun onStart() {
        super.onStart()
        signUpButton.setOnClickListener(){
            val action = HomeFragmentDirections.actionHomeFragmentToSingUpFragment()
            //v.findNavController().navigate(action)
            v.findNavController().navigate(action)
        }
        loginButton.setOnClickListener(){
            val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
            //this.findNavController().navigate(action)
            v.findNavController().navigate(action)
        }

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(HomeViewModel::class.java)
        // TODO: Use the ViewModel
    }

}