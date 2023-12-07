package com.golfzon.matching

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfzon.core_ui.Event
import com.golfzon.core_ui.ListLiveData
import com.golfzon.domain.model.Team
import com.golfzon.domain.model.User
import com.golfzon.domain.usecase.matching.GetCandidateTeamUseCase
import com.golfzon.domain.usecase.matching.GetReactedTeamUseCase
import com.golfzon.domain.usecase.matching.RequestReactionsToCandidateTeamUseCase
import com.golfzon.domain.usecase.member.GetCurUserInfoUseCase
import com.golfzon.domain.usecase.member.GetUserInfoUseCase
import com.golfzon.domain.usecase.team.GetTeamInfoDetailUseCase
import com.golfzon.domain.usecase.team.GetUserTeamInfoDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MatchingViewModel @Inject constructor(
    private val getUserTeamInfoDetailUseCase: GetUserTeamInfoDetailUseCase,
    private val getTeamInfoDetailUseCase: GetTeamInfoDetailUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val getCurUserInfoUseCase: GetCurUserInfoUseCase,
    private val getReactedTeamUseCase: GetReactedTeamUseCase,
    private val getCandidateTeamUseCase: GetCandidateTeamUseCase,
    private val requestReactionsToCandidateTeamUseCase: RequestReactionsToCandidateTeamUseCase
) : ViewModel() {

    private val _currentUserBasicInfo = MutableLiveData<Triple<String, String, String>>()
    val currentUserBasicInfo: LiveData<Triple<String, String, String>> get() = _currentUserBasicInfo

    private val _teamInfoDetail = MutableLiveData<Team>()
    val teamInfoDetail: LiveData<Team> get() = _teamInfoDetail

    private val _teamUsers = ListLiveData<Triple<User, Boolean, String>>()
    val teamUsers: ListLiveData<Triple<User, Boolean, String>> get() = _teamUsers

    private val _curCandidateTeam = MutableLiveData<Team>()
    val curCandidateTeam: LiveData<Team> get() = _curCandidateTeam

    private val _curCandidateTeamMembers = ListLiveData<User>()
    val curCandidateTeamMembers: ListLiveData<User> get() = _curCandidateTeamMembers

    private val _isSuccessMatching = MutableLiveData<Event<Boolean>>()
    val isSuccessMatching: LiveData<Event<Boolean>> get() = _isSuccessMatching

    private val _candidateTeams = ListLiveData<Team>()
    val candidateTeams: ListLiveData<Team> get() = _candidateTeams
    val curCandidateTeamIndex = MutableLiveData<Int>(0)
    // TODO candidateTeams의 사이즈 == curCandidateTeamIndex -1 이 되면 MatchingFragment에서 더이상 후보가 존재하지 않음 노출 구현 및 리액션 기능 비활성화 처리

    val curSearchingHeadCount = MutableLiveData<Int>(1)

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

    fun getCandidateTeamMembersInfo(membersUId: List<String>) = viewModelScope.launch {
        for (memberUId in membersUId) {
            getUserInfoUseCase(memberUId).let {
                _curCandidateTeamMembers.add(it.first, true)
            }
        }
    }

    fun getCurrentUserInfo() = viewModelScope.launch {
        getCurUserInfoUseCase().let {
            _currentUserBasicInfo.postValue(it)
        }
    }

    fun getFilteredCandidateTeams() = viewModelScope.launch {
        getReactedTeamUseCase()?.let {
            getCandidateTeams(it)
        }
    }

    private fun getCandidateTeams(reactedTeams: List<String>) = viewModelScope.launch {
        getCandidateTeamUseCase(curSearchingHeadCount.value ?: 1, reactedTeams)?.let {
            _candidateTeams.replaceAll(it, true)
            _curCandidateTeam.postValue(_candidateTeams.value!![0])
        }
    }

    fun reactionsToCandidateTeam(isLike: Boolean) = viewModelScope.launch {
        requestReactionsToCandidateTeamUseCase(
            candidateTeamUId =
            _candidateTeams.value!!.get(curCandidateTeamIndex.value!!).teamUId, isLike = isLike
        )?.let {
            _isSuccessMatching.postValue(Event(it))
            curCandidateTeamIndex.value = curCandidateTeamIndex.value!! + 1
            _curCandidateTeam.postValue(_candidateTeams.value!![curCandidateTeamIndex.value!!])
        }
    }

    fun addCurSearchingHeadCount() {
        curSearchingHeadCount.value?.let { headCount ->
            if (headCount < 3) curSearchingHeadCount.value = headCount.plus(1)
        }
    }

    fun minusCurSearchingHeadCount() {
        curSearchingHeadCount.value?.let { headCount ->
            if (headCount > 1) curSearchingHeadCount.value = headCount.minus(1)
        }
    }
}