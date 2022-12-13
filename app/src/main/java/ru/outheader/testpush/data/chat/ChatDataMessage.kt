package ru.outheader.testpush.data.chat

data class ChatDataMessage(
    val status: String,
    val sender: Sender,
    val name: String,
    val message: String,
    val time: String
)