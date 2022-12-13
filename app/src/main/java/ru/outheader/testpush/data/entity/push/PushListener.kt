package ru.outheader.testpush.data.entity.push

import ru.outheader.testpush.data.chat.ChatDataMessage

interface PushListener {

    fun onGetPushMessage(chatDataMessage: ChatDataMessage)
}