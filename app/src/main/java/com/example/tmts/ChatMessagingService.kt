package com.example.tmts

import com.google.firebase.messaging.FirebaseMessagingService

class ChatMessagingService(): FirebaseMessagingService() {

    /*companion object {
        const val TAG = "FirebaseMsgSrv"
        const val CHANNEL_ID = "notification_channel"
        const val CHANNEL_NAME = "com.example.tmts"
    }



    private val mDbRef = FirebaseDatabase.getInstance().getReference()
    private val userId = FirebaseAuth.getInstance().currentUser!!.uid
    private val userRef = mDbRef.child("users").child(userId)

    fun sendRegistrationToServer(
        onSuccess: (String) -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                onFailure(task.exception)
            } else {
                val token = task.result
                Log.d(TAG, "Obtained token: $token")
                // Add token to database
                FirebaseInteraction.addToken(
                    token,
                    // Subscribe to notification for user
                    onSuccess={
                        subscribeToMessageNotification(
                            onSuccess = {
                                onSuccess(token)
                            },
                            onFailure = { exc ->
                                onFailure(exc)
                            }
                        )
                    },
                    onFailure = {
                        onFailure(it)
                    }
                )
            }
        }
    }

    fun getActualToken(
        onSuccess: (String) -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        userRef.child("token").addValueEventListener(object: ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val token = snapshot.value?.toString()
                if (token != null) {
                    onSuccess(token)
                } else {
                    sendRegistrationToServer(
                        onSuccess = {
                            onSuccess(it)
                        },
                        onFailure = {
                            onFailure(it)
                        }
                    )
                }
            }

            override fun onCancelled(error: DatabaseError) {
                onFailure(error.toException())
            }
        })
    }
    override fun onNewToken(token: String) {
        Log.d(TAG, "Refreshed token obtained: $token")
        sendRegistrationToServer(
            onSuccess = {
                Log.d(TAG, "Added to DB new token: $token")
            },
            onFailure = {
                Log.e(TAG, "Token not added to DB: $token")
            }
        )
    }

    override fun onMessageReceived(message: RemoteMessage) {
        super.onMessageReceived(message)
        if (message.getNotification() != null) {
            generateNotification(message.notification!!.title!!, message.notification!!.body!!)
        }
    }

    private fun generateNotification(title: String, message: String) {
        val intent = Intent(this, MainActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP)

        val pendingIntent = PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_ONE_SHOT or PendingIntent.FLAG_IMMUTABLE)

        var builder: NotificationCompat.Builder = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setSmallIcon(R.drawable.chat)
            .setAutoCancel(true)
            .setVibrate(longArrayOf(1000, 1000, 1000, 1000))
            .setOnlyAlertOnce(true)
            .setContentIntent(pendingIntent)

        builder = builder.setContent(getRemoteView(title, message))

        val notificationManager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            val notificationChannel =
                NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH)
            notificationManager.createNotificationChannel(notificationChannel)
        }
        notificationManager.notify(0, builder.build())
    }

    private fun getRemoteView(title: String, message: String) : RemoteViews {
        val remoteView = RemoteViews(CHANNEL_NAME, R.layout.message_notification)
        remoteView.setTextViewText(R.id.tv_message_notification_title, title)
        remoteView.setTextViewText(R.id.tv_message_notification_text, message)
        remoteView.setImageViewResource(R.id.iv_message_notification_app_logo, R.drawable.chat)
        return remoteView
    }

    fun subscribeToMessageNotification(
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        Firebase.messaging.subscribeToTopic(userId).addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                onFailure(task.exception)
            } else {
                Log.d(TAG, "Subscribed to $userId")
                onSuccess()
            }
        }
    }

    fun sendNotificationToUser(
        userId: String,
        onSuccess: () -> Unit,
        onFailure: (Exception?) -> Unit
    ) {
        // val msg = Firebase.messaging.send(msg)
    }*/
}