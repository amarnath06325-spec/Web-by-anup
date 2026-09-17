package com.example.auth

data class GoogleUserProfile(
    val email: String,
    val displayName: String,
    val profilePictureUrl: String? = null,
    val idToken: String? = null
)
