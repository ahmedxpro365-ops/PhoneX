package com.phonex.app.data

import androidx.room.Database
import androidx.room.RoomDatabase

@Database(entities = [FavoriteContact::class], version = 1, exportSchema = false)
abstract class AppDatabase : RoomDatabase() {
    abstract fun favoritesDao(): FavoritesDao
}
