package ru.outheader.testpush.domain

import android.text.format.DateFormat

class AppUtils {

    companion object {

        fun getChatTime(date: Long): String {
            return DateFormat.format("dd/MM/yyyy HH:mm:ss", date).toString()
        }
    }
}