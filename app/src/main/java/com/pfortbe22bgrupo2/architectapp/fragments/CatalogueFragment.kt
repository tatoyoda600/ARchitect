package com.pfortbe22bgrupo2.architectapp.fragments


import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment

import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pfortbe22bgrupo2.architectapp.activities.CatalogueActivity
import com.pfortbe22bgrupo2.architectapp.adapters.FurnitureAdapter

import com.pfortbe22bgrupo2.architectapp.databinding.FragmentCatalogueBinding
import com.pfortbe22bgrupo2.architectapp.listeners.ShowDetailsFurniture
import com.pfortbe22bgrupo2.architectapp.models.Furniture
import com.pfortbe22bgrupo2.architectapp.viewModels.CatalogueDetailsViewModel



class CatalogueFragment(): Fragment(), ShowDetailsFurniture {
    companion object {
        fun newInstance() = CatalogueFragment()
    }

    private lateinit var furnitures: MutableList<Furniture>

    private lateinit var binding: FragmentCatalogueBinding
    private lateinit var furnitureAdapter: FurnitureAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager

    // Variable para almacenar el ViewModel compartido
    lateinit var viewModel: CatalogueDetailsViewModel

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
        viewModel.furnitureList.observe(viewLifecycleOwner) { furnitureList ->
            if(furnitureList.isNotEmpty()) {
                this.furnitures = furnitureList.toMutableList()
                initRecyclerView()
            }
        }
    }

    private fun initRecyclerView(){
        binding.catalogueRecyclerView.setHasFixedSize(true)
        furnitureAdapter = FurnitureAdapter(context, this.furnitures, this)
        linearLayoutManager = LinearLayoutManager(context)
        binding.catalogueRecyclerView.layoutManager = linearLayoutManager
        binding.catalogueRecyclerView.adapter = furnitureAdapter
    }


    private fun filterDataByCategory(category:String) {
        val filteredList = this.furnitures.filter{ item -> item.category.lowercase() == category.lowercase() }
        furnitureAdapter.updatesFurnitures(filteredList.toMutableList())
    }

    override fun showDetails(furniture: Furniture) {
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