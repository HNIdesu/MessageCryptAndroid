package com.hnidesu.messagecrypt.ui.main

import android.app.Application
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.text.TextUtils
import android.util.Base64
import android.widget.Toast
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.core.content.getSystemService
import androidx.lifecycle.ViewModel
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

class MainScreenViewModel(
    private val mApplication: Application?
) : ViewModel() {
    var inputText by mutableStateOf("")
    var password by mutableStateOf("")
    var outputText by mutableStateOf("")
    var encrypt by mutableStateOf(true)

    fun cryptText() {
        val context = mApplication ?: return
        try {
            if (TextUtils.isEmpty(inputText))
                Toast.makeText(context, "输入数据不可为空", Toast.LENGTH_SHORT)
                    .show()
            else if (TextUtils.isEmpty(password))
                Toast.makeText(context, "密码不可为空", Toast.LENGTH_SHORT).show()
            else {
                outputText = if (encrypt)
                    encryptText(inputText, password)
                else
                    decryptText(inputText, password)
            }
            context.getSharedPreferences("settings", Context.MODE_PRIVATE).edit()
                .putString("password", password).apply()
        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(context, "执行失败", Toast.LENGTH_SHORT).show()
        }
    }

    fun copyToClipboard() {
        val context = mApplication ?: return
        try {
            val clipboardManager = context.getSystemService<ClipboardManager>()
            if (!TextUtils.isEmpty(outputText)) {
                clipboardManager!!.setPrimaryClip(ClipData.newPlainText("", outputText))
                Toast.makeText(context, "已复制到剪贴板", Toast.LENGTH_SHORT)
                    .show()
            }
        } catch (ex: Exception) {
            ex.printStackTrace()
            Toast.makeText(context, "复制失败", Toast.LENGTH_SHORT).show()
        }
    }

    private fun decryptText(input: String, password: String): String {
        val inputData = Base64.decode(input, Base64.DEFAULT)
        val hash = MessageDigest.getInstance("MD5").digest(password.toByteArray())
        val iv = IvParameterSpec(inputData, 0, 16)
        val key = SecretKeySpec(hash, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE, key, iv)
        return cipher.doFinal(inputData, 16, inputData.size - 16).decodeToString()
    }

    private fun encryptText(input: String, password: String): String {
        val hash = MessageDigest.getInstance("MD5").digest(password.toByteArray())
        val iv = IvParameterSpec(Random.nextBytes(16))
        val key = SecretKeySpec(hash, "AES")
        val cipher = Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE, key, iv)
        val encrypted = cipher.doFinal(input.toByteArray())
        return Base64.encode(iv.iv + encrypted, Base64.DEFAULT).decodeToString()
    }
}