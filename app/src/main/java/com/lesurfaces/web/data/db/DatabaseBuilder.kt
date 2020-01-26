package com.lesurfaces.web.data.db

import android.content.Context
import androidx.room.Room
import androidx.room.RoomDatabase

class DatabaseBuilder {
    companion object {
        fun <T : RoomDatabase> newInstance(context: Context, name: String, databaseClass: Class<T>): T {
            return Room.databaseBuilder(context, databaseClass, name)
                .fallbackToDestructiveMigration()
                .build()
        }
    }
}