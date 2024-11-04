package com.hnidesu.messagecrypt

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.util.Base64
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.getSystemService
import com.hnidesu.messagecrypt.databinding.ActivityMainBinding
import java.security.MessageDigest
import javax.crypto.Cipher
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec
import kotlin.random.Random

class MainActivity : AppCompatActivity() {
    private var mPassword:String?=null
    private var mActivityMainBinding:ActivityMainBinding?=null

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if(intent.action==Intent.ACTION_SEND){
            val type=intent.type
            if(type!=null){
                val text=intent.getStringExtra(Intent.EXTRA_TEXT)
                mActivityMainBinding?.inputData?.setText(text)
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mPassword=getSharedPreferences("settings",Context.MODE_PRIVATE).getString("password","keyset")!!
        val binding=ActivityMainBinding.inflate(layoutInflater)
        if(intent.action==Intent.ACTION_SEND){
            val type=intent.type
            if(type!=null){
                val text=intent.getStringExtra(Intent.EXTRA_TEXT)
                binding.inputData.setText(text)
            }
        }
        mActivityMainBinding=binding
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
        binding.textResult.movementMethod=ScrollingMovementMethod.getInstance()
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
                    binding.textResult.text =
                        if(decrypt) decryptText(input,password)
                        else encryptText(input,password)
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
    private fun decryptText(input:String, password:String):String{
        val inputData=Base64.decode(input,Base64.DEFAULT)
        val hash=MessageDigest.getInstance("MD5").digest(password.toByteArray())
        val iv=IvParameterSpec(inputData,0,16)
        val key=SecretKeySpec(hash,"AES")
        val cipher=Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.DECRYPT_MODE,key,iv)
        return cipher.doFinal(inputData,16,inputData.size-16).decodeToString()
    }
    private fun encryptText(input:String, password:String):String{
        val hash=MessageDigest.getInstance("MD5").digest(password.toByteArray())
        val iv=IvParameterSpec(Random.nextBytes(16))
        val key=SecretKeySpec(hash,"AES")
        val cipher=Cipher.getInstance("AES/CBC/PKCS5Padding")
        cipher.init(Cipher.ENCRYPT_MODE,key,iv)
        val encrypted=cipher.doFinal(input.toByteArray())
        return Base64.encode(iv.iv+encrypted,Base64.DEFAULT).decodeToString()
    }

}