package com.pfortbe22bgrupo2.architectapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.pfortbe22bgrupo2.architectapp.data.HotBarSingleton
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentDetailsBinding
import com.pfortbe22bgrupo2.architectapp.viewModels.DetailsViewModel

class DetailsFragment: Fragment() {

    companion object {
        fun newInstance() = DetailsFragment()
    }

    //private lateinit var detailsViewModel: DetailsViewModel
    private lateinit var binding: FragmentDetailsBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDetailsBinding.inflate(inflater, container,false)
        //detailsViewModel = ViewModelProvider(this).get(DetailsViewModel::class.java)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val furniture = DetailsFragmentArgs.fromBundle(requireArguments()).furnitureElement
        val name = binding.detailNameTextView
        val description = binding.detailDescriptionTextView
        name.text = furniture.nombre
        description.text = furniture.description
        Glide.with(requireContext()).load(furniture.urlImage).into(binding.itemDetailImageViewId)
        binding.detailsArButton.setOnClickListener() {
            saveIntoHotBar(furniture.nombre, furniture.category)
            HotBarSingleton.hotBarItems.forEach {
                println("HotBar: ${it.first} - ${it.second}")
            }
            /*val action = DetailsFragmentDirections.actionDetailsFragmentToARTrackingTest()
            findNavController().navigate(action)*/
        }
    }

    private fun saveIntoHotBar(name: String, category: String){
        HotBarSingleton.hotBarItems.add(Pair(category, name))
    }

}