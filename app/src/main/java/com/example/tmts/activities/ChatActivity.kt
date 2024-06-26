package com.example.tmts.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.tmts.FirebaseInteraction
import com.example.tmts.R
import com.example.tmts.adapters.MessageAdapter
import com.example.tmts.beans.Message

class ChatActivity : AppCompatActivity() {

    private lateinit var bttBack: Button
    private lateinit var ivUserImage: ImageView
    private lateinit var tvUser: TextView
    private lateinit var rvMessage: RecyclerView
    private lateinit var edtMessage: EditText
    private lateinit var bttSend: Button
    private lateinit var messageAdapter: MessageAdapter
    private lateinit var messageList: ArrayList<Pair<String, Message>>
    private var messageSent = false
    var receiverRoom: String? = null
    var senderRoom: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val receiverUsername = intent.getStringExtra("username")!!
        val receiverId = intent.getStringExtra("userId")!!
        val senderId = FirebaseInteraction.user.uid
        senderRoom = receiverId + senderId
        receiverRoom = senderId + receiverId
        messageList = ArrayList()

        bttBack = findViewById(R.id.btt_arrow_back_chat
        )
        bttBack.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            intent.putExtra("reload", true)
            intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)
            intent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP)
            // intent.addFlags(Intent.)
            startActivity(intent)
        }
        ivUserImage = findViewById(R.id.iv_account_chat_image)
        tvUser = findViewById(R.id.tv_chat_user)
        rvMessage = findViewById(R.id.rv_chat_messages)
        edtMessage = findViewById(R.id.edt_chat_message)
        bttSend = findViewById(R.id.btt_send_message)

        FirebaseInteraction.getUserProfileImageRef(
            receiverId,
            onSuccess = {
                it.downloadUrl.addOnSuccessListener { uri ->
                    Glide.with(this)
                        .load(uri)
                        .into(ivUserImage)
                }.addOnFailureListener{ exc ->
                    Log.e("STORAGE DOWNLOAD", "Error: $exc")
                }
            }, onFailure = {
                Log.e("IMAGE ERROR", it)
            })
        tvUser.text = receiverUsername
        messageAdapter = MessageAdapter(this, ArrayList(messageList.map { it.second }))
        rvMessage = findViewById(R.id.rv_chat_messages)
        rvMessage.layoutManager = LinearLayoutManager(this)
        rvMessage.adapter = messageAdapter
        bttSend.setOnClickListener {
            val message = Message(
                edtMessage.text.toString(),
                senderId,
                receiverId,
                System.currentTimeMillis()
            )
            sendMessage(message)
        }
        loadMessages()
    }

    private fun loadMessages() {
        messageList.clear()
        FirebaseInteraction.getSenderRoomNewMessages(
            messageList,
            senderRoom!!,
            onSuccess = { messages ->
                messageList.addAll(messages)
                messages.forEach { messageAdapter.updateMessages(it.second) }
                rvMessage.scrollToPosition(messageList.size - 1)
            },
            onFailure = {
                Log.e("CHAT ERROR", "Fail loading sender room $senderRoom")
            })
    }

    private fun sendMessage(message: Message) {
        FirebaseInteraction.sendMessage(
            message,
            onSuccess = {
                edtMessage.setText("")
                rvMessage.scrollToPosition(messageList.size - 1)
                // messageAdapter.updateMessages(it)
            },
            onFailure = {
                Log.e("SEND MSG ERROR", it)
            })
    }
}