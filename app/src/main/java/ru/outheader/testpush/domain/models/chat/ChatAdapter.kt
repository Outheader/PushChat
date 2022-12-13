package ru.outheader.testpush.domain.models.chat

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import ru.outheader.testpush.data.StatusMessage
import ru.outheader.testpush.data.chat.ChatDataMessage
import ru.outheader.testpush.data.chat.Sender
import ru.outheader.testpush.databinding.ViewChatMessageBinding

class ChatAdapter : RecyclerView.Adapter<ChatAdapter.MessageHolder>() {

    private var listMessages = mutableListOf<ChatDataMessage>()

    fun updateChat(list: List<ChatDataMessage>) {
        this.listMessages.clear()
        this.listMessages.addAll(list)
        notifyDataSetChanged()
    }

    class MessageHolder(private val view: ViewChatMessageBinding) : RecyclerView.ViewHolder(view.root) {

        fun init(itemList: ChatDataMessage) {
            when (itemList.sender) {
                Sender.FROM -> {
                    view.messageMe.visibility = View.GONE

                    view.tvChatNameFrom.text = itemList.name
                    view.tvChatMessageFrom.text = itemList.message
                    view.tvChatTimeFrom.text = itemList.time
                }
                Sender.ME -> {
                    view.messageFrom.visibility = View.GONE

                    view.tvChatName.text = itemList.name
                    view.tvChatMessage.text = itemList.message
                    view.tvChatTime.text = itemList.time

                    when (itemList.status) {
                        StatusMessage.SEND.toString() -> view.checkSend.visibility = View.VISIBLE
                        StatusMessage.GET.toString() -> {
                            view.checkSend.visibility = View.VISIBLE
                            view.checkGet.visibility = View.VISIBLE
                        }
                    }
                }
                Sender.UNKNOWN -> {
                    view.messageMe.visibility = View.GONE

                    view.tvChatNameFrom.text = "UNKNOWN"
                    view.tvChatMessageFrom.text = itemList.message
                    view.tvChatTimeFrom.text = itemList.time
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageHolder {
        val binding = ViewChatMessageBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MessageHolder(binding)
    }

    override fun onBindViewHolder(holder: MessageHolder, position: Int) {
        holder.init(listMessages[position])
        holder.itemView.rootView.setOnClickListener {  }
        holder.itemView.rootView.setOnLongClickListener {
            Log.d("TEST ", "Long click")
            true
        }
    }

    override fun getItemCount(): Int = listMessages.size
}