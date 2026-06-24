package com.phonex.app.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "favorites")
data class FavoriteContact(
    @PrimaryKey val phoneNumber: String,
    val name: String,
    val photoUri: String? = null
)
