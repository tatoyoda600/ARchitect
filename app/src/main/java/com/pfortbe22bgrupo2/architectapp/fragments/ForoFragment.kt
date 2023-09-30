package com.pfortbe22bgrupo2.architectapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.core.widget.addTextChangedListener
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.activities.CatalogoActivity
import com.pfortbe22bgrupo2.architectapp.adapters.FurnitureAdapter
import com.pfortbe22bgrupo2.architectapp.adapters.PostAdapter
import com.pfortbe22bgrupo2.architectapp.data.FurnitureList
import com.pfortbe22bgrupo2.architectapp.data.PostList
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentForoBinding
import com.pfortbe22bgrupo2.architectapp.entities.Post
import com.pfortbe22bgrupo2.architectapp.listeners.ShowDetailsPost

class ForoFragment : Fragment(), ShowDetailsPost {

    companion object {
        fun newInstance() = ForoFragment()
    }

    private lateinit var viewModel: ForoViewModel

    private lateinit var binding: FragmentForoBinding
    lateinit var postRecycler : RecyclerView
    private lateinit var postAdapter: PostAdapter
    var posts : PostList = PostList()
    private lateinit var linearLayoutManager: LinearLayoutManager

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentForoBinding.inflate(inflater,container,false)
        val toolbar : Toolbar = binding.foroSearchToolbar
        (requireActivity() as AppCompatActivity).setSupportActionBar(toolbar)
        setHasOptionsMenu(true)
        (requireActivity() as AppCompatActivity).supportActionBar?.setDisplayShowTitleEnabled(false)

        binding.foroSearchEditTextToolbar.addTextChangedListener { postFilter ->
            startFiltering()
            val postFiltered = posts.posts.filter {
                post -> post.posteo.lowercase().contains(postFilter.toString().lowercase())
            }
            postAdapter.updatesPost(postFiltered)
        }
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        postRecycler = binding.foroRecyclerView
        postRecycler.setHasFixedSize(true)
        linearLayoutManager = LinearLayoutManager(context)
        postRecycler.layoutManager = linearLayoutManager
        postAdapter = PostAdapter(posts.posts,this)
        postRecycler.adapter = postAdapter
        finishFiltering()

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ForoViewModel::class.java)
        // TODO: Use the ViewModel
    }

    override fun showDetails(posteo: Post) {
        val action = ForoFragmentDirections.actionForoFragmentToForoDetailsFragment(posteo)
        findNavController().navigate(action)
    }

    private fun startFiltering() {
        (activity as? CatalogoActivity)?.setToolbarFiltering(true)
    }

    fun finishFiltering() {
        (activity as? CatalogoActivity)?.setToolbarFiltering(false)
    }

}