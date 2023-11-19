package com.pfortbe22bgrupo2.architectapp.fragments

import com.pfortbe22bgrupo2.architectapp.entities.FurnitureModelData


import android.content.Context

import android.content.Intent
import android.net.Uri

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.View.VISIBLE
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.navigation.fragment.findNavController
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.pfortbe22bgrupo2.architectapp.data.HotBarSingleton
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentDetailsBinding
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.activities.CatalogueActivity
import com.pfortbe22bgrupo2.architectapp.models.Furniture
import com.pfortbe22bgrupo2.architectapp.viewModels.CatalogueDetailsViewModel


class DetailsFragment: Fragment() {

    companion object {
        fun newInstance() = DetailsFragment()
    }

    private lateinit var binding: FragmentDetailsBinding

    private lateinit var context : Context

    private val db = Firebase.firestore
    private lateinit var auth: FirebaseAuth
    private lateinit var currentUser: FirebaseUser

    // Variable para almacenar el ViewModel compartido
    //lateinit var viewModel: CatalogueDetailsViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        context = requireContext()
        binding = FragmentDetailsBinding.inflate(inflater, container,false)
        //viewModel = (activity as CatalogueActivity).tuViewModel
        auth = Firebase.auth
        currentUser = auth.currentUser!!

        //Boton para eliminar prod de BD (Solo lo pueden ver los admin)
        val docRef = db.collection("users").document(currentUser.uid)
        var isAdmin = false

        val deleteProdByAdminBottom = binding.deleteArButton

        docRef.get().addOnCompleteListener {
            if (it.isSuccessful){
                isAdmin = it.result.data?.get("isAdmin") as Boolean
            }

            if(isAdmin){
                deleteProdByAdminBottom.visibility = VISIBLE
            }
        }


        return binding.root
    }

    override fun onStart() {
        super.onStart()

        val deleteProdByAdminBottom = binding.deleteArButton
        val furniture = DetailsFragmentArgs.fromBundle(requireArguments()).furnitureElement
        val name = binding.detailNameTextView
        val description = binding.detailDescriptionTextView
        name.text = furniture.name
        description.text = furniture.description
        Glide.with(requireContext()).load(furniture.imageUrl).into(binding.itemDetailImageViewId)
        binding.detailsArButton.setOnClickListener() {
            saveIntoHotBar(furniture.name!!, furniture.category!!)
            HotBarSingleton.hotBarItems.forEach {
                println("HotBar: ${it.first} - ${it.second}")
            }
            /*val action = DetailsFragmentDirections.actionDetailsFragmentToARTrackingTest()
            findNavController().navigate(action)*/
        }
        deleteProdByAdminBottom.setOnClickListener {
            setOnRemoveProdAction(furniture)
        }
        binding.redirectButton.setOnClickListener {
            val intent = Intent(Intent.ACTION_VIEW, Uri.parse(furniture.link))
            startActivity(intent)
        }
    }


    private fun saveIntoHotBar(name: String, category: String){
        HotBarSingleton.hotBarItems.add(Pair(category, name))
    }

    fun setOnRemoveProdAction(furniture: FurnitureModelData){
        val builder = AlertDialog.Builder(context)
        builder.setTitle(context.getString(R.string.remove_from_db_prod_title))
        builder.setMessage(context.getString(R.string.remove_from_db_prod_text))
        builder.setPositiveButton("Si") { dialog, which ->

            /*
            Si queremos eliminar el modelo de la BD original

            val url = "models/${furniture.nombre}.glb"
            val storageRef = Firebase.storage.reference
            val desertRef = storageRef.child("models/lacobraa.png")

            desertRef.delete().addOnSuccessListener {
                viewModel.removeElem(furniture.nombre)
                val action = DetailsFragmentDirections.actionDetailsFragmentToCatalogueFragment()
                this.findNavController().navigate(action)
            }
             */

/*            viewModel.removeElem(furniture.nombre)
            val action = DetailsFragmentDirections.actionDetailsFragmentToCatalogueFragment()
            this.findNavController().navigate(action)*/
        }
        builder.setNegativeButton("No") { dialog, which ->
            Log.i("DetailsFragment", "No")
        }
        val dialog = builder.create()
        dialog.show()
    }

}