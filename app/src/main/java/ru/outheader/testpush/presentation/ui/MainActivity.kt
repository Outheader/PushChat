package ru.outheader.testpush.presentation.ui

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AlertDialog
import ru.outheader.testpush.data.Const
import ru.outheader.testpush.data.Status
import ru.outheader.testpush.data.StatusMessage
import ru.outheader.testpush.data.chat.ChatDataMessage
import ru.outheader.testpush.data.chat.Sender
import ru.outheader.testpush.data.entity.push.PushListener
import ru.outheader.testpush.databinding.ActivityMainBinding
import ru.outheader.testpush.domain.AppUtils
import ru.outheader.testpush.domain.database.DBHelper
import ru.outheader.testpush.domain.models.chat.ChatAdapter
import ru.outheader.testpush.domain.service.PushService
import java.util.Date

class MainActivity : AppCompatActivity(), PushListener {

    private val viewModel: MainViewModel = MainViewModel()
    private lateinit var binding: ActivityMainBinding
    private val adapter = ChatAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("TEST ", "MainActivity onCreate")

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)


        //clearDB()

        PushService.updatePushListener(this)

        val tvMessage = binding.tvMessage
        val recyclerView = binding.rvChat
        recyclerView.adapter = adapter
        recyclerView.recycledViewPool.setMaxRecycledViews(0, 0)
        recyclerView.setItemViewCacheSize(100)

        viewModel.data.observe(this) {
            when (it.status) {
                Status.LOADING -> {}
                Status.SUCCESS -> {
                    Log.d("TEST ", "Успешно отправлено сообщение с id: ${it.data}")
                    //Здесь нужно выставить 1 галочку сообщению с этим id
                    it.data?.let { viewModel.updateStatusMessage(this, it, StatusMessage.SEND) }
                    viewModel.getAllMessages(this)
                }
                Status.ERROR -> {
                    simpleException(it.exception?.message)
                }
            }
        }
        viewModel.token.observe(this) {
            when (it.status) {
                Status.SUCCESS -> {
                    viewModel.saveToken(this)
                }
                Status.ERROR -> simpleException(it.exception?.message)
                else -> {}
            }
        }
        viewModel.listMessages.observe(this) {
            updateChat(it)
        }
//        if (storage.token.isNullOrEmpty()) {
        viewModel.init(this)
//        }

        binding.button.setOnClickListener {
            viewModel.sendPush(
                this,
                ChatDataMessage(
                    status = StatusMessage.NEW.toString(),
                    sender = Sender.ME,
                    name = Const.accName,
                    message = tvMessage.text.toString(),
                    time = AppUtils.getChatTime(Date().time)
                )
            )
            tvMessage.text?.clear()
            viewModel.getAllMessages(this)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel.getAllMessages(this)
    }

    private fun simpleException(msg: String?) {
        AlertDialog.Builder(this)
            .setTitle("Ошибка")
            .setMessage(msg)
            .setCancelable(false)
            .setNegativeButton("Понял") { dialog, _ -> dialog?.dismiss() }
    }

    override fun onGetPushMessage(chatDataMessage: ChatDataMessage) {
        viewModel.getAllMessages(this)
    }

    private fun clearDB() {
        val dbHelper = DBHelper(this)
        dbHelper.clearDataChatTable(dbHelper)
    }

    private fun updateChat(listMessages: List<ChatDataMessage>) {
        runOnUiThread {
            adapter.updateChat(listMessages)
            binding.rvChat.adapter?.itemCount?.let {
                binding.rvChat.smoothScrollToPosition(it)
            }
        }
    }
}