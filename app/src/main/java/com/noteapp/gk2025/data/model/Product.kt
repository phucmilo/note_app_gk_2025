package com.noteapp.gk2025.data.model

data class Product(
    val id: String = "",
    val title: String = "",
    val description: String = "",
    val file: String = ""
) {
    companion object {
        fun parsePrice(description: String): String {
            val pricePattern = Regex("Gia:\\s*(\\d+)")
            return pricePattern.find(description)?.groupValues?.get(1) ?: ""
        }
        
        fun parseType(description: String): String {
            val typePattern = Regex("Loai:\\s*([^;]+)")
            return typePattern.find(description)?.groupValues?.get(1)?.trim() ?: ""
        }
        
        fun buildDescription(price: String, type: String): String {
            return "Gia: $price; Loai: $type"
        }
    }
}

