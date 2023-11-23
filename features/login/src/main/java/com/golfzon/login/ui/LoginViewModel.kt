package com.golfzon.login.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfzon.core_ui.Event
import kotlinx.coroutines.launch

class LoginViewModel: ViewModel() {
    private val _loginSuccess = MutableLiveData<Event<Boolean>>()
    val loginSuccess : LiveData<Event<Boolean>> get() = _loginSuccess

    fun requestLoginEmail(email: String, password: String) = viewModelScope.launch {

    }
}