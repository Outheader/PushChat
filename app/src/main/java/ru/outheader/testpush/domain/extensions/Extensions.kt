package ru.outheader.testpush.domain.extensions

import okhttp3.ResponseBody
import retrofit2.Response
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import kotlin.coroutines.suspendCoroutine


suspend fun <T> Response<T>?.await(): T? = suspendCoroutine { continuation ->
    this?.let {
        val header = headers()["Error-Code"]
        if (isSuccessful) {
            if (header?.isNotBlank() == true)
                continuation.resumeWithException(Exception(header))
            else continuation.resume(body())
        } else {
            continuation.resumeWithException(Exception(header))
        }
    } ?: continuation.resume(null)
}

//Только для сообщений. В успехе возвращает id отправленного сообщения, а не тело ответа
suspend fun <T> Response<T>?.await(id: Long): Long = suspendCoroutine { continuation ->
    this?.let {
        val header = headers()["Error-Code"]
        if (isSuccessful) {
            if (header?.isNotBlank() == true)
                continuation.resumeWithException(Exception(header))
            else continuation.resume(id)
        } else {
            continuation.resumeWithException(Exception(header))
        }
    }
}

fun ResponseBody?.body(): String? {
    return try {
        this?.string()
    } catch (e: IOException) {
        e.printStackTrace()
        null
    }
}