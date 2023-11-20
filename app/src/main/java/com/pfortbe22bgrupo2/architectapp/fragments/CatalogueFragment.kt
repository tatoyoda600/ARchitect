package com.pfortbe22bgrupo2.architectapp.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment

import androidx.lifecycle.Observer

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pfortbe22bgrupo2.architectapp.activities.CatalogueActivity
import com.pfortbe22bgrupo2.architectapp.adapters.FurnitureAdapter

import com.pfortbe22bgrupo2.architectapp.databinding.FragmentCatalogueBinding
import com.pfortbe22bgrupo2.architectapp.entities.FurnitureModelData
import com.pfortbe22bgrupo2.architectapp.listeners.ShowDetailsFurniture

//import com.pfortbe22bgrupo2.architectapp.models.Furniture
//import com.pfortbe22bgrupo2.architectapp.viewModels.CatalogueDetailsViewModel

import com.pfortbe22bgrupo2.architectapp.viewModels.CatalogueViewModel



class CatalogueFragment: Fragment(), ShowDetailsFurniture {
    companion object {
        fun newInstance() = CatalogueFragment()
    }


    private lateinit var furnitures: MutableList<FurnitureModelData>


    lateinit var viewModel: CatalogueViewModel

    private lateinit var binding: FragmentCatalogueBinding
    private lateinit var furnitureAdapter: FurnitureAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCatalogueBinding.inflate(inflater, container,false)
        viewModel = (activity as CatalogueActivity).tuViewModel
        initFilter()

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        loadData()
        finishFiltering()
    }

    private fun initFilter(){
        binding.homeFilterButton.setOnClickListener{
            viewModel.loadFurnitureList()
        }
        binding.livingFilterButton.setOnClickListener{
            filterDataByCategory("living")
            startFiltering()
        }
        binding.roomFilterButton.setOnClickListener {

            filterDataByCategory("habitacion")
            startFiltering()
        }
        binding.kitchenFilterButton.setOnClickListener {

            filterDataByCategory("cocina")
            startFiltering()
        }
        binding.bathroomFilterButton.setOnClickListener {
            filterDataByCategory("baño")
            startFiltering()
        }
        binding.diningroomFilterButton.setOnClickListener {
            filterDataByCategory("comedor")
            startFiltering()
        }
        binding.outsideFilterButton.setOnClickListener {

            filterDataByCategory("exterior")
            startFiltering()
        }
    }


   private fun loadData() {
       val loadingCircle = binding.loadingView
       viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
           if (!isLoading) {
               loadingCircle.isVisible = false
               viewModel.furnitureOptions.observe(viewLifecycleOwner) { furnitureList ->
                   if (furnitureList.isNotEmpty()) {
                       this.furnitures = furnitureList.toMutableList()
                       initRecyclerView()
                   }
               }
           }
           else{
               loadingCircle.isVisible = true
           }
       }
    }


    private fun initRecyclerView(){
        binding.catalogueRecyclerView.setHasFixedSize(true)
        binding.catalogueRecyclerView.layoutManager = LinearLayoutManager(context)
        furnitureAdapter = FurnitureAdapter(this.furnitures, this)
        linearLayoutManager = LinearLayoutManager(context)
        binding.catalogueRecyclerView.layoutManager = linearLayoutManager
        binding.catalogueRecyclerView.adapter = furnitureAdapter
    }


    private fun filterDataByCategory(category:String) {

        viewModel.filterFurnitureByCategory(category)
        viewModel.furnitureOptions.observe(viewLifecycleOwner, Observer {
            furnitureAdapter.updatesFurnitures(it)
        })


    }

    override fun showDetails(furniture: FurnitureModelData) {
        val action = CatalogueFragmentDirections.actionCatalogueFragmentToDetailsFragment(furniture)
        this.findNavController().navigate(action)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
//        viewModel = ViewModelProvider(this).get(CatalogueDetailsViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun startFiltering() {
        (activity as? CatalogueActivity)?.setToolbarFiltering(true)
    }

    private fun finishFiltering() {
        (activity as? CatalogueActivity)?.setToolbarFiltering(false)
    }
}