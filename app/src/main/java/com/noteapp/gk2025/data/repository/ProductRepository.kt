package com.noteapp.gk2025.data.repository

import com.google.firebase.database.DataSnapshot
import com.google.firebase.database.DatabaseError
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.database.ValueEventListener
import com.noteapp.gk2025.data.model.Product

class ProductRepository {
    private val database = FirebaseDatabase.getInstance()
    private val productsRef = database.getReference("products")
    
    fun addProduct(product: Product, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        val productMap = mapOf(
            "title" to product.title,
            "description" to product.description,
            "file" to product.file
        )
        
        productsRef.push()
            .setValue(productMap)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
    
    fun updateProduct(product: Product, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        if (product.id.isEmpty()) {
            onError(Exception("Product ID is empty"))
            return
        }
        
        val productMap = mapOf(
            "title" to product.title,
            "description" to product.description,
            "file" to product.file
        )
        
        productsRef.child(product.id)
            .updateChildren(productMap)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
    
    fun deleteProduct(productId: String, onSuccess: () -> Unit, onError: (Exception) -> Unit) {
        productsRef.child(productId)
            .removeValue()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }
    
    fun listenToProducts(onProductsChanged: (List<Product>) -> Unit): ValueEventListener {
        val listener = object : ValueEventListener {
            override fun onDataChange(snapshot: DataSnapshot) {
                val products = mutableListOf<Product>()
                
                snapshot.children.forEach { child ->
                    val product = Product(
                        id = child.key ?: "",
                        title = child.child("title").getValue(String::class.java) ?: "",
                        description = child.child("description").getValue(String::class.java) ?: "",
                        file = child.child("file").getValue(String::class.java) ?: ""
                    )
                    products.add(product)
                }
                
                onProductsChanged(products)
            }
            
            override fun onCancelled(error: DatabaseError) {
                onProductsChanged(emptyList())
            }
        }
        
        productsRef.addValueEventListener(listener)
        return listener
    }
    
    fun removeListener(listener: ValueEventListener) {
        productsRef.removeEventListener(listener)
    }
}
