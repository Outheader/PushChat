package ru.outheader.testpush.domain.database

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.database.sqlite.SQLiteDatabase
import android.database.sqlite.SQLiteOpenHelper
import ru.outheader.testpush.data.StatusMessage
import ru.outheader.testpush.data.chat.ChatDataMessage
import ru.outheader.testpush.data.chat.Sender

class DBHelper(
    context: Context
) : SQLiteOpenHelper(context, DATABASE_NAME, null, DATABASE_VERSION) {


    private val createChatTable = "CREATE TABLE $CHAT_TABLE( " +
            "id INTEGER PRIMARY KEY, " +
            "status TEXT, " +
            "sender TEXT, " +
            "name TEXT, " +
            "message TEXT, " +
            "time TEXT " + ")"
    private val dropTable = "DROP TABLE IF EXISTS $CHAT_TABLE"
    private val sqlGetAllMessages = "SELECT * FROM $CHAT_TABLE ORDER BY id"

    override fun onCreate(db: SQLiteDatabase?) {
        db?.execSQL(createChatTable)
    }

    override fun onUpgrade(db: SQLiteDatabase?, p1: Int, p2: Int) {

    }

    companion object {
        const val CHAT_TABLE = "chat_table"
        const val DATABASE_NAME = "ru.outheader.testpush.ChatMessages"
        const val DATABASE_VERSION = 1
    }

    @SuppressLint("Range")
    fun getAllMessages(dbHelper: DBHelper) : List<ChatDataMessage> {
        val db = dbHelper.readableDatabase
        val list = mutableListOf<ChatDataMessage>()

        val cursor = db.rawQuery(sqlGetAllMessages, null)
        cursor.moveToFirst()
        if (cursor != null && cursor.count > 0)
        do {
            val sender = when (cursor.getString(cursor.getColumnIndex("sender"))) {
                "FROM" -> Sender.FROM
                "ME" -> Sender.ME
                else -> Sender.UNKNOWN
            }
            list.add(ChatDataMessage(
                status = cursor.getString(cursor.getColumnIndex("status")),
                sender = sender,
                name = cursor.getString(cursor.getColumnIndex("name")),
                message = cursor.getString(cursor.getColumnIndex("message")),
                time = cursor.getString(cursor.getColumnIndex("time"))
            ))
        } while (cursor.moveToNext())
        cursor.close()
        db.close()
        return list
    }

    @SuppressLint("Range")
    fun addMessage(dbHelper: DBHelper, data: ChatDataMessage): Long {
        val db = dbHelper.writableDatabase
        val cv = ContentValues()
        cv.clear()
        cv.put("status", data.status)
        cv.put("sender", data.sender.toString())
        cv.put("name", data.name)
        cv.put("message", data.message)
        cv.put("time", data.time)
        db.insert(CHAT_TABLE, null, cv)

        val cursor = db.rawQuery("SELECT * FROM $CHAT_TABLE ORDER BY id", null)
        cursor.moveToLast()
        val lastId = cursor.getLong(cursor.getColumnIndex("id"))
        cursor.close()
        db.close()
        return lastId
    }

    @SuppressLint("Range")
    fun updateStatus(dbHelper: DBHelper, id: Long, status: StatusMessage) {
        val db = dbHelper.writableDatabase
        val cursor = db.rawQuery("SELECT * FROM $CHAT_TABLE WHERE id = $id", null)
        cursor.moveToFirst()

        val cv = ContentValues()
        cv.clear()
        cv.put("status", status.toString())
        cv.put("sender", cursor.getString(cursor.getColumnIndex("sender")))
        cv.put("name", cursor.getString(cursor.getColumnIndex("name")))
        cv.put("message", cursor.getString(cursor.getColumnIndex("message")))
        cv.put("time", cursor.getString(cursor.getColumnIndex("time")))
        db.update(CHAT_TABLE, cv, "id = $id", null)

        cursor.close()
        db.close()
    }

    fun clearDataChatTable(dbHelper: DBHelper) {
        val db = dbHelper.writableDatabase
        db.execSQL(dropTable)
        db.execSQL(createChatTable)
        db.close()
    }
}