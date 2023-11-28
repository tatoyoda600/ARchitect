package com.pfortbe22bgrupo2.architectapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pfortbe22bgrupo2.architectapp.adapters.SavedDesignFirestoreAdapter
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentDesignsSavedBinding
import com.pfortbe22bgrupo2.architectapp.listeners.DeleteUserSavedDesign
import com.pfortbe22bgrupo2.architectapp.listeners.ShowSavedDesign
import com.pfortbe22bgrupo2.architectapp.models.SavedDesign
import com.pfortbe22bgrupo2.architectapp.viewModels.SavedDesignsViewModel

class SavedDesignsFragment : Fragment(), DeleteUserSavedDesign, ShowSavedDesign {

    companion object {
        fun newInstance() = SavedDesignsFragment()
    }

    private lateinit var savedDesignViewModel: SavedDesignsViewModel
    private lateinit var binding: FragmentDesignsSavedBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDesignsSavedBinding.inflate(inflater,container,false)
        savedDesignViewModel = ViewModelProvider(this).get(SavedDesignsViewModel::class.java)
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        getSavedDesignList()
    }

    private fun initRecyclerView(){
        binding.savedDesignRecyclerView.setHasFixedSize(true)
        binding.savedDesignRecyclerView.layoutManager = LinearLayoutManager(context)
    }

    private fun  getSavedDesignList() {
        initRecyclerView()
        savedDesignViewModel.getSavedDesignList()
        savedDesignViewModel.savedDesignOptions.observe(viewLifecycleOwner, Observer { designsList ->
            if (designsList == null) {
                binding.savedDesignRecyclerView.visibility = View.GONE
                binding.emptySavedDesignTextView.visibility = View.VISIBLE
            } else {
                val adapter = SavedDesignFirestoreAdapter(designsList, this, this)
                binding.savedDesignRecyclerView.adapter = adapter
                adapter.startListening()
            }
        })
    }

    override fun deleteSavedDesign(savedDesignId: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmación")
        builder.setMessage("¿Estás seguro de que deseas eliminar este diseño guardado?")
        builder.setPositiveButton("Si"){dialog,which ->
            savedDesignViewModel.deleteSavedDesign(savedDesignId)
        }
        builder.create().show()
    }


    override fun showSavedDesign(savedDesign: SavedDesign) {
        val action = DesignWallContainerFragmentDirections.actionDesignWallContainerFragmentToSavedDesignDetailsFragment(savedDesign)
        findNavController().navigate(action)
    }

}