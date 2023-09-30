package com.pfortbe22bgrupo2.architectapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.entities.Furniture
import com.pfortbe22bgrupo2.architectapp.entities.Post
import com.pfortbe22bgrupo2.architectapp.holders.FurnitureHolder
import com.pfortbe22bgrupo2.architectapp.holders.PostHolder
import com.pfortbe22bgrupo2.architectapp.listeners.ShowDetailsPost

class PostAdapter(
    private var postList : MutableList<Post>,
    private val showDetailsPost: ShowDetailsPost
) : RecyclerView.Adapter<PostHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PostHolder {
        val view =  LayoutInflater.from(parent.context).inflate(R.layout.post_item,parent,false)
        return (PostHolder(view))
    }

    override fun getItemCount(): Int {
        return postList.size
    }

    override fun onBindViewHolder(holder: PostHolder, position: Int) {
        holder.setPosteo(postList[position].posteo)

        holder.getCardLayout().setOnClickListener(){
            showDetailsPost.showDetails(postList[position])
        }
    }

    fun updatesPost(postList: List<Post>){
        this.postList = postList.toMutableList()
        notifyDataSetChanged()
    }
}