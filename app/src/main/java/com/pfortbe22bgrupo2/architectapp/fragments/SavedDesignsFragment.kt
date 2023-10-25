package com.pfortbe22bgrupo2.architectapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.firestore.FirebaseFirestore
import com.pfortbe22bgrupo2.architectapp.adapters.SavedDesignFiresstoreAdapter
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentDesignsSavedBinding
import com.pfortbe22bgrupo2.architectapp.models.SavedDesign
import com.pfortbe22bgrupo2.architectapp.viewModels.SavedDesignsViewModel

class SavedDesignsFragment : Fragment() {

    companion object {
        fun newInstance() = SavedDesignsFragment()
    }

    private lateinit var viewModel: SavedDesignsViewModel
    private lateinit var binding: FragmentDesignsSavedBinding



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDesignsSavedBinding.inflate(inflater,container,false)
        initRecyclerView()
        return binding.root
    }



    override fun onStart() {
        super.onStart()
        getFirebaseList()
    }

    private fun initRecyclerView(){
        binding.savedDesignRecyclerView.setHasFixedSize(true)
        binding.savedDesignRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun  getFirebaseList() {
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef.collection("saved_designs").orderBy("description")
        val options = FirestoreRecyclerOptions.Builder<SavedDesign>()
            .setQuery(query,SavedDesign::class.java)
            .build()
        val adapter = SavedDesignFiresstoreAdapter(options)
        adapter.startListening()
        binding.savedDesignRecyclerView.adapter = adapter
    }


    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(SavedDesignsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}