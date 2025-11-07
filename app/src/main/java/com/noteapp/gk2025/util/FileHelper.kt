package com.noteapp.gk2025.util

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.util.Base64
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import java.io.ByteArrayOutputStream
import java.io.InputStream

object FileHelper {
    suspend fun convertImageToBase64(context: Context, uri: Uri): String? = withContext(Dispatchers.IO) {
        try {
            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream?.close()
            
            if (bitmap != null) {
                val outputStream = ByteArrayOutputStream()
                // Compress to reduce size
                bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
                val byteArray = outputStream.toByteArray()
                outputStream.close()
                
                return@withContext Base64.encodeToString(byteArray, Base64.DEFAULT)
            }
            null
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }
    
    fun getBase64ImageUri(base64String: String): String {
        return "data:image/jpeg;base64,$base64String"
    }
    
    fun isBase64Image(uri: String): Boolean {
        return uri.startsWith("data:image") || uri.startsWith("base64,") || 
               (uri.length > 100 && !uri.startsWith("http"))
    }
}

