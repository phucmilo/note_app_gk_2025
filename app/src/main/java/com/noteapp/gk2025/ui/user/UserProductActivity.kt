package com.noteapp.gk2025.ui.user

import android.content.Intent
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.noteapp.gk2025.R
import com.noteapp.gk2025.data.repository.ProductRepository
import com.noteapp.gk2025.data.repository.UserRepository
import com.noteapp.gk2025.ui.adapter.UserProductAdapter
import com.noteapp.gk2025.ui.login.LoginActivity

class UserProductActivity : AppCompatActivity() {
    private lateinit var rvProducts: androidx.recyclerview.widget.RecyclerView
    
    private val productRepository = ProductRepository()
    private val userRepository = UserRepository()
    
    private lateinit var productAdapter: UserProductAdapter
    private var productsListener: com.google.firebase.database.ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_user_product)
        
        setupToolbar()
        initViews()
        setupRecyclerView()
        listenToProducts()
    }
    
    private fun setupToolbar() {
        supportActionBar?.title = "Danh sách sản phẩm"
    }
    
    private fun initViews() {
        rvProducts = findViewById(R.id.rvProducts)
    }
    
    private fun setupRecyclerView() {
        productAdapter = UserProductAdapter(
            products = mutableListOf()
        )
        
        rvProducts.layoutManager = LinearLayoutManager(this)
        rvProducts.adapter = productAdapter
    }
    
    private fun listenToProducts() {
        productsListener = productRepository.listenToProducts { products ->
            productAdapter.updateProducts(products)
        }
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.user_menu, menu)
        return true
    }
    
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.menu_logout -> {
                userRepository.signOut()
                val intent = Intent(this, LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        productsListener?.let { productRepository.removeListener(it) }
    }
}

