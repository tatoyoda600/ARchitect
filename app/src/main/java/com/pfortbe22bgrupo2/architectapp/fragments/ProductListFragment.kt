package com.pfortbe22bgrupo2.architectapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.snackbar.Snackbar
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.adapters.ProductListAdapter
import com.pfortbe22bgrupo2.architectapp.entities.Product
import com.pfortbe22bgrupo2.architectapp.listener.OnViewItemClickedListener

class ProductListFragment : Fragment(), OnViewItemClickedListener {

    private lateinit var v: View

    private lateinit var recProducts : RecyclerView

    private var products : MutableList<Product> = ArrayList()

    private lateinit var linearLayoutManager: LinearLayoutManager

    private lateinit var productListAdapter: ProductListAdapter

    companion object {
        fun newInstance() = ProductListFragment()
    }

    private lateinit var viewModel: ProductListViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        v = inflater.inflate(R.layout.fragment_product_list, container, false)
        recProducts = v.findViewById(R.id.rec_products)
        return v
    }


    override fun onStart() {
        super.onStart()

        //Creo la Lista Dinamica
        for (i in 1..2) {
          products.add(Product("a", "g", 100.00, R.drawable.silla_ppal, "m", 0))
            products.add(Product("b", "h", 100.00, R.drawable.sillon, "n", 0))
            products.add(Product("c", "i", 100.00, R.drawable.silla_moderna, "ñ", 0))
            products.add(Product("d", "j", 100.00, R.drawable.juego_comedor, "o", 0))
            products.add(Product("e", "k", 100.00, R.drawable.escritorio_pop_up, "p", 0))
            products.add(Product("f", "l", 100.00, R.drawable.comoda_pop_up, "q", 0))
        }
        requireActivity()
        recProducts.setHasFixedSize(false)
        linearLayoutManager = LinearLayoutManager(context)
        productListAdapter = ProductListAdapter(products, this)

        recProducts.layoutManager = linearLayoutManager
        recProducts.adapter = productListAdapter
    }

    override fun onViewItemDetail(product: Product) {
        val action = ProductListFragmentDirections.actionProductListFragmentToProductItemDetailFragment()
        v.findNavController().navigate(action)
        //Snackbar.make(v, product.name, Snackbar.LENGTH_LONG).show()
    }

}