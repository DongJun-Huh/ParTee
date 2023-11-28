package com.golfzon.login.ui

import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import dagger.hilt.android.lifecycle.HiltViewModel
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.firebase.ui.auth.data.model.FirebaseAuthUIAuthenticationResult
import com.golfzon.core_ui.Event
import com.golfzon.domain.usecase.member.RequestLoginUseCase
import com.golfzon.domain.usecase.member.RequestRegisterUseCase
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class LoginViewModel @Inject constructor(
    private val requestRegisterUseCase: RequestRegisterUseCase,
    private val requestLoginUseCase: RequestLoginUseCase
) : ViewModel() {
    private val _loginSuccess = MutableLiveData<Event<Boolean>>()
    val loginSuccess: LiveData<Event<Boolean>> get() = _loginSuccess

    private val _isInitializeNeed = MutableLiveData<Event<Boolean>>()
    private val _isRegisterSuccess = MutableLiveData<Event<Boolean>>()
    private val _isUserInitialized = MediatorLiveData<Event<Boolean>>().apply {
        var isInitialized = false
        var isRegistered = false

        val checkInitialized: () -> Unit = {
            if (_isInitializeNeed.isInitialized && _isRegisterSuccess.isInitialized) {
                value = Event(isInitialized && isRegistered)
            }
        }

        addSource(_isInitializeNeed) {
            isInitialized = !it.peekContent()
            checkInitialized()
        }
        addSource(_isRegisterSuccess) {
            isRegistered = it.peekContent()
            checkInitialized()
        }
    }
    val isUserInitialized: LiveData<Event<Boolean>> get() = _isUserInitialized

    fun onGoogleLoginResult(result: FirebaseAuthUIAuthenticationResult) {
        if (result.resultCode == AppCompatActivity.RESULT_OK) {
            val user = FirebaseAuth.getInstance().currentUser
            if (user != null && !user.email.isNullOrEmpty()) {
                requestLogin(userUId = user.uid, userEmail = user.email!!)
                _loginSuccess.postValue(Event(true))
            } else {
                _loginSuccess.postValue(Event(false))
            }
        } else {
            _loginSuccess.postValue(Event(false))
            // Sign in failed. If response is null the user canceled the
            // sign-in flow using the back button. Otherwise check
            // response.getError().getErrorCode() and handle the error
        }
    }

    private fun requestLogin(userUId: String, userEmail: String) = viewModelScope.launch {
        val loginUserInfo = requestLoginUseCase(userUId, userEmail)
        // 가입이 안되어있으면 가입하고, 가입이 되어있으면 가입할 필요가 없으니까 가입 성공 여부 체크하는 부분 true로 처리
        if (loginUserInfo.first == false) {
            requestRegister(userUId, userEmail)
        } else {
            _isRegisterSuccess.postValue(Event(true))
        }

        _isInitializeNeed.postValue(Event(loginUserInfo.second == null))
    }

    private fun requestRegister(userUId: String, userEmail: String) = viewModelScope.launch {
        requestRegisterUseCase(UId = userUId, email = userEmail).let {
            _isRegisterSuccess.postValue(Event(it))
        }
    }
}