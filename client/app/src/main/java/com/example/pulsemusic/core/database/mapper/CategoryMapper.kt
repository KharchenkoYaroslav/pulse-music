package com.example.pulsemusic.core.database.mapper

import com.example.pulsemusic.core.database.entity.CategoryEntity
import com.example.pulsemusic.core.data.model.CategoryListResponse

fun CategoryEntity.toDomain() = CategoryListResponse(id, name)
fun CategoryListResponse.toEntity() = CategoryEntity(id, title)
