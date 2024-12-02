package com.hnidesu.messagecrypt.ui.main

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider

class MainScreenViewModelFactory(
    private val mApplication: Application
) : ViewModelProvider.Factory {
    override fun <T : ViewModel> create(modelClass: Class<T>): T {
        if (modelClass.isAssignableFrom(MainScreenViewModel::class.java))
            return MainScreenViewModel(mApplication) as T
        throw IllegalArgumentException("Unknown ViewModel class")
    }
}