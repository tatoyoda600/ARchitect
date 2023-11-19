package com.pfortbe22bgrupo2.architectapp.viewModels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.models.Furniture

class CatalogueDetailsViewModel: ViewModel() {

    private val _furnitureList = MutableLiveData<MutableList<Furniture>>()
    val furnitureList: LiveData<MutableList<Furniture>> get() = _furnitureList

    fun initList(){
        _furnitureList.value = mutableListOf(
            Furniture("normal-bed","habitacion", R.drawable.normal_bed, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."),
            Furniture("normal-grey-bed","habitacion", R.drawable.normal_grey_bed, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."),
            Furniture("ADDE_Chair","comedor", R.drawable.adde_chair, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."),
            Furniture("HENRIKSDAL_Chair","comedor", R.drawable.henriksdal_chair, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."),
            Furniture("HENRIKSDAL_Chair_2","living", R.drawable.henriksdal_2_chair, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."),
            Furniture("MARKUS_Swivel_Chair","habitacion", R.drawable.markus_swivel_chair , "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."),
            Furniture("chair","exterior", R.drawable.chair , "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."),
            Furniture("Arild_Seat_Sofa","living", R.drawable.arild_seat_sofa , "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."),
            Furniture("Fothult_2_Seat_Sofa","living", R.drawable.fothult_2_seat_sofa , "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."),
            Furniture("Lack_Black_Table", "living", R.drawable.lack_black_table , "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."),
            Furniture("round-table","living", R.drawable.round_table_2 , "round"),
            Furniture("two-leg-table","comedor", R.drawable.two_leg_table , "")
        )
    }

    init{
        initList()
    }

    fun removeElem(furnitureName: String){
        _furnitureList.value?.removeIf { f -> f.nombre == furnitureName }
    }
}