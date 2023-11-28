package com.pfortbe22bgrupo2.architectapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.pfortbe22bgrupo2.architectapp.entities.DesignEntity
import com.pfortbe22bgrupo2.architectapp.entities.DesignProductsEntity
import com.pfortbe22bgrupo2.architectapp.entities.FloorEntity
import com.pfortbe22bgrupo2.architectapp.entities.FloorPointsEntity

@Database(entities = [FloorEntity::class, FloorPointsEntity::class, DesignEntity::class, DesignProductsEntity::class], version = 1)
abstract class AppDatabase: RoomDatabase() {
    abstract fun floorDao(): FloorDao
    abstract fun floorPointsDao(): FloorPointsDao
    abstract fun designDao(): DesignDao
    abstract fun designProductsDao(): DesignProductsDao

    companion object {
        var INSTANCE: AppDatabase? = null

        fun getAppDatabase(context: Context): AppDatabase? {
            if (INSTANCE == null) {
                synchronized(AppDatabase::class) {
                    INSTANCE = Room.databaseBuilder(
                        context.applicationContext,
                        AppDatabase::class.java,
                        "architectDB"
                    ).fallbackToDestructiveMigration().build()
                }
            }

            return INSTANCE
        }

        fun destroyDatabase() {
            INSTANCE = null
        }
    }
}