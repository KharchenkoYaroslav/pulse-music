package com.example.pulsemusic.core.database.mapper

import com.example.pulsemusic.core.database.entity.UserProfileEntity
import com.example.pulsemusic.feature.profile.data.model.UserResponse

fun UserProfileEntity.toDomain() = UserResponse(login, createdAt)
fun UserResponse.toEntity() = UserProfileEntity(login = login, createdAt = createdAt)
