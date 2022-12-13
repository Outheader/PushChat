package ru.outheader.testpush.domain.service

import android.annotation.SuppressLint
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Intent
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationCompat.*
import androidx.core.app.NotificationManagerCompat
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import com.google.gson.Gson
import ru.outheader.testpush.R
import ru.outheader.testpush.data.Const
import ru.outheader.testpush.data.StatusMessage
import ru.outheader.testpush.data.chat.ChatDataMessage
import ru.outheader.testpush.data.chat.Sender
import ru.outheader.testpush.data.entity.push.PushDataEntity
import ru.outheader.testpush.data.entity.push.PushListener
import ru.outheader.testpush.data.entity.push.PushNotificationEntity
import ru.outheader.testpush.domain.database.DBHelper
import ru.outheader.testpush.domain.models.push.PushSender
import ru.outheader.testpush.presentation.ui.MainActivity
import kotlin.random.Random

class PushService : FirebaseMessagingService() {

    private val pushSender = PushSender()

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        Log.d("TEST ", "PushService/onNewToken, token is $token")
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        super.onMessageReceived(remoteMessage)
        Log.d("TEST ", "remoteMessage: ${Gson().toJson(remoteMessage)}")
        try {
            if (remoteMessage.data.isNotEmpty()) {
                val sender = when (remoteMessage.data.getValue("sender")) {
                    "ME" -> Sender.ME
                    "FROM" -> Sender.FROM
                    else -> Sender.UNKNOWN
                }
                val chatDataMessage = ChatDataMessage(
                    status = remoteMessage.data.getValue("status"),
                    sender = sender,
                    name = remoteMessage.data.getValue("name"),
                    message = remoteMessage.data.getValue("message"),
                    time = remoteMessage.data.getValue("time")
                )

                val data = remoteMessage.data.getValue("data")
                val dataValues = data.split("&")
                if (dataValues.isNotEmpty())
                    when (dataValues[0]) {
                        "report" -> {
                            when (chatDataMessage.name) {
                                Const.accName -> {
                                    Log.d(
                                        "TEST ",
                                        "Получен пуш сообщения с id: ${remoteMessage.data.getValue("id")}"
                                    )
                                    updateToDatabase(dataValues[1].toLong(), StatusMessage.GET)
                                    pushListener?.onGetPushMessage(chatDataMessage)
                                }
                                else -> {}
                            }
                        }
                        else -> {
                            when (chatDataMessage.name) {
                                Const.accName -> {}
                                else -> {
                                    saveToDatabase(chatDataMessage)
                                    pushListener?.onGetPushMessage(chatDataMessage)
                                    sendNotification(
                                        title = chatDataMessage.name,
                                        message = chatDataMessage.message
                                    )
                                    val id = remoteMessage.data.getValue("id").toLong()
                                    reportMessageDelivery(
                                        id,
                                        chatDataMessage
                                    )//Сообщаю о доставке сообщения
                                }
                            }
                        }
                    }
            }
        } catch (e: Exception) {
            Log.e("TEST ", "Exception: $e")
        }
    }

    private fun updateToDatabase(id: Long, status: StatusMessage) {
        val dbHelper = DBHelper(this)
        dbHelper.updateStatus(dbHelper, id, status)
    }

    private fun saveToDatabase(data: ChatDataMessage) {
        val dbHelper = DBHelper(this)
        dbHelper.addMessage(
            dbHelper, ChatDataMessage(
                status = data.status,
                sender = data.sender,
                name = data.name,
                message = data.message,
                time = data.time
            )
        )
    }

    @SuppressLint("LaunchActivityFromNotification")
    private fun sendNotification(title: String? = "Уведомление", message: String?) {
        val flags = PendingIntent.FLAG_IMMUTABLE
        val intent = Intent(this, MainActivity::class.java).apply {
            action = "PUSH_ALERT"
            this.data = Uri.parse(Random.nextInt().toString())
            putExtra("push_data", message)
        }
        val pendingIntent = PendingIntent.getService(this, Random.nextInt(), intent, 0)

        val bitmap = BitmapFactory.decodeResource(resources, R.drawable.icon_push_message)
        val notification = NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
            .setContentTitle(title)
            .setContentText(message)
            .setSmallIcon(R.drawable.icon_push_message)
            .setLargeIcon(bitmap)
            .setChannelId(NOTIFICATION_CHANNEL_ID)
            .setPriority(PRIORITY_MAX)
            .setAutoCancel(false)
            .setContentIntent(pendingIntent)
        val notificationManager = NotificationManagerCompat.from(this)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationChannel = NotificationChannel(
                NOTIFICATION_CHANNEL_ID,
                NOTIFICATION_CHANNEL_NAME,
                NotificationManager.IMPORTANCE_HIGH
            )
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify("notification", NOTIFICATION_ID, notification.build())
    }

    private fun reportMessageDelivery(idMessage: Long, chatDataMessage: ChatDataMessage) {
        pushSender.execute(
            PushNotificationEntity(
                to = "/topics/${Const.topic}",
                data = PushDataEntity(
                    idMessage,
                    ChatDataMessage(
                        status = chatDataMessage.status,
                        sender = Sender.ME,
                        name = chatDataMessage.name,
                        message = chatDataMessage.message,
                        time = chatDataMessage.time
                    ), "report&$idMessage")
            ),
            onComplete = {},
            onError = {}
        )
    }

    companion object {
        const val NOTIFICATION_ID = 777
        const val NOTIFICATION_CHANNEL_ID = "channelID"
        const val NOTIFICATION_CHANNEL_NAME = "pushChannel"
        private var pushListener: PushListener? = null

        fun updatePushListener(pushListener: PushListener) {
            this.pushListener = pushListener
        }
    }
}