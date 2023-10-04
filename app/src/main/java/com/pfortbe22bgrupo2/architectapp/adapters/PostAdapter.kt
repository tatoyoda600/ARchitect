package com.pfortbe22bgrupo2.architectapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.databinding.FurnitureItemBinding
import com.pfortbe22bgrupo2.architectapp.databinding.PostItemBinding
import com.pfortbe22bgrupo2.architectapp.entities.Furniture
import com.pfortbe22bgrupo2.architectapp.entities.Post
import com.pfortbe22bgrupo2.architectapp.holders.FurnitureHolder
import com.pfortbe22bgrupo2.architectapp.holders.PostHolder
import com.pfortbe22bgrupo2.architectapp.listeners.ShowDetailsPost

class PostAdapter(
    private var postList: MutableList<Post>,
    private val showDetailsPost: ShowDetailsPost
): RecyclerView.Adapter<PostHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val binding = PostItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return (PostHolder(binding))
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.setPosteo(postList[position].posteo)

        holder.getCardLayout().setOnClickListener() {
            showDetailsPost.showDetails(postList[position])
        }
    }

    fun updatesPost(postList: List<Post>) {
        this.postList = postList.toMutableList()
        notifyDataSetChanged()
    }
}