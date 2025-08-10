package com.example.dogs.ui

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.LiveData
import com.example.dogs.data.AppointmentRepository
import com.example.dogs.domain.Appointment
import com.example.dogs.notifications.NotificationUtils
import kotlinx.coroutines.launch
import java.time.format.DateTimeFormatter

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel


class AgendaViewModel(app: Application) : AndroidViewModel(app) {
    private val repo = AppointmentRepository(app)
    val appointments: LiveData<List<Appointment>> = repo.data
    fun init() = viewModelScope.launch { repo.refresh().onFailure { it.printStackTrace() } }
    fun add(a: Appointment) = viewModelScope.launch { repo.add(a) }
    fun update(a: Appointment) = viewModelScope.launch { repo.update(a) }
    fun delete(id: String) = viewModelScope.launch { repo.delete(id) }
}
