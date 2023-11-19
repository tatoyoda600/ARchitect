package com.pfortbe22bgrupo2.architectapp.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider

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


    //private lateinit var furnitures: MutableList<Furniture>


    private lateinit var catalogueViewModel: CatalogueViewModel

    private lateinit var binding: FragmentCatalogueBinding
    private lateinit var furnitureAdapter: FurnitureAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    // Variable para almacenar el ViewModel compartido
    //lateinit var viewModel: CatalogueDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCatalogueBinding.inflate(inflater, container,false)
        //viewModel = (activity as CatalogueActivity).tuViewModel
        initFilter()

        catalogueViewModel = ViewModelProvider(this).get(CatalogueViewModel::class.java)

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        //loadData()
        initRecyclerView()
        finishFiltering()
    }

    private fun initFilter(){
        binding.livingFilterButton.setOnClickListener{
            //viewModel.initList()
            filterDataByCategory("living")
            startFiltering()
        }
        binding.roomFilterButton.setOnClickListener {
            //viewModel.initList()
            filterDataByCategory("habitacion")
            startFiltering()
        }
        binding.kitchenFilterButton.setOnClickListener {
            //viewModel.initList()
            filterDataByCategory("cocina")
            startFiltering()
        }
        binding.bathroomFilterButton.setOnClickListener {
            //viewModel.initList()
            filterDataByCategory("baño")
            startFiltering()
        }
        binding.diningroomFilterButton.setOnClickListener {
            //viewModel.initList()
            filterDataByCategory("comedor")
            startFiltering()
        }
        binding.outsideFilterButton.setOnClickListener {
            //viewModel.initList()
            filterDataByCategory("exterior")
            startFiltering()
        }
    }


/*    private fun loadData() {
        viewModel.furnitureList.observe(viewLifecycleOwner) { furnitureList ->
            if(furnitureList.isNotEmpty()) {
                this.furnitures = furnitureList.toMutableList()
                initRecyclerView()
            }
        }
    }*/


    private fun initRecyclerView(){
        binding.catalogueRecyclerView.setHasFixedSize(true)
        binding.catalogueRecyclerView.layoutManager = LinearLayoutManager(context)
        catalogueViewModel.getFurnitureList()
        catalogueViewModel.furnitureOptions.observe(viewLifecycleOwner, Observer {
            furnitureAdapter = FurnitureAdapter(it, this)
            linearLayoutManager = LinearLayoutManager(context)
            binding.catalogueRecyclerView.layoutManager = linearLayoutManager
            binding.catalogueRecyclerView.adapter = furnitureAdapter
        })
    }


    private fun filterDataByCategory(category:String) {

        //val filteredList = this.furnitures.filter{ item -> item.category.lowercase() == category.lowercase() }
        //furnitureAdapter.updatesFurnitures(filteredList.toMutableList())

        catalogueViewModel.filterFurnitureByCategory(category)
        catalogueViewModel.furnitureOptions.observe(viewLifecycleOwner, Observer {
            furnitureAdapter.updatesFurnitures(it)
        })
        //val filteredList = furnitures.furnitures.filter{ item -> item.category.lowercase() == category.lowercase() }


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