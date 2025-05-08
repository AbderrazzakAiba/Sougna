package com.example.sougna.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "products")
data class ProductEntity(
    @PrimaryKey val id: String,
    val name: String,
    val description: String,
    val price: Double,
    val rating: Double,
    val userId: String,
    val categoryId: String,
    val thumbnailUrl: String,
    val createdAt: Date,
    val updatedAt: Date
)
