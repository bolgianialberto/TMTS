package com.example.tmts.activities

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.tmts.FirebaseInteraction
import com.example.tmts.R
import com.example.tmts.adapters.AddChatAdapter
import com.example.tmts.beans.User

class AddChatActivity : AppCompatActivity() {

    private lateinit var addChatAdapter: AddChatAdapter
    private lateinit var rvUsers: RecyclerView
    private lateinit var edtUsername: EditText
    private val allUsers = ArrayList<User>()
    private val actuallyShownUsers = ArrayList<User>()
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_add_chat)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        addChatAdapter = AddChatAdapter(this)
        edtUsername = findViewById(R.id.edt_add_user_chat)
        edtUsername.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val startingChars = s.toString().lowercase()
                Log.d("Strating Chars", startingChars)
                actuallyShownUsers
                    .filterNot { it.name.lowercase().startsWith(startingChars) }
                    .map { addChatAdapter.removeUser(it) }
                actuallyShownUsers.removeAll { !it.name.lowercase().startsWith(startingChars) }
                allUsers
                    .filterNot { actuallyShownUsers.contains(it) }
                    .filter { it.name.lowercase().startsWith(startingChars) }
                    .map { addChatAdapter.updateUsers(it) }
                allUsers
                    .filterNot { actuallyShownUsers.contains(it) }
                    .filter { it.name.lowercase().startsWith(startingChars) }
                    .map { actuallyShownUsers.add(it) }
                Log.d("SHOWN", actuallyShownUsers.toString())
            }
        })
        rvUsers = findViewById(R.id.rv_add_chat)
        rvUsers.layoutManager = LinearLayoutManager(this)
        rvUsers.adapter = addChatAdapter
        loadUsers("")
    }

    private fun loadUsers(startingChars: String) {
        FirebaseInteraction.getUsersStartingWith(
            startingChars,
            onSuccess = {users ->
                allUsers.addAll(users)
                actuallyShownUsers.addAll(users)
                users.forEach{ addChatAdapter.updateUsers(it) }
            },
            onFailure = {
                Log.e("AddChatErr", it)
            }
        )
    }
}