package com.example.tmts.beans

data class Message(
    val text: String,
    val senderId: String,
    val receiverId: String,
    val timestamp: Long
)
