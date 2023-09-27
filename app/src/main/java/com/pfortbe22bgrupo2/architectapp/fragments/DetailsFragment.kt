package com.pfortbe22bgrupo2.architectapp.fragments

import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.navigation.findNavController
import com.pfortbe22bgrupo2.architectapp.R

class DetailsFragment : Fragment() {

    companion object {
        fun newInstance() = DetailsFragment()
    }

    private lateinit var viewModel: DetailsViewModel

    lateinit var v : View
    lateinit var arButton : Button

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_details, container, false)
        arButton = v.findViewById(R.id.details_ar_button)
        return v
    }

    override fun onStart() {
        super.onStart()
        val furniture = DetailsFragmentArgs.fromBundle(requireArguments()).furnitureElement
        val nombre = v.findViewById<TextView>(R.id.detail_textView)
        nombre.text = furniture.nombre


        arButton.setOnClickListener(){
            val action = DetailsFragmentDirections.actionDetailsFragmentToARTrackingTest()
            v.findNavController().navigate(action)
        }
    }



    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)
        viewModel = ViewModelProvider(this).get(DetailsViewModel::class.java)
        // TODO: Use the ViewModel
    }

}