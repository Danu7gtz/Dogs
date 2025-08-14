package com.example.dogs.net

import androidx.lifecycle.MutableLiveData

object NetworkEvents {
    val lastRequest  = MutableLiveData<String>()
    val lastResponse = MutableLiveData<String>()
}