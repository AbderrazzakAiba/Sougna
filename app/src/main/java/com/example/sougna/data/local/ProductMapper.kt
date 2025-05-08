package com.example.sougna.data.local


import com.example.sougna.data.model.Product

fun Product.toEntity(): ProductEntity {
    return ProductEntity(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        rating = this.rating,
        userId = this.userId,
        categoryId = this.categoryId,
        thumbnailUrl = this.thumbnailUrl,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
}

fun ProductEntity.toProduct(): Product {
    return Product(
        id = this.id,
        name = this.name,
        description = this.description,
        price = this.price,
        rating = this.rating,
        userId = this.userId,
        categoryId = this.categoryId,
        thumbnailUrl = this.thumbnailUrl,
        createdAt = this.createdAt,
        updatedAt = this.updatedAt
    )
    }

