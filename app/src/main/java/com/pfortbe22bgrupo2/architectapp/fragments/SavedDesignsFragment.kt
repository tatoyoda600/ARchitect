package com.pfortbe22bgrupo2.architectapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import com.pfortbe22bgrupo2.architectapp.adapters.FurnitureAdapter
import com.pfortbe22bgrupo2.architectapp.adapters.SavedDesignAdapter
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentDesignsSavedBinding
import com.pfortbe22bgrupo2.architectapp.models.SavedDesign
import com.pfortbe22bgrupo2.architectapp.viewModels.SavedDesignsViewModel

class SavedDesignsFragment : Fragment() {

    companion object {
        fun newInstance() = SavedDesignsFragment()
    }

    private lateinit var viewModel: SavedDesignsViewModel

    private lateinit var binding: FragmentDesignsSavedBinding
    private lateinit var savedDesignAdapter: SavedDesignAdapter
    private lateinit var linearLayoutManager: LinearLayoutManager
    private var savedDesigns: MutableList<SavedDesign> = mutableListOf()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDesignsSavedBinding.inflate(inflater,container,false)
        harcodearMiniListaParaMostarAlgo()
        return binding.root

    }

    private fun harcodearMiniListaParaMostarAlgo() {
        savedDesigns.add(SavedDesign("Diseño Guardado 1"))
        savedDesigns.add(SavedDesign("Diseño Guardado 2"))
        savedDesigns.add(SavedDesign("Diseño Guardado 3"))
        savedDesigns.add(SavedDesign("Diseño Guardado 4"))
        savedDesigns.add(SavedDesign("Diseño Guardado 5"))
    }

    override fun onStart() {
        super.onStart()
        initRecyclerView()
    }

    private fun initRecyclerView(){
        binding.savedDesignRecyclerView.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        binding.savedDesignRecyclerView.layoutManager = linearLayoutManager
        savedDesignAdapter = SavedDesignAdapter(savedDesigns)
        binding.savedDesignRecyclerView.adapter = savedDesignAdapter
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SavedDesignsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}