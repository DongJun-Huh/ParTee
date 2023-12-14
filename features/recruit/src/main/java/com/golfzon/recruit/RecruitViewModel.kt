package com.golfzon.recruit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfzon.core_ui.Event
import com.golfzon.core_ui.ListLiveData
import com.golfzon.domain.model.Recruit
import com.golfzon.domain.model.User
import com.golfzon.domain.usecase.member.GetUserInfoUseCase
import com.golfzon.domain.usecase.recruit.GetRecruitDetailUseCase
import com.golfzon.domain.usecase.recruit.GetRecruitsUseCase
import com.golfzon.domain.usecase.recruit.RequestCreateRecruitUseCase
import com.golfzon.domain.usecase.recruit.RequestParticipateRecruitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class RecruitViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val createRecruitUseCase: RequestCreateRecruitUseCase,
    private val getRecruitsUseCase: GetRecruitsUseCase,
    private val getRecruitDetailUseCase: GetRecruitDetailUseCase,
    private val participateRecruitUseCase: RequestParticipateRecruitUseCase
) : ViewModel() {
    private val _isCreateRecruitSuccess = MutableLiveData<Event<Boolean>>()
    val isCreateRecruitSuccess: LiveData<Event<Boolean>> get() = _isCreateRecruitSuccess

    private val _recruits = ListLiveData<Recruit>()
    val recruits: ListLiveData<Recruit> get() = _recruits

    private val _curRecruitDetail = MutableLiveData<Event<Recruit>>()
    val curRecruitDetail: LiveData<Event<Recruit>> get() = _curRecruitDetail

    private val _recruitsMembers = ListLiveData<List<Pair<User, Boolean>>>()
    val recruitsMembers: ListLiveData<List<Pair<User, Boolean>>> get() = _recruitsMembers

    private val _recruitMembers = ListLiveData<User>()
    val recruitMembers: ListLiveData<User> get() = _recruitMembers

    private val _isParticipateSuccess = MutableLiveData<Event<Boolean>>()
    val isParticipateSuccess: LiveData<Event<Boolean>> get() = _isParticipateSuccess

    fun getRecruitsMembersInfo(recruitsMembersUId: List<List<String>>) = viewModelScope.launch {
        _recruitsMembers.replaceAll(
            recruitsMembersUId.map { recruitMembers ->
                recruitMembers.map {
                    val curUserInfo = getUserInfoUseCase(it)
                    Pair(curUserInfo.first, curUserInfo.second)
                }
            },
            true
        )
    }

    fun getRecruitMembersInfo(recruitMembersUId: List<String>) = viewModelScope.launch {
        _recruitMembers.replaceAll(
            recruitMembersUId.map {
                val curUserInfo = getUserInfoUseCase(it)
                curUserInfo.first
            },
            true
        )
    }

    fun createRecruit(recruitInfo: Recruit) = viewModelScope.launch {
        createRecruitUseCase(recruitInfo).let { isSuccess ->
            _isCreateRecruitSuccess.postValue(Event(isSuccess))
        }
    }

    fun getRecruits() = viewModelScope.launch {
        getRecruitsUseCase().let {
            _recruits.replaceAll(it, true)
        }
    }

    fun getRecruitDetail(recruitUId: String) = viewModelScope.launch {
        getRecruitDetailUseCase(recruitUId).let {
            _curRecruitDetail.postValue(Event(it))
        }
    }

    fun participateRecruit(recruitUId: String) = viewModelScope.launch {
        participateRecruitUseCase(recruitUId = recruitUId).let { isSuccess ->
            _isParticipateSuccess.postValue(Event(isSuccess))
        }
    }
}