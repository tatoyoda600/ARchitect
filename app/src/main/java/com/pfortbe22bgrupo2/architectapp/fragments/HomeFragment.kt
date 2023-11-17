package com.pfortbe22bgrupo2.architectapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentHomeBinding

class HomeFragment: Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    //private lateinit var homeViewModel: HomeViewModel
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

        checkForLoadFragment()

        binding.firstSignUpButton.setOnClickListener() {
          val action = HomeFragmentDirections.actionHomeFragmentToSignUpFragment()
          findNavController().navigate(action)
        }
        binding.firstLogInButton.setOnClickListener() {
            navToLoginFragment()
        }

    }

    private fun checkForLoadFragment() {
        val fragmentToLoad = activity?.intent?.getStringExtra("fragmentToLoad")
        if (fragmentToLoad != null){
            navToLoginFragment()
        }
    }

    private fun navToLoginFragment() {
        val action = HomeFragmentDirections.actionHomeFragmentToLoginFragment()
        findNavController().navigate(action)
    }


}