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
import com.pfortbe22bgrupo2.architectapp.adapters.CustomWallFirestoreAdapter
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentCustomWallBinding
import com.pfortbe22bgrupo2.architectapp.models.CustomWall
import com.pfortbe22bgrupo2.architectapp.viewModels.CustomWallViewModel

class CustomWallFragment : Fragment() {

    companion object {
        fun newInstance() = CustomWallFragment()
    }

    private lateinit var viewModel: CustomWallViewModel
    private lateinit var binding: FragmentCustomWallBinding


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomWallBinding.inflate(inflater,container,false)
        initRecyclerView()
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        getFirebaseList()
    }

    private fun initRecyclerView(){
        binding.customWallRecyclerView.setHasFixedSize(true)
        binding.customWallRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    fun  getFirebaseList() {
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef.collection("custom_walls").orderBy("description")
        val options = FirestoreRecyclerOptions.Builder<CustomWall>()
            .setQuery(query, CustomWall::class.java)
            .build()
        val adapter = CustomWallFirestoreAdapter(options)
        adapter.startListening()
        binding.customWallRecyclerView.adapter = adapter
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(CustomWallViewModel::class.java)
        // TODO: Use the ViewModel
    }

}