package com.example.tmts.activities

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
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

    private var senderId: String? = null
    private var receiverId: String? = null
    private var receiverRoom: String? = null
    private var senderRoom: String? = null

    private lateinit var chatLayoutView: View
    private lateinit var bttBack: Button
    private lateinit var ivUserImage: ImageView
    private lateinit var tvUser: TextView
    private lateinit var rvMessage: RecyclerView
    private lateinit var edtMessage: EditText
    private lateinit var bttSend: Button
    private lateinit var messageAdapter: MessageAdapter
    private var standardHeight = -1
    private lateinit var messageList: ArrayList<Pair<String, Message>>

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main_chat_layout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, 0, systemBars.right, 0)
            insets
        }
        chatLayoutView = findViewById(R.id.main_chat_layout)
        standardHeight = chatLayoutView.height
        chatLayoutView.viewTreeObserver.addOnGlobalLayoutListener {
            val newHeight = chatLayoutView.height
            if (newHeight != standardHeight) {
                rvMessage.scrollToPosition(messageList.size - 1)
            }
        }
        val receiverUsername = intent.getStringExtra("username")!!
        receiverId = intent.getStringExtra("userId")!!
        senderId = FirebaseInteraction.user?.uid
        senderRoom = receiverId + senderId
        receiverRoom = senderId + receiverId
        messageList = ArrayList()

        bttBack = findViewById(R.id.btt_arrow_back_chat)
        bttBack.setOnClickListener {
            val resultIntent = Intent()
            resultIntent.putExtra("closed", true)
            setResult(Activity.RESULT_OK, resultIntent)
            finish()
        }
        ivUserImage = findViewById(R.id.iv_account_chat_image)
        tvUser = findViewById(R.id.tv_chat_user)
        rvMessage = findViewById(R.id.rv_chat_messages)
        edtMessage = findViewById(R.id.edt_chat_message)
        bttSend = findViewById(R.id.btt_send_message)
        receiverId?.let { id ->
            FirebaseInteraction.getUserProfileImageRef(
                id,
                onSuccess = { userImageRef ->
                    userImageRef.downloadUrl.addOnSuccessListener { uri ->
                        // Verifica se il contesto è un'Activity e se è ancora valida
                        val activity = this as? Activity
                        if (activity != null && !activity.isDestroyed && !activity.isFinishing) {
                            Glide.with(activity)
                                .load(uri)
                                .into(ivUserImage)
                        }
                    }.addOnFailureListener { exception ->
                        Log.e("STORAGE DOWNLOAD", "Error: $exception")
                    }
                }, onFailure = { errorMessage ->
                    Log.e("IMAGE ERROR", errorMessage)
                }
            )
        }

        ivUserImage.setOnClickListener {
            val intent = Intent(this, UserPageActivity::class.java)
            intent.putExtra("uid", receiverId)
            startActivity(intent)
        }
        tvUser.text = receiverUsername
        tvUser.setOnClickListener {
            val intent = Intent(this, UserPageActivity::class.java)
            intent.putExtra("uid", receiverId)
            startActivity(intent)
        }
        messageAdapter = MessageAdapter(this, ArrayList(messageList.map { it.second }))
        rvMessage = findViewById(R.id.rv_chat_messages)
        rvMessage.layoutManager = LinearLayoutManager(this)
        rvMessage.adapter = messageAdapter
        bttSend.setOnClickListener {
            if (edtMessage.text.toString().isNotEmpty()) {
                sendMessage(edtMessage.text.toString(), senderId!!, receiverId!!)
            }
        }
        loadMessages()
    }

    private fun loadMessages() {
        messageList.clear()
        FirebaseInteraction.getSenderRoomNewMessages(
            messageList,
            senderRoom!!,
            onSuccess = { messages ->
                Log.d("Loading messages", "${messages.size}")
                messages.forEach {Log.d("MessageDetail", it.toString())}
                messageList.addAll(messages)
                messages.forEach { messageAdapter.updateMessages(it.second) }
                rvMessage.scrollToPosition(messageList.size - 1)
            },
            onFailure = {
                Log.e("CHAT ERROR", "Fail loading sender room $senderRoom")
            })
    }

    private fun sendMessage(messageText: String, senderId: String, receiverId: String) {
        FirebaseInteraction.sendMessage(
            messageText,
            senderId,
            receiverId,
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