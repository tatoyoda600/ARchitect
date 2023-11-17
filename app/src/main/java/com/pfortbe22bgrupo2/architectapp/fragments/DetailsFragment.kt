package com.pfortbe22bgrupo2.architectapp.fragments

import android.content.Context
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.pfortbe22bgrupo2.architectapp.data.HotBarSingleton
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentDetailsBinding
import com.pfortbe22bgrupo2.architectapp.viewModels.DetailsViewModel

class DetailsFragment: Fragment() {

    companion object {
        fun newInstance() = DetailsFragment()
    }

    private lateinit var viewModel: DetailsViewModel

    private lateinit var binding: FragmentDetailsBinding

    private lateinit var context : Context

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        context = requireContext()
        binding = FragmentDetailsBinding.inflate(inflater, container,false)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val furniture = DetailsFragmentArgs.fromBundle(requireArguments()).furnitureElement
        val nombre = binding.detailNameTextView
        val descripcion = binding.detailDescriptionTextView
        val image = binding.itemDetailImageViewId
        nombre.text = furniture.nombre
        descripcion.text = furniture.description
        Glide.with(context).load(furniture.urlImage).into(binding.itemDetailImageViewId)
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

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DetailsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}