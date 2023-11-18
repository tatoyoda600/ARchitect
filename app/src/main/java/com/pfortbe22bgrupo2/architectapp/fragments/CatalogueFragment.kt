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
import com.pfortbe22bgrupo2.architectapp.data.FurnitureList
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentCatalogueBinding
import com.pfortbe22bgrupo2.architectapp.listeners.ShowDetailsFurniture
import com.pfortbe22bgrupo2.architectapp.models.Furniture


class CatalogueFragment: Fragment(), ShowDetailsFurniture {
    companion object {
        fun newInstance() = CatalogueFragment()
    }

    //private lateinit var catalogueViewModel: CatalogueViewModel
    private lateinit var binding: FragmentCatalogueBinding
    private lateinit var furnitureAdapter: FurnitureAdapter
    private var furnitures: FurnitureList = FurnitureList()
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCatalogueBinding.inflate(inflater, container,false)
        initFilter()
        //catalogueViewModel = ViewModelProvider(this).get(CatalogueViewModel::class.java)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        initRecyclerView()
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

    private fun initRecyclerView(){
        binding.catalogueRecyclerView.setHasFixedSize(true)
        furnitureAdapter = FurnitureAdapter(context, furnitures.furnitures, this)
        linearLayoutManager = LinearLayoutManager(context)
        binding.catalogueRecyclerView.layoutManager = linearLayoutManager
        binding.catalogueRecyclerView.adapter = furnitureAdapter
    }


    private fun filterDataByCategory(category:String) {
        val filteredList = furnitures.furnitures.filter{ item -> item.category.lowercase() == category.lowercase() }
        furnitureAdapter.updatesFurnitures(filteredList)
    }

    override fun showDetails(furniture: Furniture) {
        val action = CatalogueFragmentDirections.actionCatalogueFragmentToDetailsFragment(furniture)
        this.findNavController().navigate(action)
    }

    private fun startFiltering() {
        (activity as? CatalogueActivity)?.setToolbarFiltering(true)
    }

    private fun finishFiltering() {
        (activity as? CatalogueActivity)?.setToolbarFiltering(false)
    }
}