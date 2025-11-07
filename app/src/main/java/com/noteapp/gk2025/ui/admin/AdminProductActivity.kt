package com.noteapp.gk2025.ui.admin

import android.app.AlertDialog
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.noteapp.gk2025.R
import com.noteapp.gk2025.data.model.Product
import com.noteapp.gk2025.data.repository.ProductRepository
import com.noteapp.gk2025.data.repository.UserRepository
import com.noteapp.gk2025.util.FileHelper
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import com.noteapp.gk2025.ui.adapter.ProductAdapter
import com.noteapp.gk2025.ui.login.LoginActivity

class AdminProductActivity : AppCompatActivity() {
    private lateinit var etProductTitle: TextInputEditText
    private lateinit var etProductType: TextInputEditText
    private lateinit var etProductPrice: TextInputEditText
    private lateinit var tvFileName: android.widget.TextView
    private lateinit var ivPickFile: android.widget.ImageView
    private lateinit var btnAddProduct: MaterialButton
    private lateinit var rvProducts: androidx.recyclerview.widget.RecyclerView
    
    private val productRepository = ProductRepository()
    private val userRepository = UserRepository()
    
    private lateinit var productAdapter: ProductAdapter
    private var currentEditingProductId: String? = null
    private var selectedFileUri: Uri? = null
    private var currentFileBase64: String = ""
    
    private val filePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedFileUri = it
            val fileName = it.lastPathSegment ?: "file"
            tvFileName.text = fileName
        }
    }
    
    private var productsListener: com.google.firebase.database.ValueEventListener? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_admin_product)
        
        setupToolbar()
        initViews()
        setupRecyclerView()
        setupClickListeners()
        listenToProducts()
    }
    
    private fun setupToolbar() {
        supportActionBar?.title = getString(R.string.screen_title)
    }
    
    private fun initViews() {
        etProductTitle = findViewById(R.id.etProductTitle)
        etProductType = findViewById(R.id.etProductType)
        etProductPrice = findViewById(R.id.etProductPrice)
        tvFileName = findViewById(R.id.tvFileName)
        ivPickFile = findViewById(R.id.ivPickFile)
        btnAddProduct = findViewById(R.id.btnAddProduct)
        rvProducts = findViewById(R.id.rvProducts)
    }
    
    private fun setupRecyclerView() {
        productAdapter = ProductAdapter(
            products = mutableListOf(),
            onEditClick = { product ->
                editProduct(product)
            },
            onDeleteClick = { product ->
                showDeleteConfirmDialog(product)
            }
        )
        
        rvProducts.layoutManager = LinearLayoutManager(this)
        rvProducts.adapter = productAdapter
    }
    
    private fun setupClickListeners() {
        ivPickFile.setOnClickListener {
            filePickerLauncher.launch("image/*")
        }
        
        btnAddProduct.setOnClickListener {
            if (currentEditingProductId != null) {
                updateProduct()
            } else {
                addProduct()
            }
        }
    }
    
    private fun listenToProducts() {
        productsListener = productRepository.listenToProducts { products ->
            productAdapter.updateProducts(products)
        }
    }
    
    private fun convertFileToBase64AndAddProduct(title: String, type: String, price: String) {
        selectedFileUri?.let { uri ->
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val base64 = FileHelper.convertImageToBase64(this@AdminProductActivity, uri)
                    if (base64 != null) {
                        val description = Product.buildDescription(price, type)
                        val product = Product(
                            title = title,
                            description = description,
                            file = base64
                        )
                        
                        productRepository.addProduct(
                            product = product,
                            onSuccess = {
                                clearForm()
                                Toast.makeText(this@AdminProductActivity, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                Toast.makeText(this@AdminProductActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Toast.makeText(this@AdminProductActivity, "Lỗi: Không thể đọc file", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@AdminProductActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun convertFileToBase64AndUpdateProduct(productId: String, title: String, type: String, price: String) {
        selectedFileUri?.let { uri ->
            CoroutineScope(Dispatchers.Main).launch {
                try {
                    val base64 = FileHelper.convertImageToBase64(this@AdminProductActivity, uri)
                    if (base64 != null) {
                        val description = Product.buildDescription(price, type)
                        val product = Product(
                            id = productId,
                            title = title,
                            description = description,
                            file = base64
                        )
                        
                        productRepository.updateProduct(
                            product = product,
                            onSuccess = {
                                clearForm()
                                Toast.makeText(this@AdminProductActivity, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show()
                            },
                            onError = { error ->
                                Toast.makeText(this@AdminProductActivity, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                            }
                        )
                    } else {
                        Toast.makeText(this@AdminProductActivity, "Lỗi: Không thể đọc file", Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    Toast.makeText(this@AdminProductActivity, "Lỗi: ${e.message}", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
    
    private fun addProduct() {
        val title = etProductTitle.text?.toString()?.trim() ?: ""
        val type = etProductType.text?.toString()?.trim() ?: ""
        val price = etProductPrice.text?.toString()?.trim() ?: ""
        
        // Validation
        if (title.isEmpty()) {
            etProductTitle.error = getString(R.string.error_empty_title)
            return
        }
        
        if (price.isEmpty()) {
            etProductPrice.error = getString(R.string.error_empty_price)
            return
        }
        
        val priceValue = price.toIntOrNull() ?: 0
        if (priceValue <= 0) {
            etProductPrice.error = getString(R.string.error_invalid_price)
            return
        }
        
        // Convert file to base64 if selected
        if (selectedFileUri != null) {
            convertFileToBase64AndAddProduct(title, type, price)
        } else {
            val description = Product.buildDescription(price, type)
            val product = Product(
                title = title,
                description = description,
                file = ""
            )
            
            productRepository.addProduct(
                product = product,
                onSuccess = {
                    clearForm()
                    Toast.makeText(this, "Thêm sản phẩm thành công", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(this, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
    
    private fun updateProduct() {
        val productId = currentEditingProductId ?: return
        
        val title = etProductTitle.text?.toString()?.trim() ?: ""
        val type = etProductType.text?.toString()?.trim() ?: ""
        val price = etProductPrice.text?.toString()?.trim() ?: ""
        
        // Validation
        if (title.isEmpty()) {
            etProductTitle.error = getString(R.string.error_empty_title)
            return
        }
        
        if (price.isEmpty()) {
            etProductPrice.error = getString(R.string.error_empty_price)
            return
        }
        
        val priceValue = price.toIntOrNull() ?: 0
        if (priceValue <= 0) {
            etProductPrice.error = getString(R.string.error_invalid_price)
            return
        }
        
        // If new file selected, convert to base64 first
        if (selectedFileUri != null) {
            convertFileToBase64AndUpdateProduct(productId, title, type, price)
        } else {
            // Use existing file base64
            val description = Product.buildDescription(price, type)
            val product = Product(
                id = productId,
                title = title,
                description = description,
                file = currentFileBase64
            )
            
            productRepository.updateProduct(
                product = product,
                onSuccess = {
                    clearForm()
                    Toast.makeText(this, "Cập nhật sản phẩm thành công", Toast.LENGTH_SHORT).show()
                },
                onError = { error ->
                    Toast.makeText(this, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
                }
            )
        }
    }
    
    private fun editProduct(product: Product) {
        currentEditingProductId = product.id
        currentFileBase64 = product.file
        
        etProductTitle.setText(product.title)
        etProductType.setText(Product.parseType(product.description))
        etProductPrice.setText(Product.parsePrice(product.description))
        
        if (product.file.isNotEmpty()) {
            tvFileName.text = "Đã có hình ảnh"
        } else {
            tvFileName.text = ""
        }
        
        selectedFileUri = null
        btnAddProduct.text = getString(R.string.update_product)
        
        // Scroll to top
        findViewById<androidx.core.widget.NestedScrollView>(R.id.nestedScrollView)?.scrollTo(0, 0)
    }
    
    private fun showDeleteConfirmDialog(product: Product) {
        AlertDialog.Builder(this)
            .setTitle("Xác nhận")
            .setMessage(getString(R.string.delete_confirm))
            .setPositiveButton(getString(R.string.yes)) { _, _ ->
                deleteProduct(product)
            }
            .setNegativeButton(getString(R.string.no), null)
            .show()
    }
    
    private fun deleteProduct(product: Product) {
        productRepository.deleteProduct(
            productId = product.id,
            onSuccess = {
                Toast.makeText(this, "Xóa sản phẩm thành công", Toast.LENGTH_SHORT).show()
            },
            onError = { error ->
                Toast.makeText(this, "Lỗi: ${error.message}", Toast.LENGTH_SHORT).show()
            }
        )
    }
    
    private fun clearForm() {
        etProductTitle.text?.clear()
        etProductType.text?.clear()
        etProductPrice.text?.clear()
        tvFileName.text = ""
        selectedFileUri = null
        currentEditingProductId = null
        currentFileBase64 = ""
        btnAddProduct.text = getString(R.string.add_product)
    }
    
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.admin_menu, menu)
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

