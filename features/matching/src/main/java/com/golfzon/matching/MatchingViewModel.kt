package com.golfzon.matching

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfzon.core_ui.ListLiveData
import com.golfzon.domain.model.Team
import com.golfzon.domain.model.User
import com.golfzon.domain.usecase.member.GetCurUserInfoUseCase
import com.golfzon.domain.usecase.member.GetUserInfoUseCase
import com.golfzon.domain.usecase.team.GetUserTeamInfoDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchingViewModel @Inject constructor(
    private val getUserTeamInfoDetailUseCase: GetUserTeamInfoDetailUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getCurUserInfoUseCase: GetCurUserInfoUseCase
): ViewModel() {

    private val _currentUserBasicInfo = MutableLiveData<Triple<String,String,String>>()
    val currentUserBasicInfo: LiveData<Triple<String,String,String>> get() = _currentUserBasicInfo

    private val _teamInfoDetail = MutableLiveData<Team>()
    val teamInfoDetail: LiveData<Team> get() = _teamInfoDetail

    private val _teamUsers = ListLiveData<Triple<User, Boolean, String>>()
    val teamUsers: ListLiveData<Triple<User, Boolean, String>> get() = _teamUsers
    fun getTeamInfo() = viewModelScope.launch {
        getUserTeamInfoDetailUseCase().let { curTeam ->
            _teamInfoDetail.postValue(curTeam)
        }
    }

    fun clearUserInfo() {
        _teamUsers.clear(true)
    }

    fun getTeamMemberInfo(UId: String, leaderUId: String) = viewModelScope.launch {
        getUserInfoUseCase(UId).let {
            _teamUsers.add(Triple(it.first, it.second, leaderUId), true)
        }
    }

    fun getCurrentUserInfo() = viewModelScope.launch {
        getCurUserInfoUseCase().let {
            _currentUserBasicInfo.postValue(it)
        }
    }
}