package com.example.tmts

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.example.tmts.activities.ChatActivity

class ChatResultContract : ActivityResultContract<Pair<String, String>, String?>() {

    override fun createIntent(context: Context, input: Pair<String, String>): Intent {
        val intent = Intent(context, ChatActivity::class.java)
        intent.putExtra("userId", input.first)
        intent.putExtra("username", input.second)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode == Activity.RESULT_OK) {
            return intent?.getStringExtra("key")
        }
        return null
    }
}