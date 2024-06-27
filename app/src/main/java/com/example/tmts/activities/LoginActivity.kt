package com.example.tmts.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tmts.R
import com.google.firebase.auth.FirebaseAuth

class LoginActivity : AppCompatActivity() {

    private lateinit var edtEmail: EditText
    private lateinit var edtPassword: EditText
    private lateinit var tvLoginErr: TextView
    private lateinit var btnLogin: Button
    private lateinit var tvSignUp: TextView
    private lateinit var mAuth: FirebaseAuth

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        edtEmail = findViewById(R.id.edt_email)
        edtPassword = findViewById(R.id.edt_password)
        tvLoginErr = findViewById(R.id.tv_login_error)
        btnLogin = findViewById(R.id.btt_login)
        tvSignUp = findViewById(R.id.tv_logged1)
        mAuth = FirebaseAuth.getInstance()

        edtEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                hideTvLoginError()
            }
        })

        edtPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                hideTvLoginError()
            }
        })

        edtEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                edtPassword.requestFocus()
                true
            } else {
                false
            }
        }

        edtPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                // Nascondi la tastiera virtuale
                hideKeyboard()

                // Rimuovi il focus dall'EditText della password
                edtPassword.clearFocus()

                // Esegui il login
                btnLogin.performClick()
                true
            } else {
                false
            }
        }

        btnLogin.setOnClickListener {
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()

            if (email.isNotEmpty() && password.isNotEmpty()) {
                login(email, password)
            } else {
                Toast.makeText(this@LoginActivity, "Please fill in both fields", Toast.LENGTH_SHORT).show()
            }
        }

        tvSignUp.setOnClickListener {
            val intent = Intent(this@LoginActivity, SignUpActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun login(email: String, password: String) {
        mAuth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    val intent = Intent(this@LoginActivity, MainActivity::class.java)
                    startActivity(intent)
                } else {
                    showTvLoginError()
                }
            }
    }

    private fun hideTvLoginError() {
        tvLoginErr.text = ""
        tvLoginErr.visibility = View.GONE
    }

    private fun showTvLoginError() {
        tvLoginErr.text = "Invalid credentials"
        tvLoginErr.visibility = View.VISIBLE
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}