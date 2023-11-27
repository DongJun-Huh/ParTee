package com.golfzon.login.ui

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfzon.core_ui.Event
import kotlinx.coroutines.launch

@HiltViewModel
class LoginViewModel: ViewModel() {
    val email = MutableLiveData<String>()
    val password = MutableLiveData<String>()

    private val _loginSuccess = MutableLiveData<Event<Boolean>>()
    val loginSuccess : LiveData<Event<Boolean>> get() = _loginSuccess

    fun requestLoginEmail() = viewModelScope.launch {

    }
}