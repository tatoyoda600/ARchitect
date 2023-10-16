package com.pfortbe22bgrupo2.architectapp.adapters

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.pfortbe22bgrupo2.architectapp.fragments.CustomWallFragment
import com.pfortbe22bgrupo2.architectapp.fragments.SavedDesignsFragment

class ViewPagerAdapter(fragmentActivity: FragmentActivity): FragmentStateAdapter(fragmentActivity) {

    override fun getItemCount(): Int {
        return TAB_COUNT
    }
    companion object {
        private const val TAB_COUNT = 2
    }

    override fun createFragment(position: Int): Fragment {
        return when(position){
            0 -> SavedDesignsFragment()
            1 -> CustomWallFragment()
            else -> SavedDesignsFragment()
        }
    }
}