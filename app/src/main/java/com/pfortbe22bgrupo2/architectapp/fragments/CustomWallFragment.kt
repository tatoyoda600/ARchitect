package com.pfortbe22bgrupo2.architectapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.pfortbe22bgrupo2.architectapp.adapters.CustomWallFirestoreAdapter
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentCustomWallBinding
import com.pfortbe22bgrupo2.architectapp.listeners.DeleteUserSavedDesign
import com.pfortbe22bgrupo2.architectapp.viewModels.CustomWallViewModel

class CustomWallFragment : Fragment(), DeleteUserSavedDesign {

    companion object {
        fun newInstance() = CustomWallFragment()
    }

    private lateinit var customWallViewModel: CustomWallViewModel
    private lateinit var binding: FragmentCustomWallBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomWallBinding.inflate(inflater,container,false)
        customWallViewModel = ViewModelProvider(this).get(CustomWallViewModel::class.java)

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        getCustomWallList()

    }

    private fun initRecyclerView(){
        binding.customWallRecyclerView.setHasFixedSize(true)
        binding.customWallRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun  getCustomWallList() {
        initRecyclerView()
        customWallViewModel.getCustomWallList()
        customWallViewModel.customWallOptions.observe(viewLifecycleOwner, Observer { options ->
            if (options.owner != null) {
                // VER......
                binding.emptyCustomWallsTextView.visibility = View.VISIBLE
            } else {
                val adapter = CustomWallFirestoreAdapter(options, this@CustomWallFragment)
                binding.customWallRecyclerView.adapter = adapter
                adapter.startListening()
            }
        })
    }

    override fun deleteSavedDesign(savedDesignId: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmación")
        builder.setMessage("¿Estás seguro de que deseas eliminar este diseño guardado?")
        builder.setPositiveButton("Si"){dialog,which ->
            customWallViewModel.deleteSavedDesign(savedDesignId)
        }
        builder.create().show()
    }



}