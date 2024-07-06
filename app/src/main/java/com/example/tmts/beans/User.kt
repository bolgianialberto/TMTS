package com.example.tmts.beans

data class User(
    val id: String,
    var name: String,
    var email: String,
    var biography: String? = null
)