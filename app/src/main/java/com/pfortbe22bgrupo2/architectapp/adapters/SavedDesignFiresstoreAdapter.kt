package com.pfortbe22bgrupo2.architectapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.pfortbe22bgrupo2.architectapp.databinding.SavedDesignItemBinding
import com.pfortbe22bgrupo2.architectapp.holders.SavedDesignHolder
import com.pfortbe22bgrupo2.architectapp.models.SavedDesign

class SavedDesignFiresstoreAdapter(
    private val options: FirestoreRecyclerOptions<SavedDesign>
): FirestoreRecyclerAdapter<SavedDesign,SavedDesignHolder>(options) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SavedDesignHolder {
        val binding = SavedDesignItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return (SavedDesignHolder(binding))
    }

    override fun onBindViewHolder(holder: SavedDesignHolder, position: Int, model: SavedDesign) {
        holder.setDescription(model.description)
    }
}