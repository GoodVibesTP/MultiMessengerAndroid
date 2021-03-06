package com.goodvibes.multimessenger.network.vkmessenger

import android.annotation.SuppressLint
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.goodvibes.multimessenger.R
import com.goodvibes.multimessenger.datastructure.*
import com.goodvibes.multimessenger.network.Messenger
import com.goodvibes.multimessenger.network.vkmessenger.dto.*
import com.vk.api.sdk.VK as OriginalVKClient
import com.vk.api.sdk.VKTokenExpiredHandler
import com.vk.api.sdk.auth.VKScope
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.text.SimpleDateFormat
import java.util.*


object VK : Messenger {
    override val messenger = Messengers.VK

    @SuppressLint("SimpleDateFormat")
    val dateFormat = SimpleDateFormat("dd.MM.yyyy HH:mm:ss", Locale("ru", "ru"))

    private lateinit var activity: AppCompatActivity

    fun init(activity: AppCompatActivity) {
        this.activity = activity
        getUserInfo(null) {
            currentUserId = it[0].id
        }
    }

    private var haveAuthorization = false // true if there is a token
    private var currentUserId = 0L

    private val vkClient = OriginalVKClient

    private var token = "your_token" // placeholder for token

    private val permissions = arrayListOf<VKScope>()

    private val tokenTracker = object: VKTokenExpiredHandler {
        override fun onTokenExpired() {
            // token expired
        }
    }

    private const val LOG_TAG = "MultiMessenger_VK_logs"
    private val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://api.vk.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    private val messagesService = retrofit.create(VKMessagesApiService::class.java)
    private val usersService = retrofit.create(VKUsersApiService::class.java)

    private val mapVKUsers: MutableMap<Long, User> = mutableMapOf()

    private fun toDefaultChat(
        conversationWithMessage: VKMessagesConversationWithMessage,
        response: VKMessagesGetConversationsResponse,
        currentUserId: Long
    ) : Chat {
        val lastMessage = toDefaultMessage(conversationWithMessage.lastMessage, currentUserId)
        if (lastMessage != null) {
            lastMessage.read = if (lastMessage.isMyMessage) {
                lastMessage.id <= conversationWithMessage.conversation.outRead
            }
            else {
                lastMessage.id <= conversationWithMessage.conversation.inRead
            }
        }

        return Chat(
            chatId = conversationWithMessage.conversation.peer.id,
            img = R.mipmap.tg_icon,
            imgUri = when(conversationWithMessage.conversation.peer.type) {
                VKMessagesConversationPeerType.CHAT -> {
                    conversationWithMessage.conversation.chatSettings.photo?.photo100
                }
                VKMessagesConversationPeerType.USER -> {
                    val profile = response.profiles?.firstOrNull {
                        it.id == conversationWithMessage.conversation.peer.id
                    }
                    profile?.photo100
                }
                VKMessagesConversationPeerType.GROUP -> {
                    val group = response.groups?.firstOrNull {
                        it.id == -conversationWithMessage.conversation.peer.id
                    }
                    group?.photo100
                }
                else -> null
            },
            title = when(conversationWithMessage.conversation.peer.type) {
                VKMessagesConversationPeerType.CHAT -> {
                    conversationWithMessage.conversation.chatSettings.title
                }
                VKMessagesConversationPeerType.USER -> {
                    val profile = response.profiles?.firstOrNull {
                        it.id == conversationWithMessage.conversation.peer.id
                    }
                    "${profile?.firstName} ${profile?.lastName}"
                }
                VKMessagesConversationPeerType.GROUP -> {
                    val group = response.groups?.firstOrNull {
                        it.id == - conversationWithMessage.conversation.peer.id
                    }
                    "${group?.name}"
                }
                else -> ""
            },
            chatType = when(conversationWithMessage.conversation.peer.type) {
                VKMessagesConversationPeerType.CHAT -> ChatType.CHAT
                VKMessagesConversationPeerType.USER -> ChatType.USER
                VKMessagesConversationPeerType.GROUP -> ChatType.GROUP
                else -> ChatType.OTHER
            },
            messenger = Messengers.VK,
            lastMessage = lastMessage,
            inRead = conversationWithMessage.conversation.inRead,
            outRead = conversationWithMessage.conversation.outRead,
            unreadMessage = conversationWithMessage.conversation.unreadCount
        )
    }

    private fun toDefaultChat(
        conversationWithMessage: VKMessagesConversationWithMessage,
        response: VKMessagesGetConversationsByIdResponse,
        currentUserId: Long
    ) : Chat {
        val lastMessage = toDefaultMessage(conversationWithMessage.lastMessage, currentUserId)
        if (lastMessage != null) {
            lastMessage.read = if (lastMessage.isMyMessage) {
                lastMessage.id <= conversationWithMessage.conversation.outRead
            }
            else {
                lastMessage.id <= conversationWithMessage.conversation.inRead
            }
        }
        return Chat(
            chatId = conversationWithMessage.conversation.peer.id,
            img = R.mipmap.tg_icon,
            imgUri = when(conversationWithMessage.conversation.peer.type) {
                VKMessagesConversationPeerType.CHAT -> {
                    conversationWithMessage.conversation.chatSettings.photo?.photo100
                }
                VKMessagesConversationPeerType.USER -> {
                    val profile = response.profiles?.firstOrNull {
                        it.id == conversationWithMessage.conversation.peer.id
                    }
                    profile?.photo100
                }
                VKMessagesConversationPeerType.GROUP -> {
                    val group = response.groups?.firstOrNull {
                        it.id == -conversationWithMessage.conversation.peer.id
                    }
                    group?.photo100
                }
                else -> null
            },
            title = when(conversationWithMessage.conversation.peer.type) {
                VKMessagesConversationPeerType.CHAT -> {
                    conversationWithMessage.conversation.chatSettings.title
                }
                VKMessagesConversationPeerType.USER -> {
                    val profile = response.profiles?.firstOrNull {
                        it.id == conversationWithMessage.conversation.peer.id
                    }
                    "${profile?.firstName} ${profile?.lastName}"
                }
                VKMessagesConversationPeerType.GROUP -> {
                    val group = response.groups?.firstOrNull {
                        it.id == - conversationWithMessage.conversation.peer.id
                    }
                    "${group?.name}"
                }
                else -> ""
            },
            chatType = when(conversationWithMessage.conversation.peer.type) {
                VKMessagesConversationPeerType.CHAT -> ChatType.CHAT
                VKMessagesConversationPeerType.USER -> ChatType.USER
                VKMessagesConversationPeerType.GROUP -> ChatType.GROUP
                else -> ChatType.OTHER
            },
            messenger = Messengers.VK,
            lastMessage = lastMessage,
            inRead = conversationWithMessage.conversation.inRead,
            outRead = conversationWithMessage.conversation.outRead,
            unreadMessage = conversationWithMessage.conversation.unreadCount
        )
    }

    private fun toDefaultMessage(
        message: VKMessagesMessage?,
        currentUserId: Long
    ) : Message? {
        if (message == null) {
            return null
        }
        var fwdMessages: ArrayList<Message>? = null
        if (message.fwdMessages != null) {
            fwdMessages = arrayListOf()
            fwdMessages.ensureCapacity(message.fwdMessages.size)
            for (msg in message.fwdMessages) {
                fwdMessages.add(toDefaultMessage(msg, currentUserId)!!)
            }
        }

        return Message(
            id = message.id,
            chatId = message.peerId,
            userId = message.fromId,
            text = message.text,
            date = message.date,
            time = VK.dateFormat.format(message.date * 1000L),
            isMyMessage = currentUserId == message.fromId,
            fwdMessages = fwdMessages,
            replyTo = toDefaultMessage(message.replyMessage, currentUserId),
            messenger = Messengers.VK,
            attachments = toDefaultAttachments(message.attachments)
        )
    }

    private fun toDefaultAttachments(
        vkAttachments: List<VKMessagesAttachment>?
    ) : List<MessageAttachment>? {
        var attachments: List<MessageAttachment>? = null
        if (vkAttachments != null && vkAttachments.isNotEmpty()) {
            attachments = mutableListOf()
            for (vkAttachment in vkAttachments) {
                if (vkAttachment.photo != null) {
                    val photoBestQuality = vkAttachment.photo.sizes.maxByOrNull { it.width }
                    if (photoBestQuality != null) {
                        attachments.add(
                            MessageAttachment.Image(
                                height = photoBestQuality.height,
                                width = photoBestQuality.width,
                                imgUri = photoBestQuality.url
                            )
                        )
                    }
                }
            }
            if (attachments.isEmpty()) {
                attachments = null
            }
        }
        return attachments
    }

    private fun toDefaultUser(
        vkUser: VKUserFull
    ) : User {
        return User(
            userId = vkUser.id,
            firstName = vkUser.firstName ?: "",
            lastName = vkUser.lastName ?: "",
            imgUri = vkUser.photo200 ?: vkUser.photo100 ?: ""
        )
    }

    override fun isAuthorized(): Boolean {
        return haveAuthorization
    }

    override fun getUserId(): Long {
        return currentUserId
    }

    override fun getAllChats(
        count: Int,
        first_chat: Int,
        callback: (MutableList<Chat>) -> Unit
    ) {
        val methodName = "${this.javaClass.name}->${object {}.javaClass.enclosingMethod?.name}"

        val callForVKRespond: Call<VKRespond<VKMessagesGetConversationsResponse>>
            = messagesService.getConversations(
            access_token = this.token,
            count = count,
            offset = first_chat
        )

        callForVKRespond.enqueue(object : Callback<VKRespond<VKMessagesGetConversationsResponse>> {
            override fun onResponse(
                call: Call<VKRespond<VKMessagesGetConversationsResponse>>,
                response: Response<VKRespond<VKMessagesGetConversationsResponse>>
            ) {
                Log.d(LOG_TAG, "$methodName response code: ${response.code()}")
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    when {
                        responseBody == null -> {
                            Log.d(LOG_TAG, "$methodName successful, but response.body() is null")
                        }
                        responseBody.response != null -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName successful"
                            )
                            Log.d(
                                LOG_TAG,
                                "$methodName: get ${responseBody.response.count} chats"
                            )
                            val chatArray = arrayListOf<Chat>()
                            chatArray.ensureCapacity(responseBody.response.count)
                            for (item in responseBody.response.items) {
                                val nextChat = toDefaultChat(item, responseBody.response, currentUserId)
                                chatArray.add(nextChat)
                            }
                            if (responseBody.response.profiles != null) {
                                for (vkUser in responseBody.response.profiles) {
                                    mapVKUsers[vkUser.id] = toDefaultUser(vkUser)
                                }
                            }
                            callback(chatArray)
                        }
                        responseBody.error != null -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName error ${responseBody.error.errorCode}: ${responseBody.error.errorMsg}"
                            )
                        }
                        else -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName response is null && error is null"
                            )
                        }
                    }
                }
            }

            override fun onFailure(
                call: Call<VKRespond<VKMessagesGetConversationsResponse>>,
                t: Throwable
            ) {
                Log.d(LOG_TAG, "$methodName failure: $t")
            }
        })

        Log.d(LOG_TAG, "$methodName request: ${callForVKRespond.request()}")
    }

    override fun getMessagesFromChat(
        chat_id: Long,
        count: Int,
        offset: Int,
        first_msg_id: Long,
        callback: (MutableList<Message>) -> Unit
    ) {
        val methodName = "${this.javaClass.name}->${object {}.javaClass.enclosingMethod?.name}"
        val callForVKRespond: Call<VKRespond<VKMessagesGetHistoryResponse>> = messagesService.getHistory(
            access_token = this.token,
            peer_id = chat_id,
            count = count,
            offset = offset,
            start_message_id = first_msg_id
        )

        callForVKRespond.enqueue(object : Callback<VKRespond<VKMessagesGetHistoryResponse>> {
            override fun onResponse(
                call: Call<VKRespond<VKMessagesGetHistoryResponse>>,
                response: Response<VKRespond<VKMessagesGetHistoryResponse>>
            ) {
                Log.d(LOG_TAG, "$methodName response code: ${response.code()}")
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    when {
                        responseBody == null -> {
                            Log.d(LOG_TAG, "$methodName successful, but response.body() is null")
                        }
                        responseBody.response != null -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName successful"
                            )
                            val messagesArray = arrayListOf<Message>()
                            messagesArray.ensureCapacity(responseBody.response.count)
                            for (item in responseBody.response.items) {
                                val nextMessage = toDefaultMessage(item, currentUserId)
                                if (nextMessage != null) {
                                    nextMessage.read = if (nextMessage.isMyMessage) {
                                        nextMessage.id <= responseBody.response.conversations[0].outRead
                                    }
                                    else {
                                        nextMessage.id <= responseBody.response.conversations[0].inRead
                                    }
                                    messagesArray.add(nextMessage)
                                }
                            }
                            callback(messagesArray)
                        }
                        responseBody.error != null -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName error ${responseBody.error.errorCode}: ${responseBody.error.errorMsg}"
                            )
                        }
                        else -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName response is null && error is null"
                            )
                        }
                    }
                }
            }

            override fun onFailure(
                call: Call<VKRespond<VKMessagesGetHistoryResponse>>,
                t: Throwable
            ) {
                Log.d(LOG_TAG, "$methodName failure: $t")
            }
        })

        Log.d(LOG_TAG, "$methodName request: ${callForVKRespond.request()}")
    }

    override fun getChatById(
        chat_id: Long,
        callback: (Chat) -> Unit
    ) {
        val methodName = "${this.javaClass.name}->${object {}.javaClass.enclosingMethod?.name}"
        val callForVKRespond: Call<VKRespond<VKMessagesGetConversationsByIdResponse>> = messagesService.getConversationsById(
            access_token = this.token,
            extended = 1,
            peer_ids = chat_id
        )

        Log.d(LOG_TAG, "$methodName request: ${callForVKRespond.request()}")

        callForVKRespond.enqueue(object : Callback<VKRespond<VKMessagesGetConversationsByIdResponse>> {
            override fun onResponse(
                call: Call<VKRespond<VKMessagesGetConversationsByIdResponse>>,
                response: Response<VKRespond<VKMessagesGetConversationsByIdResponse>>
            ) {
                Log.d(LOG_TAG, "$methodName response code: ${response.code()}")
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    when {
                        responseBody == null -> {
                            Log.d(LOG_TAG, "$methodName successful, but response.body() is null")
                        }
                        responseBody.response != null -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName successful"
                            )
                            Log.d(
                                LOG_TAG,
                                "$methodName: get ${responseBody.response.count} chats"
                            )
                            val conversationWithMessage = VKMessagesConversationWithMessage(
                                conversation = responseBody.response.items[0],
                                lastMessage = null
                            )
                            if (responseBody.response.profiles != null) {
                                for (vkUser in responseBody.response.profiles) {
                                    mapVKUsers[vkUser.id] = toDefaultUser(vkUser)
                                }
                            }
                            val newChat = toDefaultChat(conversationWithMessage, responseBody.response, currentUserId)
                            callback(newChat)
                        }
                        responseBody.error != null -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName error ${responseBody.error.errorCode}: ${responseBody.error.errorMsg}"
                            )
                        }
                        else -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName response is null && error is null"
                            )
                        }
                    }
                }
            }

            override fun onFailure(
                call: Call<VKRespond<VKMessagesGetConversationsByIdResponse>>,
                t: Throwable
            ) {
                Log.d(LOG_TAG, "$methodName failure: $t")
            }
        })
    }

    override fun sendMessage(
        chat_id: Long,
        text: String,
        callback: (Long) -> Unit
    ) {
        val methodName = "${this.javaClass.name}->${object {}.javaClass.enclosingMethod?.name}"
        val callForVKRespond: Call<VKRespond<Long>> = messagesService.send(
            access_token = this.token,
            peer_id = chat_id,
            message = text
        )

        callForVKRespond.enqueue(object : Callback<VKRespond<Long>> {
            override fun onResponse(
                call: Call<VKRespond<Long>>,
                response: Response<VKRespond<Long>>
            ) {
                Log.d(LOG_TAG, "$methodName response code: ${response.code()}")
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    when {
                        responseBody == null -> {
                            Log.d(LOG_TAG, "$methodName successful, but response.body() is null")
                        }
                        responseBody.response != null -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName successful"
                            )
                            callback(responseBody.response)
                        }
                        responseBody.error != null -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName error ${responseBody.error.errorCode}: ${responseBody.error.errorMsg}"
                            )
                        }
                        else -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName response is null && error is null"
                            )
                        }
                    }
                }
            }

            override fun onFailure(
                call: Call<VKRespond<Long>>,
                t: Throwable
            ) {
                Log.d(LOG_TAG, "$methodName failure: $t")
            }
        })

        Log.d(LOG_TAG, "$methodName request: ${callForVKRespond.request()}")
    }

    override fun editMessage(
        chat_id: Long,
        message_id: Long,
        text: String,
        callback: (Long) -> Unit
    ) {
        val methodName = "${this.javaClass.name}->${object {}.javaClass.enclosingMethod?.name}"
        val callForVKRespond: Call<VKRespond<Long>> = messagesService.edit(
            access_token = this.token,
            peer_id = chat_id,
            message_id = message_id,
            message = text
        )

        callForVKRespond.enqueue(object : Callback<VKRespond<Long>> {
            override fun onResponse(
                call: Call<VKRespond<Long>>,
                response: Response<VKRespond<Long>>
            ) {
                Log.d(LOG_TAG, "$methodName response code: ${response.code()}")
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    when {
                        responseBody == null -> {
                            Log.d(LOG_TAG, "$methodName successful, but response.body() is null")
                        }
                        responseBody.response != null -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName successful"
                            )
                            callback(responseBody.response)
                        }
                        responseBody.error != null -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName error ${responseBody.error.errorCode}: ${responseBody.error.errorMsg}"
                            )
                        }
                        else -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName response is null && error is null"
                            )
                        }
                    }
                }
            }

            override fun onFailure(
                call: Call<VKRespond<Long>>,
                t: Throwable
            ) {
                Log.d(LOG_TAG, "$methodName failure: $t")
            }
        })

        Log.d(LOG_TAG, "$methodName request: ${callForVKRespond.request()}")
    }

    override fun deleteMessages(
        chat_id: Long,
        message_ids: List<Long>,
        callback: (List<Int>) -> Unit
    ) {
        val methodName = "${this.javaClass.name}->${object {}.javaClass.enclosingMethod?.name}"
        val callForVKRespond: Call<VKRespond<Any>> = messagesService.delete(
            access_token = this.token,
            peer_id = chat_id,
            message_ids = message_ids.joinToString(separator = ",")
        )

        callForVKRespond.enqueue(object : Callback<VKRespond<Any>> {
            override fun onResponse(
                call: Call<VKRespond<Any>>,
                response: Response<VKRespond<Any>>
            ) {
                Log.d(LOG_TAG, "$methodName response code: ${response.code()}")
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    when {
                        responseBody == null -> {
                            Log.d(LOG_TAG, "$methodName successful, but response.body() is null")
                        }
                        responseBody.response != null -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName successful"
                            )
                            callback(listOf())
                        }
                        responseBody.error != null -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName error ${responseBody.error.errorCode}: ${responseBody.error.errorMsg}"
                            )
                        }
                        else -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName response is null && error is null"
                            )
                        }
                    }
                }
            }

            override fun onFailure(
                call: Call<VKRespond<Any>>,
                t: Throwable
            ) {
                Log.d(LOG_TAG, "$methodName failure: $t")
            }
        })

        Log.d(LOG_TAG, "$methodName request: ${callForVKRespond.request()}")
    }

    override fun markAsRead(
        peer_id: Long,
        message_ids: List<Long>?,
        start_message_id: Long?,
        mark_conversation_as_read: Boolean,
        callback: (Int) -> Unit
    ) {
        val maxMessageId = if (start_message_id == null) start_message_id else {
            message_ids?.maxByOrNull { it }
        }
        val methodName = "${this.javaClass.name}->${object {}.javaClass.enclosingMethod?.name}"
        val callForVKRespond: Call<VKRespond<Int>> = messagesService.markAsRead(
            access_token = this.token,
            peer_id = peer_id,
            message_ids = null,
            start_message_id = maxMessageId,
            mark_conversation_as_read = if (mark_conversation_as_read) 1 else 0
        )

        callForVKRespond.enqueue(object : Callback<VKRespond<Int>> {
            override fun onResponse(
                call: Call<VKRespond<Int>>,
                response: Response<VKRespond<Int>>
            ) {
                Log.d(LOG_TAG, "$methodName response code: ${response.code()}")
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    when {
                        responseBody == null -> {
                            Log.d(LOG_TAG, "$methodName successful, but response.body() is null")
                        }
                        responseBody.response != null -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName successful"
                            )
                            callback(responseBody.response)
                        }
                        responseBody.error != null -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName error ${responseBody.error.errorCode}: ${responseBody.error.errorMsg}"
                            )
                        }
                        else -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName response is null && error is null"
                            )
                        }
                    }
                }
            }

            override fun onFailure(
                call: Call<VKRespond<Int>>,
                t: Throwable
            ) {
                Log.d(LOG_TAG, "$methodName failure: $t")
            }
        })

        Log.d(LOG_TAG, "$methodName request: ${callForVKRespond.request()}")
    }

    override fun startUpdateListener(callback: (Event) -> Unit) {
        val methodName = "${this.javaClass.name}->${object {}.javaClass.enclosingMethod?.name}"
        val callForVKRespond: Call<VKRespond<VKMessagesGetLongPoolServerResponse>> =
            messagesService.getLongPollServer(
            access_token = this.token,
            need_pts = 1,
            lp_version = 3
        )

        callForVKRespond.enqueue(object : Callback<VKRespond<VKMessagesGetLongPoolServerResponse>> {
            override fun onResponse(
                call: Call<VKRespond<VKMessagesGetLongPoolServerResponse>>,
                response: Response<VKRespond<VKMessagesGetLongPoolServerResponse>>
            ) {
                Log.d(LOG_TAG, "$methodName response code: ${response.code()}")
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    when {
                        responseBody == null -> {
                            Log.d(LOG_TAG, "$methodName successful, but response.body() is null")
                        }
                        responseBody.response != null -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName successful"
                            )

                            val serverUrl = responseBody.response.server.split("/")

                            val longPoolService = Retrofit.Builder()
                                .baseUrl("https://${serverUrl[0]}/")
                                .addConverterFactory(GsonConverterFactory.create())
                                .build()
                                .create(VKLongPoolApiService::class.java)

                            listenForEvents(
                                longPoolApiService = longPoolService,
                                server = serverUrl[1],
                                key = responseBody.response.key,
                                ts = responseBody.response.ts,
                                callback = callback
                            )
                        }
                        responseBody.error != null -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName error ${responseBody.error.errorCode}: ${responseBody.error.errorMsg}"
                            )
                        }
                        else -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName response is null && error is null"
                            )
                        }
                    }
                }
            }

            override fun onFailure(
                call: Call<VKRespond<VKMessagesGetLongPoolServerResponse>>,
                t: Throwable
            ) {
                Log.d(LOG_TAG, "$methodName failure: $t")
            }
        })

        Log.d(LOG_TAG, "$methodName request: ${callForVKRespond.request()}")
    }

    override fun getUser(user_id: Long, callback: (User) -> Unit) {
        val loadedUser = mapVKUsers[user_id]
        if (loadedUser != null) {
            callback(loadedUser)
        }
        else {
            getUserInfo(user_id.toString(), "photo_200") {
                val user = User(
                    userId = it[0].id,
                    firstName = it[0].firstName ?: "",
                    lastName = it[0].lastName ?: "",
                    imgUri = it[0].photoMaxOrig ?: it[0].photo200 ?: it[0].photo100 ?: ""
                )
                mapVKUsers[user_id] = user
                callback(user)
            }
        }
    }

    fun getCurrentUser(callback: (User) -> Unit) {
        val loadedUser = mapVKUsers[currentUserId]
        if (loadedUser != null) {
            callback(loadedUser)
        }
        else {
            getUserInfo(null, "photo_200") {
                val currentUser = User(
                    userId = it[0].id,
                    firstName = it[0].firstName ?: "",
                    lastName = it[0].lastName ?: "",
                    imgUri = it[0].photoMaxOrig ?: it[0].photo200 ?: it[0].photo100 ?: ""
                )
                mapVKUsers[currentUserId] = currentUser
                callback(currentUser)
            }
        }
    }

    private fun getUserInfo(
        user_ids: String?,
        fields: String = "",
        callback: (List<VKUserFull>) -> Unit
    ) {
        val methodName = "${this.javaClass.name}->${object {}.javaClass.enclosingMethod?.name}"
        val callForVKRespond: Call<VKRespond<List<VKUserFull>>> = if (user_ids == null) {
            usersService.get(
                access_token = this.token,
                fields = fields
            )
        }
        else {
            usersService.get(
                access_token = this.token,
                user_ids = user_ids,
                fields = fields
            )
        }

        callForVKRespond.enqueue(object : Callback<VKRespond<List<VKUserFull>>> {
            override fun onResponse(
                call: Call<VKRespond<List<VKUserFull>>>,
                response: Response<VKRespond<List<VKUserFull>>>
            ) {
                Log.d(LOG_TAG, "$methodName response code: ${response.code()}")
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    when {
                        responseBody == null -> {
                            Log.d(LOG_TAG, "$methodName successful, but response.body() is null")
                        }
                        responseBody.response != null -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName successful"
                            )
                            Log.d(LOG_TAG, "$methodName: get ${responseBody.response[0].firstName} " +
                                    "${responseBody.response[0].lastName} ${responseBody.response[0].sex}")
                            callback(responseBody.response)
                        }
                        responseBody.error != null -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName error ${responseBody.error.errorCode}: ${responseBody.error.errorMsg}"
                            )
                        }
                        else -> {
                            Log.d(
                                LOG_TAG,
                                "$methodName response is null && error is null"
                            )
                        }
                    }
                }
            }

            override fun onFailure(
                call: Call<VKRespond<List<VKUserFull>>>,
                t: Throwable
            ) {
                Log.d(LOG_TAG, "$methodName failure: $t")
            }
        })

        Log.d(LOG_TAG, "$methodName request: ${callForVKRespond.request()}")
    }

    override fun authorize() {
//        authLauncher.launch(permissions)
    }

    private fun listenForEvents(
        longPoolApiService: VKLongPoolApiService,
        server: String,
        key: String,
        ts: Int,
        callback: (Event) -> Unit
    ) {
        val methodName = "listenForEvents"
        val callForUpdates: Call<VKGetUpdates> = longPoolApiService.getUpdates(
            server = server,
            key = key,
            ts = ts,
            wait = 5,
            mode = 2,
            version = 3
        )

        val listenForEventsCallback = object : Callback<VKGetUpdates> {
            override fun onResponse(
                call: Call<VKGetUpdates>,
                response: Response<VKGetUpdates>
            ) {
                Log.d(LOG_TAG, "$methodName response code: ${response.code()}")
                if (response.isSuccessful) {
                    val responseBody = response.body()
                    if (responseBody == null) {
                            Log.d(LOG_TAG, "$methodName successful, but response.body() is null")
                    }
                    else {
                        Log.d(
                            LOG_TAG,
                            "ts: ${responseBody.ts}"
                        )
                        for (updateItem in responseBody.updates) {
                            val event: Event? = when(updateItem[0].asInt) {
                                VK_UPDATE.EVENTS.MESSAGE_FLAGS_STATED -> {
                                    if (updateItem[VK_UPDATE.MESSAGE_FLAGS_STATED.FLAGS].asInt and 128 != 0) {
                                        Event.DeleteMessage(
                                            updateItem[VK_UPDATE.MESSAGE_FLAGS_STATED.MINOR_ID].asLong,
                                            updateItem[VK_UPDATE.MESSAGE_FLAGS_STATED.MESSAGE_ID].asLong,
                                            Messengers.VK
                                        )
                                    }
                                    else {
                                        Event.DefaultEvent()
                                    }
                                }
                                VK_UPDATE.EVENTS.NEW_MESSAGE -> {
//                                    var getAllMessageInfo = false
//                                    if (updateItem.size > VK_UPDATE.NEW_MESSAGE.ADDITIONAL_FIELD) {
//                                        val ADDITIONAL_FIELD = VK_UPDATE.NEW_MESSAGE.ADDITIONAL_FIELD
//                                        if (updateItem[ADDITIONAL_FIELD].isJsonObject) {
//                                            updateItem[ADDITIONAL_FIELD].asJsonObject.get("fwd")?.asString
//                                        }
//                                        else {
//                                            Log.d(LOG_TAG, "$methodName field $ADDITIONAL_FIELD exists, " +
//                                                    "but isJsonObject = false")
//                                        }
//                                    }
//                                    val datetime = (System.currentTimeMillis() / 1000L).toInt()

                                    getMessagesFromChat(
                                        chat_id = updateItem[VK_UPDATE.NEW_MESSAGE.MINOR_ID].asLong,
                                        count = 1,
                                        offset = 0,
                                        first_msg_id = updateItem[VK_UPDATE.NEW_MESSAGE.MESSAGE_ID].asLong
                                    ) {
                                        if (it.isNotEmpty()) {
                                            callback(
                                                Event.NewMessage(
                                                    message = it[0],
                                                    direction = if (updateItem[VK_UPDATE.NEW_MESSAGE.FLAGS].asInt and 2 == 0) {
                                                        Event.NewMessage.Direction.INGOING
                                                    }
                                                    else {
                                                        Event.NewMessage.Direction.OUTGOING
                                                    }
                                                )
                                            )
                                        }
                                    }
                                    Event.DefaultEvent()
                                }
                                VK_UPDATE.EVENTS.MESSAGE_EDITED -> {
                                    if (updateItem.size > VK_UPDATE.NEW_MESSAGE.ADDITIONAL_FIELD) {
                                        val ADDITIONAL_FIELD = VK_UPDATE.NEW_MESSAGE.ADDITIONAL_FIELD
                                        if (updateItem[ADDITIONAL_FIELD].isJsonObject) {
                                            updateItem[ADDITIONAL_FIELD].asJsonObject.get("fwd")?.asString
                                        }
                                        else {
                                            Log.d(LOG_TAG, "$methodName field $ADDITIONAL_FIELD exists, " +
                                                    "but isJsonObject = false")
                                        }
                                    }
                                    val datetime = (System.currentTimeMillis() / 1000L).toInt()
                                    Event.EditMessage(
                                        message = Message(
                                            id = updateItem[VK_UPDATE.NEW_MESSAGE.MESSAGE_ID].asLong,
                                            chatId = updateItem[VK_UPDATE.NEW_MESSAGE.MINOR_ID].asLong,
                                            userId = updateItem[VK_UPDATE.NEW_MESSAGE.MINOR_ID].asLong,
                                            text = updateItem[VK_UPDATE.NEW_MESSAGE.TEXT].asString,
                                            isMyMessage = updateItem[VK_UPDATE.NEW_MESSAGE.FLAGS].asInt and 2 != 0,
                                            read = false,
                                            date = datetime,
                                            time = dateFormat.format(datetime * 1000L),
                                            fwdMessages = null,
                                            replyTo = null,
                                            messenger = Messengers.VK,
                                            attachments = null
                                        )
                                    )
                                }
                                VK_UPDATE.EVENTS.READ_INGOING -> {
                                    Event.ReadIngoingUntil(
                                        chat_id = updateItem[VK_UPDATE.READ_INGOING.PEER_ID].asLong,
                                        message_id = updateItem[VK_UPDATE.READ_INGOING.MESSAGE_ID].asLong,
                                        messenger = Messengers.VK
                                    )
                                }
                                VK_UPDATE.EVENTS.READ_OUTGOING -> {
                                    Event.ReadOutgoingUntil(
                                        chat_id = updateItem[VK_UPDATE.READ_OUTGOING.PEER_ID].asLong,
                                        message_id = updateItem[VK_UPDATE.READ_OUTGOING.MESSAGE_ID].asLong,
                                        messenger = Messengers.TELEGRAM
                                    )
                                }
                                else -> {
                                    Log.d(
                                        LOG_TAG,
                                        "update_code: ${updateItem[0].asInt}"
                                    )
                                    null
                                }
                            }

                            if (event != null) {
                                callback(event)
                            }
                        }
                    }
                    listenForEvents(
                        longPoolApiService = longPoolApiService,
                        server = server,
                        key = key,
                        ts = responseBody?.ts ?: ts,
                        callback = callback
                    )
                }
            }

            override fun onFailure(
                call: Call<VKGetUpdates>,
                t: Throwable
            ) {
                Log.d(LOG_TAG, "$methodName failure: $t")
            }
        }

        callForUpdates.enqueue(listenForEventsCallback)
        Log.d(LOG_TAG, "$methodName request: ${callForUpdates.request()}")
    }
}
