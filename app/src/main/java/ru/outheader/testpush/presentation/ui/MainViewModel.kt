package ru.outheader.testpush.presentation.ui

import android.content.Context
import android.util.Log
import android.widget.Toast
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.google.gson.Gson
import ru.outheader.testpush.data.Const
import ru.outheader.testpush.data.StatusMessage
import ru.outheader.testpush.data.chat.ChatDataMessage
import ru.outheader.testpush.data.chat.Sender
import ru.outheader.testpush.data.entity.push.PushDataEntity
import ru.outheader.testpush.data.entity.push.PushNotificationEntity
import ru.outheader.testpush.data.repository.StorageRepository
import ru.outheader.testpush.domain.Event
import ru.outheader.testpush.domain.database.DBHelper
import ru.outheader.testpush.domain.models.push.PushSender

class MainViewModel : ViewModel() {

    val data = MutableLiveData<Event<Long>>()
    private val pushSender = PushSender()
    val token = MutableLiveData<Event<String>>()
    var listMessages = MutableLiveData<List<ChatDataMessage>>()

    fun init(context: Context) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener {
            if (!it.isSuccessful) {
                throw Exception(it.exception)
            } else {
                token.postValue(Event.success(it.result))
            }
        }
        Firebase.messaging.subscribeToTopic(Const.topic).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Toast.makeText(context, "Connected error: ${task.exception}", Toast.LENGTH_LONG).show()
                throw Exception(task.exception)
            } else {
                Toast.makeText(context, "Connected successfully!", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun saveToken(context: Context) {
        StorageRepository(context).token = token.value?.data
    }

    fun sendPush(context: Context, chatDataMessage: ChatDataMessage) {
        val idMessage = saveToDatabase(context, chatDataMessage)
        data.postValue(Event.loading())
        pushSender.execute(
            PushNotificationEntity(
                to = "/topics/${Const.topic}",
                data = PushDataEntity(
                    idMessage,
                    ChatDataMessage(
                        status = chatDataMessage.status,
                        sender = Sender.FROM,
                        name = chatDataMessage.name,
                        message = chatDataMessage.message,
                        time = chatDataMessage.time
                    ), "data")
            ),
            onComplete = { data.postValue(Event.success(it)) },
            onError = { data.postValue(Event.error(it)) }
        )
    }

    private fun saveToDatabase(context: Context, chatDataMessage: ChatDataMessage): Long {
        val dbHelper = DBHelper(context)
        return dbHelper.addMessage(dbHelper, chatDataMessage)
    }

    fun updateStatusMessage(context: Context, id: Long, status: StatusMessage){
        val dbHelper = DBHelper(context)
        dbHelper.updateStatus(dbHelper, id, status)
    }

    fun getAllMessages(context: Context) {
        val dbHelper = DBHelper(context)
        val list = dbHelper.getAllMessages(dbHelper)
        listMessages.postValue(list)
    }
}