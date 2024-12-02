package com.hnidesu.messagecrypt

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.viewModels
import androidx.compose.material3.MaterialTheme
import com.hnidesu.messagecrypt.ui.main.MainScreen
import com.hnidesu.messagecrypt.ui.main.MainScreenViewModel
import com.hnidesu.messagecrypt.ui.main.MainScreenViewModelFactory

class MainActivity : ComponentActivity() {
    private val mMainScreenViewModel by viewModels<MainScreenViewModel>(
        factoryProducer = {
            MainScreenViewModelFactory(application)
        }
    )

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        if (intent.action == Intent.ACTION_SEND) {
            val type = intent.type
            if (type != null) {
                mMainScreenViewModel.inputText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        mMainScreenViewModel.password =
            getSharedPreferences("settings", Context.MODE_PRIVATE).getString("password", "keyset")!!
        if (intent.action == Intent.ACTION_SEND) {
            val type = intent.type
            if (type != null)
                mMainScreenViewModel.inputText = intent.getStringExtra(Intent.EXTRA_TEXT) ?: ""
        }
        setContent {
            MaterialTheme {
                MainScreen(mMainScreenViewModel)
            }
        }
    }

}