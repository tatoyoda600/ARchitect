package com.pfortbe22bgrupo2.architectapp.data

import com.pfortbe22bgrupo2.architectapp.models.Furniture

class FurnitureList {

    var furnitures: MutableList<Furniture> = mutableListOf()
}
//    val storage = FirebaseStorage.getInstance()
//    private lateinit var models: Task<ListResult>
//    private lateinit var images: Task<ListResult>
//    private lateinit var downloadImage: String

//    val categoriesRef = storage.reference.child("/models").listAll()


 /*   init {
//        categoriesRef.addOnSuccessListener { it1 ->
//            val categories = it1.prefixes
//
//            if(categories.isNotEmpty()){
//                for (c in categories){
//                    val categoryName = c.name
////                    models = c.child("/${categoryName}/models").listAll()
//
//
//                    //NO LLEGA A EJECUTAR ESTO, POR LA TARDANZA, NO SE SI SE PODRA PONER UN AWAIT?
//                    c.child("/${categoryName}/models").listAll().addOnSuccessListener { it2 ->
//                        images = c.child("/${categoryName}/images").listAll()
//                        images.addOnSuccessListener { it3 ->
//                            val models = it2.items
//                            val images = it3.items
//
//                            if(models.isNotEmpty() && images.isNotEmpty()){
//                                for (model in models){
//                                    val modelName = model.name.substring(0, model.name.length-4)
//                                    for (image in images){
//                                        val imageName = image.name.substring(0, image.name.length-4)
//                                        if(imageName == modelName){
//                                            image.downloadUrl.addOnSuccessListener {
//                                                downloadImage = it.toString()
//                                            }
//                                        }
//                                    }
//
//                                    furnitures.add(Furniture(modelName, categoryName, downloadImage, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
//                                }
//                            }
//                        }
//                    }
//                }
//            }
//        }

        furnitures.add(Furniture("normal-bed","habitacion", R.drawable.normal_bed, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("normal-grey-bed","habitacion", R.drawable.normal_grey_bed, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("ADDE_Chair","comedor", R.drawable.adde_chair, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("HENRIKSDAL_Chair","comedor", R.drawable.henriksdal_chair, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("HENRIKSDAL_Chair_2","living", R.drawable.henriksdal_2_chair, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("MARKUS_Swivel_Chair","habitacion",R.drawable.markus_swivel_chair , "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("chair","exterior",R.drawable.chair , "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("Arild_Seat_Sofa","living",R.drawable.arild_seat_sofa , "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("Fothult_2_Seat_Sofa","living",R.drawable.fothult_2_seat_sofa , "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("Lack_Black_Table", "living",R.drawable.lack_black_table , "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("round-table","living",R.drawable.round_table_2 , "round"))
        furnitures.add(Furniture("two-leg-table","comedor",R.drawable.two_leg_table , ""))


        *//*
=======
/*    init {
>>>>>>> Fernando
        furnitures.add(Furniture("Silla","living", R.drawable.silla_moderna, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("Sillon 3 piezas","living", R.drawable.sillon, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("Silla de escritorio","habitacion", R.drawable.silla_ppal, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("Silla","habitacion", R.drawable.silla_pop_up, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("Juego de comedor","comedor", R.drawable.juego_comedor, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("Escritorio","habitacion", R.drawable.escritorio_pop_up, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("Comoda","habitacion", R.drawable.comoda_pop_up, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("Planta","living", R.drawable.planta, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("Planta con maceta", "living", R.drawable.planta_pop_up, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
        furnitures.add(Furniture("Textura pared","exterior", R.drawable.textura_pared, "Lorem ipsum dolor sit amet, consectetur adipiscing elit. Pellentesque nec orci ante. Proin sed nulla a neque varius vulputate. Sed pharetra fringilla eros ut egestas. Nullam pulvinar orci quis velit efficitur vestibulum. Ut eu laoreet mauris. Ut auctor tortor eget quam condimentum suscipit."))
<<<<<<< HEAD
        furnitures.add(Furniture("escalera","habitacion", R.drawable.silla_moderna, ""))
=======
        *//*/*furnitures.add(Furniture("escalera","habitacion", R.drawable.silla_moderna, ""))
>>>>>>> Fernando
        furnitures.add(Furniture("sillon","living", R.drawable.silla_moderna, ""))
        furnitures.add(Furniture("cuadro","living", R.drawable.silla_moderna, ""))
        furnitures.add(Furniture("mesa ratonera","living", R.drawable.silla_moderna, ""))
        furnitures.add(Furniture("sillon 3 piezas","living", R.drawable.silla_moderna, ""))
        furnitures.add(Furniture("cama","habitacion", R.drawable.silla_moderna, ""))*//*/*
    }*//*
}*/