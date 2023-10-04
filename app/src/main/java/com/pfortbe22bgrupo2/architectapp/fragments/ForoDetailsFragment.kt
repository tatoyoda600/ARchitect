package com.pfortbe22bgrupo2.architectapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentForoDetailsBinding

class ForoDetailsFragment: Fragment() {

    companion object {
        fun newInstance() = ForoDetailsFragment()
    }

    private lateinit var viewModel: ForoDetailsViewModel

    private lateinit var binding: FragmentForoDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForoDetailsBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val post = ForoDetailsFragmentArgs.fromBundle(requireArguments()).posteo
        binding.postDetailsTextView.text = post.posteo
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ForoDetailsViewModel::class.java)
        // TODO: Use the ViewModel
    }
}