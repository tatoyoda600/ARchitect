package com.pfortbe22bgrupo2.architectapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.pfortbe22bgrupo2.architectapp.databinding.SavedDesignItemBinding
import com.pfortbe22bgrupo2.architectapp.holders.SavedDesignHolder
import com.pfortbe22bgrupo2.architectapp.listeners.DeleteUserSavedDesign
import com.pfortbe22bgrupo2.architectapp.listeners.ShowSavedDesign
import com.pfortbe22bgrupo2.architectapp.models.SavedDesign

class SavedDesignFirestoreAdapter(
    private val options: FirestoreRecyclerOptions<SavedDesign>,
    private val deleteSavedDesign: DeleteUserSavedDesign,
    private val showSavedDesign: ShowSavedDesign
): FirestoreRecyclerAdapter<SavedDesign,SavedDesignHolder>(options) {

    private lateinit var binding: SavedDesignItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedDesignHolder {
        binding = SavedDesignItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return (SavedDesignHolder(binding))
    }

    override fun onBindViewHolder(holder: SavedDesignHolder, position: Int, model: SavedDesign) {
        holder.setName(model.name)
        //holder.setImage(model.image,binding.root)
        holder.getDeleteButtom().setOnClickListener(){
            deleteSavedDesign.deleteSavedDesign(model.id)
        }
        holder.getCardLayout().setOnClickListener(){
            showSavedDesign.showSavedDesign(model)
        }


    }
}