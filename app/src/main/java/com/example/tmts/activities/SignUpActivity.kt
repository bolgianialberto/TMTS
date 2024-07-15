package com.example.tmts.activities

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.KeyEvent
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
import com.example.tmts.FirebaseInteraction
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

        // Set OnEditorActionListener for edtName
        edtName.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                edtEmail.requestFocus()
                true
            } else {
                false
            }
        }

        // Set OnEditorActionListener for edtEmail
        edtEmail.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                edtPassword.requestFocus()
                true
            } else {
                false
            }
        }

        // Set OnEditorActionListener for edtPassword
        edtPassword.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_NEXT) {
                edtConfirmPassword.requestFocus()
                true
            } else {
                false
            }
        }

        // Set OnEditorActionListener for edtConfirmPassword
        edtConfirmPassword.setOnEditorActionListener { _, actionId, event ->
            if (actionId == EditorInfo.IME_ACTION_DONE || event.keyCode == KeyEvent.KEYCODE_ENTER) {
                bttSignUp.performClick()
                hideKeyboard()
                true
            } else {
                false
            }
        }

        bttSignUp.setOnClickListener {
            val name = edtName.text.toString().trim()
            val email = edtEmail.text.toString().trim()
            val password = edtPassword.text.toString().trim()
            val confirmPassword = edtConfirmPassword.text.toString().trim()

            if (name.isNotEmpty() && email.isNotEmpty() && password.isNotEmpty() && confirmPassword.isNotEmpty()){
                if (validateFields(name, email, password, confirmPassword)) {
                    signup(name, email, password)
                }
            } else {
                Toast.makeText(this@SignUpActivity, "Please fill in all fields correctly", Toast.LENGTH_SHORT).show()
            }
        }

        // Add TextChangedListeners for input validation
        edtName.addTextChangedListener(createTextWatcherForField(tvUsernameErr) { text ->
            val result = UsernameRules.checkUsernameRules(text)
            if (result.resultId < 0) showTvUsernameErrorWithMessage(result.message)
            else hideTvUsernameError()
        })

        edtEmail.addTextChangedListener(createTextWatcherForField(tvEmailErr) { text ->
            val result = EmailRules.checkEmailRules(text)
            if (result.resultId < 0) showTvEmailErrorWithMessage(result.message)
            else hideTvEmailError()
        })

        edtPassword.addTextChangedListener(createTextWatcherForField(tvPasswordErr) { text ->
            val result = PasswordRules.checkPasswordRules(text)
            if (result.resultId < 0) showTvPasswordErrorWithMessage(result.message)
            else hideTvPasswordError()
        })

        edtConfirmPassword.addTextChangedListener(createTextWatcherForField(tvConfirmPasswordErr) { text ->
            if (edtConfirmPassword.text.isNotEmpty() && !checkEdtPasswordConfirmed()) {
                showTvConfirmPasswordError()
            } else {
                hideTvConfirmPasswordError()
            }
        })

        tvLogin.setOnClickListener {
            val intent = Intent(this@SignUpActivity, LoginActivity::class.java)
            startActivity(intent)
            finish()
        }
    }

    private fun validateFields(name: String, email: String, password: String, confirmPassword: String): Boolean {
        var isValid = true

        val usernameResult = UsernameRules.checkUsernameRules(name)
        if (usernameResult.resultId < 0) {
            showTvUsernameErrorWithMessage(usernameResult.message)
            isValid = false
        } else {
            hideTvUsernameError()
        }

        val emailResult = EmailRules.checkEmailRules(email)
        if (emailResult.resultId < 0) {
            showTvEmailErrorWithMessage(emailResult.message)
            isValid = false
        } else {
            hideTvEmailError()
        }

        val passwordResult = PasswordRules.checkPasswordRules(password)
        if (passwordResult.resultId < 0) {
            showTvPasswordErrorWithMessage(passwordResult.message)
            isValid = false
        } else {
            hideTvPasswordError()
        }

        if (password != confirmPassword) {
            showTvConfirmPasswordError()
            isValid = false
        } else {
            hideTvConfirmPasswordError()
        }

        return isValid
    }

    private fun signup(name: String, email: String, password: String) {
        mAuth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener(this) { task ->
                if (task.isSuccessful) {
                    FirebaseInteraction.reset()
                    addUserToDatabase(name, email, mAuth.currentUser?.uid!!)
                    val intent = Intent(this@SignUpActivity, MainActivity::class.java)
                    startActivity(intent)
                    finish()
                } else {
                    if (task.exception.toString().startsWith("com.google.firebase.auth.FirebaseAuthUserCollisionException")) {
                        showTvEmailErrorWithMessage("Email is already in use")
                    } else {
                        Toast.makeText(this@SignUpActivity, "Sign up failed: ${task.exception?.message}", Toast.LENGTH_SHORT).show()
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
        tvConfirmPasswordErr.text = "Passwords do not match"
        tvConfirmPasswordErr.visibility = View.VISIBLE
    }

    private fun createTextWatcherForField(errorTextView: TextView, validationFunction: (String) -> Unit): TextWatcher {
        return object : TextWatcher {
            override fun afterTextChanged(s: Editable?) { }

            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let {
                    validationFunction.invoke(it.toString())
                }
            }
        }
    }

    private fun hideKeyboard() {
        val imm = getSystemService(INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(currentFocus?.windowToken, 0)
    }
}

    object UsernameRules {
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

    // Rules for Email validation
    object EmailRules {
        fun checkEmailRules(email: String): EmailCheckResult {
            val emailRegex = Regex("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}$")
            if (email.matches(emailRegex)) {
                return EmailCheckResult(1, "Email is ok")
            }
            return EmailCheckResult(-1, "Email is not in the correct format")
        }
    }

    // Rules for Password validation
    object PasswordRules {
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

    // Data class to hold validation result
    data class ValidationResult(val resultId: Int, val message: String)
    data class UsernameCheckResult (var resultId: Int, var message: String)
    data class EmailCheckResult (var resultId: Int, var message: String)
    data class PasswordCheckResult (var resultId: Int, var message: String)

