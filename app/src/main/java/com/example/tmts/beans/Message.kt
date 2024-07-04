package com.example.tmts.beans

data class Message(
    val messageId: String,
    val text: String,
    val senderId: String,
    val receiverId: String,
    val timestamp: Long,
    val read: Boolean
)
