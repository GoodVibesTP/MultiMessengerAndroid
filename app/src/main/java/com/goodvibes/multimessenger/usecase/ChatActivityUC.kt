package com.goodvibes.multimessenger.usecase

import android.widget.Toast
import com.goodvibes.multimessenger.ChatActivity
import com.goodvibes.multimessenger.datastructure.Chat
import com.goodvibes.multimessenger.datastructure.Event
import com.goodvibes.multimessenger.datastructure.Message
import com.goodvibes.multimessenger.datastructure.Messengers
import com.goodvibes.multimessenger.network.tgmessenger.Telegram
import com.goodvibes.multimessenger.network.vkmessenger.VK

class ChatActivityUC(_activityChat: ChatActivity) {
    var activityChat = _activityChat
    val tg = Telegram
    val vk = VK

    fun sendMessage(message: Message, callback: (Long) -> Unit) {
        when(message.messenger) {
            Messengers.VK -> {
                vk.sendMessage(message.chatId, message.text, callback)
            }
            Messengers.TELEGRAM -> {
                tg.sendMessage(message.chatId, message.text, callback)
            }
        }
    }

    fun getMessageFromChat(chat: Chat, count: Int, first_msg: Int = 0, callback: (MutableList<Message>) -> Unit) {
        when(chat.messenger) {
            Messengers.VK -> {
                vk.getMessagesFromChat(chat.chatId, count, first_msg, callback)
            }
            Messengers.TELEGRAM -> {
                tg.getMessagesFromChat(chat.chatId, count, first_msg, callback)
            }
        }
    }

    fun markAsRead(chat: Chat, callback: (Int) -> Unit) {
        when(chat.messenger) {
            Messengers.VK -> {
                vk.markAsRead(
                    chat.chatId,
                    null,
                    null,
                    true,
                    callback
                )
            }
            Messengers.TELEGRAM -> {
                tg.markAsRead(
                    chat.chatId,
                    null,
                    null,
                    true,
                    callback
                )
            }
        }
    }

    fun startUpdateListener(chat: Chat, callback: (Event) -> Unit) {
        when(chat.messenger) {
            Messengers.VK -> {
                vk.startUpdateListener(callback)
            }
            Messengers.TELEGRAM -> {
                tg.startUpdateListener(callback)
            }
        }
    }

}