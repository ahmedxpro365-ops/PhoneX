package com.phonex.app.data

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

@Dao
interface FavoritesDao {
    @Query("SELECT * FROM favorites")
    fun getAllFavorites(): Flow<List<FavoriteContact>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertFavorite(contact: FavoriteContact)

    @Query("DELETE FROM favorites WHERE phoneNumber = :phoneNumber")
    suspend fun deleteFavorite(phoneNumber: String)
    
    @Query("SELECT EXISTS(SELECT 1 FROM favorites WHERE phoneNumber = :phoneNumber)")
    fun isFavorite(phoneNumber: String): Flow<Boolean>
}
