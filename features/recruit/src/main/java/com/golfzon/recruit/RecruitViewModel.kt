package com.golfzon.recruit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfzon.core_ui.Event
import com.golfzon.domain.model.Recruit
import com.golfzon.domain.usecase.recruit.RequestCreateRecruitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecruitViewModel @Inject constructor(
    private val createRecruitUseCase: RequestCreateRecruitUseCase,
) : ViewModel() {
    private val _isCreateRecruitSuccess = MutableLiveData<Event<Boolean>>()
    val isCreateRecruitSuccess: LiveData<Event<Boolean>> get() = _isCreateRecruitSuccess

    fun createRecruit(recruitInfo: Recruit) = viewModelScope.launch {
        createRecruitUseCase(recruitInfo).let { isSuccess ->
            _isCreateRecruitSuccess.postValue(Event(isSuccess))
        }
    }
}