package com.hnidesu.messagecrypt

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.widget.Toast
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.material3.Button
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.RadioButton
import androidx.compose.material3.Text
import androidx.compose.material3.TextField
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.getSystemService
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

class MainActivity : ComponentActivity() {
    private var mPassword: String = ""
    private var mInputText: String by mutableStateOf("")
    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_SEND) {
            val type = intent.type
            if (type != null) {
                mInputText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPassword =
            getSharedPreferences("settings", Context.MODE_PRIVATE).getString("password", "keyset")!!
        if (intent.action == Intent.ACTION_SEND) {
            val type = intent.type
            if (type != null)
                mInputText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        }
        setContent {
            MaterialTheme {
                MainForm()
            }
        }
    }

    @Composable
    private fun MainForm() {
        var password by remember { mutableStateOf(mPassword) }
        var outputText by remember { mutableStateOf("") }
        var encrypt by remember { mutableStateOf(true) }
        Column {
            Row(
                Modifier
                    .fillMaxWidth()
                    .background(MaterialTheme.colorScheme.primary)) {
                Text(
                    getString(R.string.app_name),
                    Modifier
                        .padding(10.dp, 10.dp, 10.dp, 10.dp)
                        .align(Alignment.CenterVertically),
                    style = TextStyle(color = Color.White), fontSize = 24.sp
                )
            }
            TextField(mInputText, { text -> mInputText = text }, Modifier
                .fillMaxWidth()
                .height(200.dp)
                .background(Color.Transparent)
                .padding(10.dp), placeholder = {
                Text("请输入要加密的数据")
            })
            Row {
                RadioButton(encrypt, {
                    encrypt = true
                }, Modifier.align(Alignment.CenterVertically))
                Text("加密", Modifier.align(Alignment.CenterVertically))
                RadioButton(!encrypt, {
                    encrypt = false
                }, Modifier.align(Alignment.CenterVertically))
                Text("解密", Modifier.align(Alignment.CenterVertically))
                Spacer(Modifier.width(20.dp))
                TextField(password,
                    { text -> password = text },
                    Modifier.padding(0.dp, 0.dp, 10.dp, 0.dp),
                    singleLine = true,
                    visualTransformation = PasswordVisualTransformation(),
                    placeholder = {
                        Text("请输入密码")
                    },
                    label = {
                        Text("密码")
                    })
            }
            Button({
                try {
                    if (TextUtils.isEmpty(mInputText))
                        Toast.makeText(this@MainActivity, "输入数据不可为空", Toast.LENGTH_SHORT)
                            .show()
                    else if (TextUtils.isEmpty(password))
                        Toast.makeText(this@MainActivity, "密码不可为空", Toast.LENGTH_SHORT).show()
                    else {
                        outputText = if (encrypt)
                            encryptText(mInputText, password)
                        else
                            decryptText(mInputText, password)
                    }
                    if (password != mPassword) {
                        val pref = getSharedPreferences("settings", Context.MODE_PRIVATE)
                        pref.edit().putString("password", password).apply()
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    Toast.makeText(this@MainActivity, "执行失败", Toast.LENGTH_SHORT).show()
                }
            },
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 10.dp, 10.dp, 0.dp)) { Text("执行") }
            TextField(outputText, {},
                Modifier
                    .fillMaxWidth()
                    .height(200.dp)
                    .background(Color.Transparent)
                    .padding(10.dp),
                placeholder = {
                    Text("结果将在这里显示")
                }, readOnly = true
            )
            Button({
                try {
                    val clipboardManager = getSystemService<ClipboardManager>()
                    if (!TextUtils.isEmpty(outputText)) {
                        clipboardManager!!.setPrimaryClip(ClipData.newPlainText("", outputText))
                        Toast.makeText(this@MainActivity, "已复制到剪贴板", Toast.LENGTH_SHORT)
                            .show()
                    }
                } catch (ex: Exception) {
                    ex.printStackTrace()
                    Toast.makeText(this@MainActivity, "复制失败", Toast.LENGTH_SHORT).show()
                }
            },
                Modifier
                    .fillMaxWidth()
                    .padding(10.dp, 0.dp, 10.dp, 0.dp)) { Text("复制结果") }
        }
    }

    @Composable
    @Preview
    private fun MainFormPreview() {
        MaterialTheme {
            MainForm()
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