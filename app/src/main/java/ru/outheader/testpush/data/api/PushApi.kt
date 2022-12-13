package ru.outheader.testpush.data.api

import com.google.gson.JsonObject
import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.Headers
import retrofit2.http.POST
import ru.outheader.testpush.data.Const
import ru.outheader.testpush.data.entity.push.FireBaseEntity

interface PushApi {

    @POST("fcm/send")
    @Headers(
        "Content-Type: application/json",
        "Authorization: key=${Const.KEY}",
    )
    suspend fun sendPush(@Body jsonObject: JsonObject): Response<FireBaseEntity?>
}