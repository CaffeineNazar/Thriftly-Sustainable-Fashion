package com.example.thriftlyfashion.ui.signup

import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import com.example.thriftlyfashion.R
import com.example.thriftlyfashion.remote.api.ApiService
import com.example.thriftlyfashion.remote.api.RetrofitClient
import com.example.thriftlyfashion.remote.model.SignupRequest
import com.example.thriftlyfashion.ui.login.LoginActivity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SignupActivity : AppCompatActivity() {
    private var isPasswordVisible = false
    private var isConfirmPasswordVisible = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_signup)

        setupView()
        setupAction()
    }

    private fun setupView() {
        @Suppress("DEPRECATION")
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.R) {
            window.insetsController?.hide(WindowInsets.Type.statusBars())
        } else {
            window.setFlags(
                WindowManager.LayoutParams.FLAG_FULLSCREEN,
                WindowManager.LayoutParams.FLAG_FULLSCREEN
            )
        }
        supportActionBar?.hide()
    }

    private fun setupAction() {
        val ivTogglePassword = findViewById<ImageView>(R.id.ivTogglePassword)
        val ivConfirmPasswordToggle = findViewById<ImageView>(R.id.ivConfirPasswordToggle)
        val signupButton = findViewById<Button>(R.id.signupButton)
        val backButton = findViewById<Button>(R.id.backButton)

        val etNama = findViewById<EditText>(R.id.etNama)
        val etEmail = findViewById<EditText>(R.id.email)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val etConfirmPassword = findViewById<EditText>(R.id.etConfirPassword)

        ivTogglePassword.setOnClickListener {
            togglePasswordVisibility(etPassword, ivTogglePassword)
        }

        ivConfirmPasswordToggle.setOnClickListener {
            toggleConfirmPasswordVisibility(etConfirmPassword, ivConfirmPasswordToggle)
        }

        signupButton.setOnClickListener {
            val name = etNama.text.toString().trim()
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString()
            val confirmPassword = etConfirmPassword.text.toString()
            val isOwner = false

            if (name.isEmpty()) {
                Log.e("SignupActivity", "Nama tidak boleh kosong!")
                Toast.makeText(this, "Nama tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (email.isEmpty() || !android.util.Patterns.EMAIL_ADDRESS.matcher(email).matches()) {
                Log.e("SignupActivity", "Email tidak valid: $email")
                Toast.makeText(this, "Email tidak valid!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                Log.e("SignupActivity", "Password tidak boleh kosong!")
                Toast.makeText(this, "Password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password.length < 8) {
                Log.e("SignupActivity", "Password terlalu pendek: $password")
                Toast.makeText(this, "Password harus memiliki minimal 8 karakter!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (confirmPassword.isEmpty()) {
                Log.e("SignupActivity", "Konfirmasi password tidak boleh kosong!")
                Toast.makeText(this, "Konfirmasi password tidak boleh kosong!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            if (password != confirmPassword) {
                Log.e("SignupActivity", "Password dan konfirmasi password tidak sama!")
                Toast.makeText(this, "Password dan konfirmasi password tidak sama!", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val signupRequest = SignupRequest(name, email, password, isOwner)

            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val response = withContext(Dispatchers.IO) {
                        val apiService = RetrofitClient.createService(ApiService::class.java)
                        apiService.registerUser(signupRequest)
                    }

                    if (response.isSuccessful) {
                        Log.d("SignupActivity", "Akun berhasil dibuat: $email")
                        Toast.makeText(this@SignupActivity, "Akun berhasil dibuat!", Toast.LENGTH_SHORT).show()
                        val intent = Intent(this@SignupActivity, LoginActivity::class.java)
                        startActivity(intent)
                        finish()
                    } else {
                        Log.e("SignupActivity", "Gagal membuat akun untuk: $email")
                        Toast.makeText(this@SignupActivity, "Gagal membuat akun: ${response.message()}", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Log.e("SignupActivity", "Terjadi kesalahan: ${e.message}")
                    Toast.makeText(this@SignupActivity, "Terjadi kesalahan: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }

        backButton.setOnClickListener {
            finish()
        }
    }


    private fun togglePasswordVisibility(passwordField: EditText, toggleButton: ImageView) {
        isPasswordVisible = !isPasswordVisible
        passwordField.inputType = if (isPasswordVisible) {
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }
        passwordField.setSelection(passwordField.text.length)

        toggleButton.setImageResource(
            if (isPasswordVisible) R.drawable.baseline_visibility_off_24 else R.drawable.baseline_visibility_24
        )
    }

    private fun toggleConfirmPasswordVisibility(confirmPasswordField: EditText, toggleButton: ImageView) {
        isConfirmPasswordVisible = !isConfirmPasswordVisible
        confirmPasswordField.inputType = if (isConfirmPasswordVisible) {
            InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
        } else {
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        }

        confirmPasswordField.setSelection(confirmPasswordField.text.length)
        toggleButton.setImageResource(
            if (isConfirmPasswordVisible) R.drawable.baseline_visibility_off_24 else R.drawable.baseline_visibility_24
        )
    }
}
