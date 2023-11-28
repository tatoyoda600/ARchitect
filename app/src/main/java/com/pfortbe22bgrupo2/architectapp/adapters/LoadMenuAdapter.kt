package com.pfortbe22bgrupo2.architectapp.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.pfortbe22bgrupo2.architectapp.databinding.LoadDesignItemBinding
import com.pfortbe22bgrupo2.architectapp.databinding.LoadFloorItemBinding
import com.pfortbe22bgrupo2.architectapp.holders.LoadMenuViewHolder

class LoadMenuAdapter(
    val list: List<Pair<String, Int>>,
    val tab: TabType,
    val onClickListener: (Int) -> Unit
): RecyclerView.Adapter<LoadMenuViewHolder>() {

    enum class TabType {
        FLOORS,
        DESIGNS
    }

    override fun getItemCount() = list.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): LoadMenuViewHolder {
        val binding = if (tab == TabType.FLOORS)
                LoadFloorItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
            else
                LoadDesignItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoadMenuViewHolder(binding)
    }

    override fun onBindViewHolder(holder: LoadMenuViewHolder, position: Int) {
        val menuItem = list[position]
        holder.setText(menuItem.first)
        holder.setOnClickListener {
            onClickListener(menuItem.second)
        }
    }
}