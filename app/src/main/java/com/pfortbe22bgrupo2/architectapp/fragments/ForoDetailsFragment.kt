package com.pfortbe22bgrupo2.architectapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import com.pfortbe22bgrupo2.architectapp.R

class ForoDetailsFragment : Fragment() {

    companion object {
        fun newInstance() = ForoDetailsFragment()
    }

    private lateinit var viewModel: ForoDetailsViewModel
    lateinit var v:View
    lateinit var foroDetailsTextView: TextView

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_foro_details, container, false)
        foroDetailsTextView = v.findViewById(R.id.post_details_textView)
        return v
    }

    override fun onStart() {
        super.onStart()
        val post = ForoDetailsFragmentArgs.fromBundle(requireArguments()).posteo
        foroDetailsTextView.text = post.posteo

    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(ForoDetailsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}