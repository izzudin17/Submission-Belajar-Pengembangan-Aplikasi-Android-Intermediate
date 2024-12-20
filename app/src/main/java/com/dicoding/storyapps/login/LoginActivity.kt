package com.dicoding.storyapps.login

import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.View
import android.view.WindowInsets
import android.view.WindowManager
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import com.dicoding.storyapps.databinding.ActivityLoginBinding
import com.dicoding.storyapps.main.MainActivity
import com.dicoding.storyapps.response.LoginResult
import com.dicoding.storyapps.uti.Result
import com.dicoding.storyapps.uti.ViewModelFactory

class LoginActivity : AppCompatActivity() {

    private val binding: ActivityLoginBinding by lazy {
        ActivityLoginBinding.inflate(layoutInflater)
    }

    private val loginViewModel: LoginViewModel by viewModels {
        ViewModelFactory.getInstance(this)
    }


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(binding.root)

        setupView()
        setupAction()
        setupAnimation()

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
        binding.loginButton.setOnClickListener {
            val email = binding.edLoginEmail.text.toString()
            val password = binding.edLoginPassword.text.toString()
            when {
                email.isEmpty() -> {
                    binding.emailEditTextLayout.error = "Masukkan Email"
                }
                email.length < 8 -> {
                    binding.emailEditTextLayout.error = "Email harus memiliki setidaknya 8 karakter"
                }
                password.isEmpty() -> {
                    binding.passwordEditTextLayout.error = "Masukkan Password"
                }
                password.length < 8 -> {
                    binding.passwordEditTextLayout.error = "Password harus memiliki setidaknya 8 karakter"
                }
                else -> {
                    postLogin(email, password)
                }
            }
        }
    }

    private fun setupAnimation() {

        ObjectAnimator.ofFloat(binding.imageViewLogin, View.TRANSLATION_X, -30F, 30F).apply {
            duration = 6000
            repeatCount = ObjectAnimator.INFINITE
            repeatMode = ObjectAnimator.REVERSE
        }.start()

        val title = ObjectAnimator.ofFloat(binding.titleTextView, View.ALPHA, 1f).setDuration(600)
        val message = ObjectAnimator.ofFloat(binding.messageTextView, View.ALPHA, 1f).setDuration(600)
        val emailText = ObjectAnimator.ofFloat(binding.emailTextView, View.ALPHA, 1f).setDuration(600)
        val emailEdit = ObjectAnimator.ofFloat(binding.emailEditTextLayout, View.ALPHA, 1f).setDuration(600)
        val passText = ObjectAnimator.ofFloat(binding.passwordTextView, View.ALPHA, 1f).setDuration(600)
        val passEdit = ObjectAnimator.ofFloat(binding.passwordEditTextLayout, View.ALPHA, 1f).setDuration(600)
        val login = ObjectAnimator.ofFloat(binding.loginButton, View.ALPHA, 1f).setDuration(600)

        AnimatorSet().apply {
            playSequentially(title, message, emailText, emailEdit, passText, passEdit, login)
            start()
        }

    }

    private fun postLogin(email: String,password: String) {
        loginViewModel.postLogin(email, password).observe(this@LoginActivity) {
            if (it != null) {
                when (it) {

                    is Result.Loading -> {
                        binding.progressBar.visibility = View.VISIBLE
                    }

                    is Result.Success -> {
                        binding.progressBar.visibility = View.GONE
                        val user = it.data
                        userSave(user)

                        AlertDialog.Builder(this@LoginActivity).apply {
                            setTitle("Yes!")
                            setMessage("Login Success!")
                            setPositiveButton("Continue") { _, _ ->
                                val intent = Intent(
                                    this@LoginActivity,
                                    MainActivity::class.java
                                )
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            }
                            create()
                            show()
                        }
                    }

                    is Result.Error -> {
                        binding.progressBar.visibility = View.GONE
                        Toast.makeText(
                            this@LoginActivity,
                            it.error,
                            Toast.LENGTH_SHORT
                        ).show()

                        AlertDialog.Builder(this@LoginActivity).apply {
                            setTitle("Oops!")
                            setMessage("Something went wrong. Please make sure that you are registered or check your username and password")
                            setPositiveButton("Continue") { _, _ ->
                                val intent = Intent(
                                    this@LoginActivity,
                                    MainActivity::class.java
                                )
                                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
                                startActivity(intent)
                                finish()
                            }
                            create()
                            show()
                        }
                    }

                }
            }
        }
    }

    private fun userSave(user: LoginResult) {
        loginViewModel.userSave(user)
    }
}

