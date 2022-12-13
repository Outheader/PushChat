package ru.outheader.testpush.data.repository

import com.google.gson.JsonObject
import ru.outheader.testpush.data.Const
import ru.outheader.testpush.data.StatusMessage
import ru.outheader.testpush.data.api.RetrofitInstance
import ru.outheader.testpush.data.chat.ChatDataMessage
import ru.outheader.testpush.data.entity.push.FireBaseEntity
import ru.outheader.testpush.data.entity.push.PushDataEntity
import ru.outheader.testpush.domain.extensions.await

class PushRepository {

    suspend fun sendPush(
        to: String?,
        pushDataEntity: PushDataEntity?,
        data: String?
    ): Long {
        return RetrofitInstance.api.sendPush(
            JsonObject().apply {
                addProperty("to", to)
                pushDataEntity?.message?.let {
                    add("data", JsonObject().apply {
                        addProperty("id", pushDataEntity.id)
                        addProperty("status", StatusMessage.NEW.toString())
                        addProperty("sender", it.sender.toString())
                        addProperty("name", it.name)
                        addProperty("message", it.message)
                        addProperty("time", it.time)
                        addProperty("data", data)
                    })
                }
            }
        ).await(pushDataEntity!!.id) ?: throw Exception("no push data")
    }

}