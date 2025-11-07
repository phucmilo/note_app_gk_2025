package com.noteapp.gk2025.ui.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import android.graphics.BitmapFactory
import android.util.Base64
import com.noteapp.gk2025.R
import com.noteapp.gk2025.data.model.Product

class UserProductAdapter(
    private val products: MutableList<Product>
) : RecyclerView.Adapter<UserProductAdapter.ProductViewHolder>() {

    inner class ProductViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivProductImage: ImageView = itemView.findViewById(R.id.ivProductImage)
        val tvProductName: TextView = itemView.findViewById(R.id.tvProductName)
        val tvProductPrice: TextView = itemView.findViewById(R.id.tvProductPrice)
        val tvProductType: TextView = itemView.findViewById(R.id.tvProductType)
        val btnEditProduct: View = itemView.findViewById(R.id.btnEditProduct)
        val btnDeleteProduct: View = itemView.findViewById(R.id.btnDeleteProduct)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ProductViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_product, parent, false)
        return ProductViewHolder(view)
    }

    override fun onBindViewHolder(holder: ProductViewHolder, position: Int) {
        val product = products[position]
        
        // Load image from base64
        if (product.file.isNotEmpty()) {
            try {
                val imageBytes = Base64.decode(product.file, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
                if (bitmap != null) {
                    holder.ivProductImage.setImageBitmap(bitmap)
                } else {
                    holder.ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
                }
            } catch (e: Exception) {
                holder.ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            holder.ivProductImage.setImageResource(android.R.drawable.ic_menu_gallery)
        }
        
        // Set text
        holder.tvProductName.text = "Tên sp: ${product.title}"
        holder.tvProductPrice.text = "Giá sp: ${Product.parsePrice(product.description)}"
        holder.tvProductType.text = "Loại sp: ${Product.parseType(product.description)}"
        
        // Hide edit/delete buttons for user view
        holder.btnEditProduct.visibility = View.GONE
        holder.btnDeleteProduct.visibility = View.GONE
    }

    override fun getItemCount() = products.size
    
    fun updateProducts(newProducts: List<Product>) {
        products.clear()
        products.addAll(newProducts)
        notifyDataSetChanged()
    }
}

