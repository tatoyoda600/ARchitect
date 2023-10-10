package com.pfortbe22bgrupo2.architectapp.data

import com.pfortbe22bgrupo2.architectapp.entities.Furniture

class FurnitureList {

    var furnitures: MutableList<Furniture> = mutableListOf()

    init {
        furnitures.add(Furniture("silla","living"))
        furnitures.add(Furniture("mesa","living"))
        furnitures.add(Furniture("escalera","habitacion"))
        furnitures.add(Furniture("sillon","living"))
        furnitures.add(Furniture("cuadro","living"))
        furnitures.add(Furniture("mesa ratonera","living"))
        furnitures.add(Furniture("sillon 3 piezas","living"))
        furnitures.add(Furniture("cama","habitacion"))
        furnitures.add(Furniture("silla", "living"))
        furnitures.add(Furniture("mesa","living"))
        furnitures.add(Furniture("escalera","habitacion"))
        furnitures.add(Furniture("sillon","living"))
        furnitures.add(Furniture("cuadro","living"))
        furnitures.add(Furniture("mesa ratonera","living"))
        furnitures.add(Furniture("sillon 3 piezas","living"))
        furnitures.add(Furniture("cama","habitacion"))
    }
}