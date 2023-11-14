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
import com.pfortbe22bgrupo2.architectapp.adapters.CustomWallFirestoreAdapter
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentCustomWallBinding
import com.pfortbe22bgrupo2.architectapp.listeners.DeleteUserSavedDesign
import com.pfortbe22bgrupo2.architectapp.models.CustomWall
import com.pfortbe22bgrupo2.architectapp.viewModels.CustomWallViewModel

class CustomWallFragment : Fragment(), DeleteUserSavedDesign {

    companion object {
        fun newInstance() = CustomWallFragment()
    }

    private lateinit var viewModel: CustomWallViewModel
    private lateinit var binding: FragmentCustomWallBinding
    private lateinit var auth: FirebaseAuth
    private val db = Firebase.firestore


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentCustomWallBinding.inflate(inflater,container,false)
        auth = Firebase.auth
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
        val currentUser = auth.currentUser!!
        val rootRef = FirebaseFirestore.getInstance()
        val query = rootRef.collection("custom_walls").whereEqualTo("userId", currentUser.uid)
        initRecyclerView()
        query.get().addOnSuccessListener { documents ->
            if (documents.isEmpty || documents == null){
                binding.emptyCustomWallsTextView.visibility = View.VISIBLE
            }else{
                val options = FirestoreRecyclerOptions.Builder<CustomWall>()
                    .setQuery(query, CustomWall::class.java)
                    .build()
                val adapter = CustomWallFirestoreAdapter(options,this)
                adapter.startListening()
                binding.customWallRecyclerView.adapter = adapter
            }
        }
    }

    override fun deleteSavedDesign(savedDesignId: String) {
        val builder = AlertDialog.Builder(requireContext())
        builder.setTitle("Confirmación")
        builder.setMessage("¿Estás seguro de que deseas eliminar este diseño guardado?")
        builder.setPositiveButton("Si"){dialog,which ->
            db.collection("custom_walls").document(savedDesignId).delete()
        }
        builder.create().show()
    }



}