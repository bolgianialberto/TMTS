package com.example.tmts.beans.viewmodels

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class UsersViewModel : ViewModel(){

    private val _text = MutableLiveData<String>()
    val text: LiveData<String> get() = _text

    fun setText(newText: String) {
        _text.value = newText
    }
}