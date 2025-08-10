package com.example.dogs.net

import androidx.lifecycle.MutableLiveData

object NetworkEvents {
    val lastRequest  = MutableLiveData<String>()  // método, url, headers (opcional)
    val lastResponse = MutableLiveData<String>()  // código, body (texto JSON)
}