package ru.outheader.testpush.data.repository

import android.content.Context
import androidx.preference.PreferenceManager
import ru.outheader.testpush.data.chat.ChatDataMessage
import ru.outheader.testpush.domain.models.chat.ChatAdapter

class StorageRepository(context: Context) {

    private val storage = PreferenceManager.getDefaultSharedPreferences(context)

    var token: String?
        get() = storage.getString("token", null)
        set(value) {
            storage.edit().putString("token", value).apply()
        }
}