package com.goodvibes.multimessenger

import android.content.Context
import android.content.Intent
import android.graphics.BitmapFactory
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.goodvibes.multimessenger.datastructure.Chat
import com.goodvibes.multimessenger.datastructure.Messengers
import com.goodvibes.multimessenger.network.tgmessenger.Telegram
import com.squareup.picasso.Picasso
import java.net.URL


//class ListChatsAdapter: ArrayAdapter<Chat> {
//    public constructor(ctx: Context, chats: List<Chat>) :
//            super(ctx, R.layout.list_item_chats, chats){}
//
//    public override fun getView(position: Int, convertView: View?, parent: ViewGroup): View {
//        var convertView_ = convertView;
//        val chat = getItem(position);
//        if (convertView_ == null) {
//            convertView_ = LayoutInflater.from(context).inflate(R.layout.list_item_chats, parent,false);
//        }
//
//        val imageViewAva: ImageView = convertView_!!.findViewById(R.id.chat_all_image);
//        val imageViewMessenger : ImageView = convertView_!!.findViewById(R.id.chat_all_messenger_img);
//        val textViewTitle: TextView = convertView_!!.findViewById(R.id.chat_all_title);
//        val textViewLastMessage: TextView = convertView_!!.findViewById(R.id.last_message);
//        imageViewAva.setImageResource(chat!!.img);
//        if (chat!!.messenger == Messengers.VK) {
//            imageViewMessenger.setImageResource(R.drawable.vk);
//        } else {
//            imageViewMessenger.setImageResource(R.mipmap.tg_icon)
//        }
//
//        textViewTitle.setText(chat!!.title);
//        textViewLastMessage.setText(chat!!.lastMessage!!.text);
//
//        return convertView_;
//    }
//
//}
class ListChatsAdapter(
    ctx: Context,
    val chats: MutableList<Chat>,
    var mainActivity: MainActivity
): RecyclerView.Adapter<ListChatsAdapter.ViewHolder>() {
    private val inflater = LayoutInflater.from(ctx)


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListChatsAdapter.ViewHolder {
        return ViewHolder(
            inflater.inflate(
                R.layout.list_item_chats,
                parent,
                false
            )
        )
    }

    override fun onBindViewHolder(
        holder: ListChatsAdapter.ViewHolder,
        position: Int
    ) {
        val chat = chats[position]
        if (chat.messenger == Messengers.VK) {
            holder.imageViewMessenger.setImageResource(R.drawable.vk)
        } else {
            holder.imageViewMessenger.setImageResource(R.mipmap.tg_icon)
        }
        if (chat.imgUri != null) {
            Picasso.get().load(chat.imgUri).into(holder.imageViewChatAva)
        } else {

                holder.imageViewChatAva.setImageResource(R.drawable.images)

        }

        holder.textViewTitle.text = chat.title
        holder.textViewLastMessage.text = chat.lastMessage?.text
        holder.textViewCountUnreadMessage.text = chat.unreadMessage.toString()

        holder.bindListeners(position)
    }

    override fun getItemCount(): Int {
        return chats.size
    }

    inner class ViewHolder internal constructor(var view: View) : RecyclerView.ViewHolder(view) {
        internal val imageViewChatAva: ImageView
        internal val imageViewMessenger: ImageView
        internal val textViewTitle: TextView
        internal val textViewLastMessage: TextView
        internal val textViewCountUnreadMessage: TextView

        init {
            imageViewChatAva = view.findViewById(R.id.chat_all_image);
            imageViewMessenger = view.findViewById(R.id.chat_all_messenger_img)
            textViewTitle = view.findViewById(R.id.chat_all_title)
            textViewLastMessage = view.findViewById(R.id.last_message);
            textViewCountUnreadMessage = view.findViewById(R.id.chat_all_message_unread)
        }

        fun bindListeners(position: Int) {
            view.setOnLongClickListener{ view ->
                if (mainActivity.mActionMode != null) {
                    false
                }
                view.isSelected = true
                mainActivity.callback.setClickedView(position)
                mainActivity.mActionMode = mainActivity.startActionMode(mainActivity.callback)!!
                true
            }

            view.setOnClickListener { View ->
                val intent = Intent(mainActivity, ChatActivity::class.java)
                val chat = mainActivity.listChatsAdapter.chats[adapterPosition]
                intent.putExtra("Chat", chat)
                mainActivity.startActivity(intent)
            }
        }
    }

}