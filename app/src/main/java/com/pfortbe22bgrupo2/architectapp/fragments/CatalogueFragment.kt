package com.pfortbe22bgrupo2.architectapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.activities.CatalogoActivity
import com.pfortbe22bgrupo2.architectapp.adapters.FurnitureAdapter
import com.pfortbe22bgrupo2.architectapp.data.FurnitureList
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentCatalogueBinding
import com.pfortbe22bgrupo2.architectapp.entities.Furniture
import com.pfortbe22bgrupo2.architectapp.listeners.ShowDetailsFurniture


class CatalogueFragment : Fragment(), ShowDetailsFurniture {

    companion object {
        fun newInstance() = CatalogueFragment()
    }

    private lateinit var viewModel: CatalogueViewModel

    private lateinit var binding : FragmentCatalogueBinding
    lateinit var furnitureRecycler : RecyclerView
    private lateinit var furnitureAdapter : FurnitureAdapter
    var furnitures : FurnitureList = FurnitureList()
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCatalogueBinding.inflate(inflater,container,false)

        val toolbar : Toolbar = binding.catalogoSearchToolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.searchEditTextToolbar.addTextChangedListener { fornitureFilter ->
            startFiltering()
            val furnitureFiltered = furnitures.furnitures.filter {
                    furniture -> furniture.nombre.lowercase().contains(fornitureFilter.toString().lowercase())
            }
            furnitureAdapter.updatesFurnitures(furnitureFiltered)
        }
        return binding.root
    }


    override fun onStart() {
        super.onStart()
        furnitureRecycler = binding.catalogueRecyclerView
        furnitureRecycler.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        furnitureRecycler.layoutManager = linearLayoutManager
        furnitureAdapter = FurnitureAdapter(furnitures.furnitures, this)
        furnitureRecycler.adapter = furnitureAdapter
        finishFiltering()
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        inflater.inflate(R.menu.toolbar_search_menu, menu)
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
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
    }

    private fun filterDataByCategory(category:String){
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

    fun finishFiltering() {
        (activity as? CatalogoActivity)?.setToolbarFiltering(false)
    }



}