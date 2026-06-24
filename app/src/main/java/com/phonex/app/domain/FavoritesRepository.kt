package com.phonex.app.domain

import com.phonex.app.data.FavoriteContact
import com.phonex.app.data.FavoritesDao
import kotlinx.coroutines.flow.Flow

class FavoritesRepository(private val favoritesDao: FavoritesDao) {
    val allFavorites: Flow<List<FavoriteContact>> = favoritesDao.getAllFavorites()

    suspend fun addFavorite(contact: FavoriteContact) {
        favoritesDao.insertFavorite(contact)
    }

    suspend fun removeFavorite(phoneNumber: String) {
        favoritesDao.deleteFavorite(phoneNumber)
    }
    
    fun isFavorite(phoneNumber: String): Flow<Boolean> = favoritesDao.isFavorite(phoneNumber)
}
