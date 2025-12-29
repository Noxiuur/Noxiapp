package com.noxi.noxiapp.data

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "user_profiles")
data class UserProfile(
    @PrimaryKey val email: String,
    val name: String = "",
    val phoneNumber: String = "",
    val photoUri: String? = null,
    val weight: Float? = null,
    val height: Float? = null,
    val age: Int? = null
)
