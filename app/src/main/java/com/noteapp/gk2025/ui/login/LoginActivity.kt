package com.noteapp.gk2025.ui.login

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.noteapp.gk2025.R
import com.noteapp.gk2025.data.repository.UserRepository
import com.noteapp.gk2025.ui.admin.AdminProductActivity
import com.noteapp.gk2025.ui.user.UserProductActivity
import kotlinx.coroutines.launch

class LoginActivity : AppCompatActivity() {
    private lateinit var etEmail: TextInputEditText
    private lateinit var etPassword: TextInputEditText
    private lateinit var btnLogin: MaterialButton
    private lateinit var progressBar: android.widget.ProgressBar
    
    private val auth = FirebaseAuth.getInstance()
    private val userRepository = UserRepository()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        
        initViews()
        
        // Check if user is already logged in
        if (auth.currentUser != null) {
            progressBar.visibility = android.view.View.VISIBLE
            btnLogin.isEnabled = false
            checkUserRoleAndNavigate()
        } else {
            setupClickListeners()
        }
    }
    
    private fun initViews() {
        etEmail = findViewById(R.id.etEmail)
        etPassword = findViewById(R.id.etPassword)
        btnLogin = findViewById(R.id.btnLogin)
        progressBar = findViewById(R.id.progressBar)
    }
    
    private fun setupClickListeners() {
        btnLogin.setOnClickListener {
            performLogin()
        }
    }
    
    private fun performLogin() {
        val email = etEmail.text?.toString()?.trim() ?: ""
        val password = etPassword.text?.toString() ?: ""
        
        if (email.isEmpty() || password.isEmpty()) {
            Toast.makeText(this, "Vui lòng nhập đầy đủ thông tin", Toast.LENGTH_SHORT).show()
            return
        }
        
        progressBar.visibility = android.view.View.VISIBLE
        btnLogin.isEnabled = false
        
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                progressBar.visibility = android.view.View.GONE
                btnLogin.isEnabled = true
                
                if (task.isSuccessful) {
                    checkUserRoleAndNavigate()
                } else {
                    Toast.makeText(
                        this,
                        getString(R.string.login_failed) + ": ${task.exception?.message}",
                        Toast.LENGTH_SHORT
                    ).show()
                }
            }
    }
    
    private fun checkUserRoleAndNavigate() {
        val currentUser = auth.currentUser
        if (currentUser == null) {
            return
        }
        
        progressBar.visibility = android.view.View.VISIBLE
        
        lifecycleScope.launch {
            try {
                val role = userRepository.getUserRole(currentUser.uid)
                
                val intent = when (role) {
                    "admin" -> Intent(this@LoginActivity, AdminProductActivity::class.java)
                    else -> Intent(this@LoginActivity, UserProductActivity::class.java)
                }
                
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
            } catch (e: Exception) {
                Toast.makeText(
                    this@LoginActivity,
                    getString(R.string.no_permission),
                    Toast.LENGTH_SHORT
                ).show()
            } finally {
                progressBar.visibility = android.view.View.GONE
                btnLogin.isEnabled = true
            }
        }
    }
}

