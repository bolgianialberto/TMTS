package com.example.tmts.beans.contracts

import android.app.Activity
import android.content.Context
import android.content.Intent
import androidx.activity.result.contract.ActivityResultContract
import com.example.tmts.activities.AddChatActivity

class AddChatResultContract : ActivityResultContract<Void?, String?>() {

    override fun createIntent(context: Context, input: Void?): Intent {
        val intent = Intent(context, AddChatActivity::class.java)
        return intent
    }

    override fun parseResult(resultCode: Int, intent: Intent?): String? {
        if (resultCode == Activity.RESULT_OK) {
            return intent?.getStringExtra("key")
        }
        return null
    }
}