package com.pfortbe22bgrupo2.architectapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.adapters.FurnitureAdapter
import com.pfortbe22bgrupo2.architectapp.adapters.PostAdapter
import com.pfortbe22bgrupo2.architectapp.data.FurnitureList
import com.pfortbe22bgrupo2.architectapp.data.PostList
import com.pfortbe22bgrupo2.architectapp.entities.Post
import com.pfortbe22bgrupo2.architectapp.listeners.ShowDetailsPost

class ForoFragment : Fragment(), ShowDetailsPost {

    companion object {
        fun newInstance() = ForoFragment()
    }

    private lateinit var viewModel: ForoViewModel

    lateinit var v:View

    lateinit var postRecycler : RecyclerView
    private lateinit var postAdapter: PostAdapter
    var posts : PostList = PostList()
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_foro, container, false)
        postRecycler = v.findViewById(R.id.foro_recyclerView)
        return v
    }

    override fun onStart() {
        super.onStart()

        postRecycler.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        postRecycler.layoutManager = linearLayoutManager
        postAdapter = PostAdapter(posts.posts,this)
        postRecycler.adapter = postAdapter

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ForoViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun showDetails(posteo: Post) {
        val action = ForoFragmentDirections.actionForoFragmentToForoDetailsFragment(posteo)
        v.findNavController().navigate(action)

    }

}