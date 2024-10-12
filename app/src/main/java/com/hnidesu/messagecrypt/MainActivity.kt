package com.hnidesu.messagecrypt

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.TextUtils
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.hnidesu.messagecrypt.databinding.ActivityMainBinding
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.SecretKeySpec

class MainActivity : AppCompatActivity() {
    private lateinit var mPassword:String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPassword=getSharedPreferences("settings",Context.MODE_PRIVATE).getString("password","keyset")!!
        val binding=ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)
        binding.buttonCopy.setOnClickListener {
            try{
                val clipboardManager=getSystemService<ClipboardManager>()
                val result=binding.textResult.text
                if(!TextUtils.isEmpty(result)){
                    clipboardManager!!.setPrimaryClip(ClipData.newPlainText("",result))
                    Toast.makeText(this,"已复制到剪贴板",Toast.LENGTH_SHORT).show()
                }
            }catch (ex:Exception){
                ex.printStackTrace()
                Toast.makeText(this,"复制失败",Toast.LENGTH_SHORT).show()
            }
        }
        binding.buttonExecute.setOnClickListener {
            try{
                val input=binding.inputData.text.toString()
                val password=binding.inputPassword.text.toString()
                val decrypt=binding.radioDecrypt.isChecked
                if(TextUtils.isEmpty(input))
                    Toast.makeText(this,"输入数据不可为空",Toast.LENGTH_SHORT).show()
                else if(TextUtils.isEmpty(password))
                    Toast.makeText(this,"密码不可为空",Toast.LENGTH_SHORT).show()
                else
                    binding.textResult.text = cryptText(input,password,decrypt)
                if(password!=mPassword){
                    val pref=getSharedPreferences("settings",Context.MODE_PRIVATE)
                    pref.edit().putString("password",password).apply()
                }
            }catch (ex:Exception){
                ex.printStackTrace()
                Toast.makeText(this,"执行失败",Toast.LENGTH_SHORT).show()
            }
        }
        binding.inputPassword.setText(mPassword)
    }

    private fun cryptText(input:String, password:String, decrypt:Boolean):String{
        val rawData= if(decrypt)
            Base64.decode(input,Base64.DEFAULT)
        else
            input.toByteArray()
        val digest=MessageDigest.getInstance("MD5")
        val hash= digest.digest(password.toByteArray())
        val cipher=Cipher.getInstance("AES/ECB/PKCS5Padding")
        val key=SecretKeySpec(hash,"AES")
        cipher.init(
            if(decrypt) Cipher.DECRYPT_MODE
            else Cipher.ENCRYPT_MODE
            ,key)
        val crypted=cipher.doFinal(rawData)
        return if(decrypt) crypted.decodeToString()
        else Base64.encode(crypted,Base64.DEFAULT).decodeToString()
    }

}