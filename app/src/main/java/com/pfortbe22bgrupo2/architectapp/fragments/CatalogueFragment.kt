package com.pfortbe22bgrupo2.architectapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.NavigationUI
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.adapters.FurnitureAdapter
import com.pfortbe22bgrupo2.architectapp.data.FurnitureList
import com.pfortbe22bgrupo2.architectapp.entities.Furniture
import com.pfortbe22bgrupo2.architectapp.listeners.ShowDetailsFurniture


class CatalogueFragment : Fragment(), ShowDetailsFurniture {

    companion object {
        fun newInstance() = CatalogueFragment()
    }

    private lateinit var viewModel: CatalogueViewModel
    lateinit var v :View

    lateinit var furnitureRecycler : RecyclerView
    private lateinit var furnitureAdapter : FurnitureAdapter
    var furnitures : FurnitureList = FurnitureList()
    private lateinit var linearLayoutManager: LinearLayoutManager



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_catalogue, container, false)
        furnitureRecycler = v.findViewById(R.id.catalogue_recyclerView)
        return v
    }


    override fun onStart() {
        super.onStart()

        furnitureRecycler.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        furnitureRecycler.layoutManager = linearLayoutManager
        furnitureAdapter = FurnitureAdapter(furnitures.furnitures, this)
        furnitureRecycler.adapter = furnitureAdapter
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CatalogueViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun showDetails(furniture: Furniture) {
        val action = CatalogueFragmentDirections.actionCatalogueFragmentToDetailsFragment(furniture)
        v.findNavController().navigate(action)
    }

}