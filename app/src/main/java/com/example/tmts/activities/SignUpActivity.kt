package com.example.tmts.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.tmts.MainActivity
import com.example.tmts.R
import com.example.tmts.beans.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.database.DatabaseReference
import com.google.firebase.database.database

class SignUpActivity : AppCompatActivity() {

    private lateinit var edtName: EditText
    private lateinit var tvUsernameErr: TextView
    private lateinit var edtEmail: EditText
    private lateinit var tvEmailErr: TextView
    private lateinit var edtPassword: EditText
    private lateinit var tvPasswordErr: TextView
    private lateinit var edtConfirmPassword: EditText
    private lateinit var tvConfirmPasswordErr: TextView
    private lateinit var bttSignUp: Button
    private lateinit var tvLogin: TextView
    private lateinit var mAuth: FirebaseAuth
    private lateinit var mDbRef: DatabaseReference

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_sign_up)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        edtName = findViewById(R.id.edt_name)
        tvUsernameErr = findViewById(R.id.tv_username_error)
        edtEmail = findViewById(R.id.edt_email)
        tvEmailErr = findViewById(R.id.tv_email_error)
        edtPassword = findViewById(R.id.edt_password)
        tvPasswordErr = findViewById(R.id.tv_password_error)
        edtConfirmPassword = findViewById(R.id.edt_confirm_password)
        tvConfirmPasswordErr = findViewById(R.id.tv_confirm_password_error)
        bttSignUp = findViewById(R.id.btt_signup)
        tvLogin = findViewById(R.id.tv_registered1)
        mAuth = FirebaseAuth.getInstance()
        mDbRef = Firebase.database.reference

        bttSignUp.setOnClickListener {
            val name = edtName.text.toString()
            val email = edtEmail.text.toString()
            val password = edtPassword.text.toString()
            val usernameResult = UsernameRules.checkUsernameRules(edtName.text.toString()).resultId == 1
            val emailResult = EmailRules.checkEmailRules(edtEmail.text.toString()).resultId == 1
            val passwordResult = PasswordRules.checkPasswordRules(edtPassword.text.toString()).resultId == 1
            val confirmPassword = checkEdtPasswordConfirmed()
            if (usernameResult && emailResult && passwordResult && confirmPassword) {
                signup(name, email, password)
            }
        }

        edtName.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val username = s.toString()
                val result = UsernameRules.checkUsernameRules(username)
                if (result.resultId < 0) {
                    showTvUsernameErrorWithMessage(result.message)
                } else {
                    hideTvUsernameError()
                }
            }
        })

        edtEmail.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val email = s.toString()
                val result = EmailRules.checkEmailRules(email)
                if (result.resultId < 0) {
                    showTvEmailErrorWithMessage(result.message)
                } else {
                    hideTvEmailError()
                }
            }
        })

        edtPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                val password = s.toString()

                // check if password pass all rules
                val result = PasswordRules.checkPasswordRules(password)
                if (result.resultId < 0) {
                    showTvPasswordErrorWithMessage(result.message)
                } else {
                    hideTvPasswordError()
                }

                // if tvConfirmPasswordErr is not Empty, check
                if (edtConfirmPassword.text.isNotEmpty() && !checkEdtPasswordConfirmed()){
                    showTvConfirmPasswordError()
                } else {
                    hideTvConfirmPasswordError()
                }
            }
        })

        edtConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                if (edtConfirmPassword.text.isNotEmpty() && !checkEdtPasswordConfirmed()){
                    showTvConfirmPasswordError()
                } else {
                    hideTvConfirmPasswordError()
                }
            }
        })

    }

    private object UsernameRules {
        private const val minLength: Int = 2
        private const val maxLength: Int = 15

        fun checkUsernameRules(password: String): UsernameCheckResult {
            if (password.isEmpty()) {
                return UsernameCheckResult(0, "Username is empty")
            }
            if (password.length < minLength) {
                return UsernameCheckResult(-1, "Username must be $minLength characters minimum")
            }
            if (password.length > maxLength) {
                return UsernameCheckResult(-2, "Username must be $maxLength characters maximum")
            }
            return UsernameCheckResult(1, "Username is ok")
        }
    }

    private object EmailRules {
        fun checkEmailRules(email: String): EmailCheckResult {
            val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
            if (email.matches(emailRegex)) {
                return EmailCheckResult(1, "Email is ok")
            }
            return EmailCheckResult(-1, "Email is not in the correct format")
        }
    }

    private object PasswordRules {
        private const val minLength: Int = 6
        private const val maxLength: Int = 15
        private const val containsNumber: Boolean = true
        fun checkPasswordRules(password: String): PasswordCheckResult {
            if (password.isEmpty()) {
                return PasswordCheckResult(0, "Password is empty")
            }
            if (password.length < minLength) {
                return PasswordCheckResult(-1, "Password must be $minLength minimum")
            }
            if (password.length > maxLength) {
                return PasswordCheckResult(-2, "Password must be $maxLength maximum")
            }
            if (containsNumber && !password.any{it.isDigit()}) {
                return PasswordCheckResult(-2, "Password must contain at least one number")
            }
            return PasswordCheckResult(1, "Password is ok")
        }
    }

    private data class UsernameCheckResult (var resultId: Int, var message: String)

    private data class EmailCheckResult (var resultId: Int, var message: String)

    private data class PasswordCheckResult (var resultId: Int, var message: String)


    private fun signup(name: String, email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    addUserToDatabase(name, email, mAuth.currentUser?.uid!!)
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    if (task.exception.toString().startsWith("com.google.firebase.auth.FirebaseAuthUserCollisionException")) {
                        showTvEmailErrorWithMessage(EmailCheckResult(-2, "Mail is already used").message)
                    }
                }
            }
    }

    private fun addUserToDatabase(name: String, email: String, uid: String) {
        mDbRef.child("users").child(uid).setValue(User(uid, name, email))
    }

    private fun checkEdtPasswordConfirmed(): Boolean {
        return edtPassword.text.toString() == edtConfirmPassword.text.toString()
    }

    private fun hideTvUsernameError() {
        tvUsernameErr.text = ""
        tvUsernameErr.visibility = View.GONE
    }

    private fun showTvUsernameErrorWithMessage(message: String) {
        tvUsernameErr.text = message
        tvUsernameErr.visibility = View.VISIBLE
    }

    private fun hideTvEmailError() {
        tvEmailErr.text = ""
        tvEmailErr.visibility = View.GONE
    }

    private fun showTvEmailErrorWithMessage(message: String) {
        tvEmailErr.text = message
        tvEmailErr.visibility = View.VISIBLE
    }

    private fun hideTvPasswordError() {
        tvPasswordErr.text = ""
        tvPasswordErr.visibility = View.GONE
    }

    private fun showTvPasswordErrorWithMessage(message: String) {
        tvPasswordErr.text = message
        tvPasswordErr.visibility = View.VISIBLE
    }

    private fun hideTvConfirmPasswordError() {
        tvConfirmPasswordErr.text = ""
        tvConfirmPasswordErr.visibility = View.GONE
    }

    private fun showTvConfirmPasswordError() {
        tvConfirmPasswordErr.text = "Password do not match"
        tvConfirmPasswordErr.visibility = View.VISIBLE
    }

}