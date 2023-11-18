package com.pfortbe22bgrupo2.architectapp.fragments

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.viewModels.ProductItemDetailViewModel

class ProductItemDetailFragment : Fragment() {

    companion object {
        fun newInstance() = ProductItemDetailFragment()
    }

    private lateinit var viewModel: ProductItemDetailViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_product_item_detail, container, false)
    }

}