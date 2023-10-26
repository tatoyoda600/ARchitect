package com.pfortbe22bgrupo2.architectapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.activities.CatalogoActivity
import com.pfortbe22bgrupo2.architectapp.adapters.FurnitureAdapter
import com.pfortbe22bgrupo2.architectapp.data.FurnitureList
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentCatalogueBinding
import com.pfortbe22bgrupo2.architectapp.entities.Product
import com.pfortbe22bgrupo2.architectapp.listeners.ShowDetailsFurniture
import com.pfortbe22bgrupo2.architectapp.models.Furniture
import com.pfortbe22bgrupo2.architectapp.viewModels.CatalogueViewModel


class CatalogueFragment: Fragment(), ShowDetailsFurniture {
    companion object {
        fun newInstance() = CatalogueFragment()
    }

    private lateinit var viewModel: CatalogueViewModel

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
        linearLayoutManager = LinearLayoutManager(context)
        binding.catalogueRecyclerView.layoutManager = linearLayoutManager
        furnitureAdapter = FurnitureAdapter(furnitures.furnitures, this)
        binding.catalogueRecyclerView.adapter = furnitureAdapter
    }

/*    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_search_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }*/

/*    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_option_filter1 -> {
                filterDataByCategory("living")
                startFiltering()
                true
            }
            R.id.menu_option_filter2 -> {
                filterDataByCategory("habitacion")
                startFiltering()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }*/

    private fun filterDataByCategory(category:String) {
        val filteredList = furnitures.furnitures.filter{ item -> item.category.lowercase() == category.lowercase() }
        furnitureAdapter.updatesFurnitures(filteredList)
    }

    override fun showDetails(furniture: Furniture) {
        val action = CatalogueFragmentDirections.actionCatalogueFragmentToDetailsFragment(furniture)
        this.findNavController().navigate(action)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CatalogueViewModel::class.java)
        // TODO: Use the ViewModel
    }

    private fun startFiltering() {
        (activity as? CatalogoActivity)?.setToolbarFiltering(true)
    }

    private fun finishFiltering() {
        (activity as? CatalogoActivity)?.setToolbarFiltering(false)
    }
}