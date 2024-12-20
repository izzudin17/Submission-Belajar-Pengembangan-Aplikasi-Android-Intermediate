package com.dicoding.storyapps.local

data class userModel(
    val id: String,
    val name: String,
    val email: String,
    val token: String,
    val isLogin: Boolean
)
