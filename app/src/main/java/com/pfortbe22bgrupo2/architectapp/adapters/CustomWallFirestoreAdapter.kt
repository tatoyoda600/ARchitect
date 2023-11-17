package com.pfortbe22bgrupo2.architectapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import com.firebase.ui.firestore.FirestoreRecyclerAdapter
import com.firebase.ui.firestore.FirestoreRecyclerOptions
import com.pfortbe22bgrupo2.architectapp.databinding.CustomWallItemBinding
import com.pfortbe22bgrupo2.architectapp.holders.CustomWallHolder
import com.pfortbe22bgrupo2.architectapp.listeners.DeleteUserSavedDesign
import com.pfortbe22bgrupo2.architectapp.models.CustomWall

class CustomWallFirestoreAdapter(
    private val options: FirestoreRecyclerOptions<CustomWall>,
    private val deleteUserWall: DeleteUserSavedDesign
): FirestoreRecyclerAdapter<CustomWall,CustomWallHolder>(options) {

    private lateinit var binding: CustomWallItemBinding
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CustomWallHolder {
        binding = CustomWallItemBinding.inflate(LayoutInflater.from(parent.context),parent,false)
        return (CustomWallHolder(binding))
    }

    override fun onBindViewHolder(holder: CustomWallHolder, position: Int, model: CustomWall) {
        //holder.setDescription(model.description)
        holder.setImage(model.image,binding.root)

        holder.getDeleteButtom().setOnClickListener(){
            deleteUserWall.deleteSavedDesign(model.id)
        }
    }
}