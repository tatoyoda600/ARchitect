package com.pfortbe22bgrupo2.architectapp.data

import com.pfortbe22bgrupo2.architectapp.R
import com.pfortbe22bgrupo2.architectapp.models.Furniture

class FurnitureList {

    var furnitures: MutableList<Furniture> = mutableListOf()

    init {
        furnitures.add(Furniture("Silla","living", R.drawable.silla_moderna))
        furnitures.add(Furniture("Sillon 3 piezas","living", R.drawable.sillon))
        furnitures.add(Furniture("Silla de escritorio","habitacion", R.drawable.silla_ppal))
        furnitures.add(Furniture("Silla","habitacion", R.drawable.silla_pop_up))
        furnitures.add(Furniture("Juego de comedor","comedor", R.drawable.juego_comedor))
        furnitures.add(Furniture("Escritorio","habitacion", R.drawable.escritorio_pop_up))
        furnitures.add(Furniture("Comoda","habitacion", R.drawable.comoda_pop_up))
        furnitures.add(Furniture("Planta","living", R.drawable.planta))
        furnitures.add(Furniture("Planta con maceta", "living", R.drawable.planta_pop_up))
        furnitures.add(Furniture("Textura pared","exterior", R.drawable.textura_pared))
        /*furnitures.add(Furniture("escalera","habitacion", R.drawable.silla_moderna))
        furnitures.add(Furniture("sillon","living", R.drawable.silla_moderna))
        furnitures.add(Furniture("cuadro","living", R.drawable.silla_moderna))
        furnitures.add(Furniture("mesa ratonera","living", R.drawable.silla_moderna))
        furnitures.add(Furniture("sillon 3 piezas","living", R.drawable.silla_moderna))
        furnitures.add(Furniture("cama","habitacion", R.drawable.silla_moderna))*/
    }
}