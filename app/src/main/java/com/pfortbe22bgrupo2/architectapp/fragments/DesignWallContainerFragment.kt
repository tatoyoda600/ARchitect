package com.pfortbe22bgrupo2.architectapp.fragments

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.tabs.TabLayout
import com.google.android.material.tabs.TabLayoutMediator
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.adapters.ViewPagerAdapter
import com.pfortbe22bgrupo2.architectapp.databinding.FragmentDesignWallContainerBinding
import com.pfortbe22bgrupo2.architectapp.databinding.FurnitureItemBinding


class DesignWallContainerFragment : Fragment() {

    lateinit var viewPager: ViewPager2
    lateinit var tabLayout: TabLayout
    private lateinit var binding: FragmentDesignWallContainerBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentDesignWallContainerBinding.inflate(inflater,container,false)
        viewPager = binding.viewPager2
        tabLayout = binding.tabLayout
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        viewPager.setAdapter(ViewPagerAdapter(requireActivity()))


        //Toast.makeText(requireActivity(), "Fragmento creado", Toast.LENGTH_SHORT).show()
        //Snackbar.make(v, "Fragmento creado", Snackbar.LENGTH_SHORT).show()

        TabLayoutMediator(tabLayout, viewPager, TabLayoutMediator.TabConfigurationStrategy { tab, position ->
            when (position) {
                0 -> {
                    tab.text = "Diseños"
                }
                1 -> {
                    tab.text = "Paredes"
                }
                else -> tab.text = "undefined"
            }
        }).attach()
    }

}




