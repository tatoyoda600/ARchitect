package com.pfortbe22bgrupo2.architectapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.bumptech.glide.Glide
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentSavedDesignDetailsBinding
import com.pfortbe22bgrupo2.architectapp.viewModels.SavedDesignDetailsViewModel

class SavedDesignDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = SavedDesignDetailsFragment()
    }

    private lateinit var viewModel: SavedDesignDetailsViewModel
    private lateinit var binding: FragmentSavedDesignDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentSavedDesignDetailsBinding.inflate(inflater,container,false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val savedDesign = SavedDesignDetailsFragmentArgs.fromBundle(requireArguments()).savedDesignSelected
        Glide.with(requireContext()).load(savedDesign.image).into(binding.savedDesignImageView)
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SavedDesignDetailsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}