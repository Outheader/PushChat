package ru.outheader.testpush.data.entity.push

import ru.outheader.testpush.data.chat.ChatDataMessage

data class PushDataEntity(
    val id: Long,
    val message: ChatDataMessage?,
    val data: String?
)