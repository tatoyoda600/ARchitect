package com.pfortbe22bgrupo2.architectapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.adapters.SavedDesignFiresstoreAdapter
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentDesignsSavedBinding
import com.pfortbe22bgrupo2.architectapp.listeners.DeleteUserSavedDesign
import com.pfortbe22bgrupo2.architectapp.models.SavedDesign
import com.pfortbe22bgrupo2.architectapp.viewModels.SavedDesignsViewModel

class SavedDesignsFragment : Fragment(), DeleteUserSavedDesign {

    companion object {
        fun newInstance() = SavedDesignsFragment()
    }

    private lateinit var viewModel: SavedDesignsViewModel
    private lateinit var binding: FragmentDesignsSavedBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDesignsSavedBinding.inflate(inflater,container,false)
        //initRecyclerView()
        auth = Firebase.auth
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
        val currentUser = auth.currentUser!!
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef.collection("saved_designs").whereEqualTo("userId", currentUser.uid)
        initRecyclerView()
        query.get().addOnSuccessListener { documents ->
            if (documents.isEmpty || documents == null) {
                binding.savedDesignRecyclerView.visibility = View.GONE
                binding.emptySavedDesignTextView.visibility = View.VISIBLE
            }else{
                val options = FirestoreRecyclerOptions.Builder<SavedDesign>()
                    .setQuery(query,SavedDesign::class.java)
                    .build()
                val adapter = SavedDesignFiresstoreAdapter(options,this)
                adapter.startListening()
                binding.savedDesignRecyclerView.adapter = adapter
            }
        }
    }

    override fun deleteSavedDesign(savedDesignId: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmación")
        builder.setMessage("¿Estás seguro de que deseas eliminar este diseño guardado?")
        builder.setPositiveButton("Si"){dialog,which ->
            db.collection("saved_designs").document(savedDesignId).delete()
        }
        builder.create().show()
    }

}