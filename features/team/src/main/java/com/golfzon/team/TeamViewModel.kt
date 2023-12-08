package com.golfzon.team

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfzon.core_ui.Event
import com.golfzon.core_ui.ListLiveData
import com.golfzon.domain.model.Team
import com.golfzon.domain.model.TeamInfo
import com.golfzon.domain.model.User
import com.golfzon.domain.usecase.member.GetCurUserInfoUseCase
import com.golfzon.domain.usecase.member.GetSearchedUsersUseCase
import com.golfzon.domain.usecase.member.GetUserInfoUseCase
import com.golfzon.domain.usecase.team.DeleteTeamUseCase
import com.golfzon.domain.usecase.team.GetUserTeamInfoBriefUseCase
import com.golfzon.domain.usecase.team.GetUserTeamInfoDetailUseCase
import com.golfzon.domain.usecase.team.RequestTeamOrganizedUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val getCurUserInfoUseCase: GetCurUserInfoUseCase,
    private val getUserTeamInfoBriefUseCase: GetUserTeamInfoBriefUseCase,
    private val getUserTeamInfoDetailUseCase: GetUserTeamInfoDetailUseCase,
    private val getSearchedUsersUseCase: GetSearchedUsersUseCase,
    private val requestTeamOrganizedUseCase: RequestTeamOrganizedUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val deleteTeamUseCase: DeleteTeamUseCase
) : ViewModel() {
    private val _teamInfoBrief = MutableLiveData<TeamInfo>()
    val teamInfoBrief: LiveData<TeamInfo> get() = _teamInfoBrief

    private val _teamInfoDetail = MutableLiveData<Team?>()
    val teamInfoDetail: LiveData<Team?> get() = _teamInfoDetail

    private val _searchedUsers = ListLiveData<User>()
    val searchedUsers: ListLiveData<User> get() = _searchedUsers

    private val _teamUsers = ListLiveData<Triple<User, Boolean, String>>()
    val teamUsers: ListLiveData<Triple<User, Boolean, String>> get() = _teamUsers

    private val _newTeam = MutableLiveData<Team>()
    val newTeam: LiveData<Team> get() = _newTeam

    private val _isTeamOrganizeSuccess = MutableLiveData<Event<Boolean>>()
    val isTeamOrganizeSuccess: LiveData<Event<Boolean>> get() = _isTeamOrganizeSuccess

    private val _isTeamDeleteSuccess = MutableLiveData<Event<Boolean>>()
    val isTeamDeleteSuccess: LiveData<Event<Boolean>> get() = _isTeamDeleteSuccess

    fun getNewTeamInfo() = viewModelScope.launch {
        getUserTeamInfoDetailUseCase().let {
            if (it != null) _newTeam.postValue(it)
            else {
                var curUserUId = ""
                var curUserAge = 0
                var curUserYearsPlaying = 0
                var curUserAverage = 0

                curUserUId = getCurUserInfoUseCase().first
                getUserInfoUseCase(curUserUId).first.let {
                    curUserAge = it.age ?: 0
                    curUserYearsPlaying = it.yearsPlaying ?: 0
                    curUserAverage = it.average ?: 0
                }

                _newTeam.postValue(
                    Team(
                        teamUId = "",
                        teamName = "팀 이름",
                        teamImageUrl = "",
                        leaderUId = curUserUId,
                        membersUId = listOf(curUserUId),
                        headCount = 1,
                        searchingTimes = "",
                        searchingDays = "",
                        searchingLocations = listOf(),
                        openChatUrl = "",
                        searchingHeadCount = 0,
                        totalAge = curUserAge,
                        totalYearsPlaying = curUserYearsPlaying,
                        totalAverage = curUserAverage,
                        priorityScore = 0
                    )
                )
            }
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

    fun searchUsers(nickname: String) = viewModelScope.launch {
        getSearchedUsersUseCase(nickname = nickname).let {
            val results = it.filter { user ->
                !(_newTeam.value?.membersUId?.contains(user.userUId) ?: true)
            }
            _searchedUsers.replaceAll(results, true)
        }
    }

    fun clearSearchedUsers() {
        _searchedUsers.clear(true)
    }

    fun organizeTeam() = viewModelScope.launch {
        requestTeamOrganizedUseCase(
            // TODO teamName, teamImageUrl, leaderUId, membersUId, headCount, searchingTimes, searchingLocations, openChatUrl만 설정
            newTeam = _newTeam.value!!
        )?.let {
            _isTeamOrganizeSuccess.postValue(Event(true))
        }
    }

    fun addTeamMember(
        newUserUId: String,
        newUserAge: Int,
        newUserYearsPlaying: Int,
        newUserAverage: Int
    ) = viewModelScope.launch {
        _newTeam.postValue(
            _newTeam.value?.let {
                it.copy(
                    membersUId = it.membersUId + listOf(newUserUId),
                    headCount = it.headCount + 1,
                    totalAge = it.totalAge + newUserAge,
                    totalYearsPlaying = it.totalYearsPlaying + newUserYearsPlaying,
                    totalAverage = it.totalAverage + newUserAverage
                )
            }
        )
    }

    fun setLocation(locations: List<String>) = viewModelScope.launch {
        _newTeam.postValue(
            _newTeam.value?.let {
                it.copy(
                    searchingLocations = locations
                )
            }
        )
    }

    fun changeTeamName(newTeamName: String) = viewModelScope.launch {
        _newTeam.postValue(
            _newTeam.value?.let {
                it.copy(
                    teamName = newTeamName
                )
            }
        )
    }

    fun deleteTeam() = viewModelScope.launch {
        _newTeam.value?.let {
            if (it.teamUId.isNotEmpty()) {
                deleteTeamUseCase(it.teamUId).let {
                    _isTeamDeleteSuccess.postValue(Event(it))
                }
            } else {
                _isTeamDeleteSuccess.postValue(Event(true))
            }
        }
    }
}