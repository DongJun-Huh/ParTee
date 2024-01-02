package com.golfzon.team

import android.graphics.Bitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfzon.core_ui.Event
import com.golfzon.core_ui.ImageUploadUtil
import com.golfzon.core_ui.ListLiveData
import com.golfzon.domain.model.Team
import com.golfzon.domain.model.User
import com.golfzon.domain.usecase.member.GetCurUserInfoUseCase
import com.golfzon.domain.usecase.member.GetSearchedUsersUseCase
import com.golfzon.domain.usecase.member.GetUserInfoUseCase
import com.golfzon.domain.usecase.team.DeleteTeamUseCase
import com.golfzon.domain.usecase.team.GetUserTeamInfoDetailUseCase
import com.golfzon.domain.usecase.team.RequestTeamOrganizedUseCase
import com.google.firebase.ktx.Firebase
import com.google.firebase.perf.ktx.performance
import com.google.firebase.perf.metrics.Trace
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val getCurUserInfoUseCase: GetCurUserInfoUseCase,
    private val getUserTeamInfoDetailUseCase: GetUserTeamInfoDetailUseCase,
    private val getSearchedUsersUseCase: GetSearchedUsersUseCase,
    private val requestTeamOrganizedUseCase: RequestTeamOrganizedUseCase,
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val deleteTeamUseCase: DeleteTeamUseCase
) : ViewModel() {
    private val _searchedUsers = ListLiveData<User>()
    val searchedUsers: ListLiveData<User> get() = _searchedUsers

    private val _teamUsers = ListLiveData<Triple<User, Boolean, String>>()
    val teamUsers: ListLiveData<Triple<User, Boolean, String>> get() = _teamUsers

    private val _newTeam = MutableLiveData<Team>()
    val newTeam: LiveData<Team> get() = _newTeam

    val newTeamImageBitmap = MutableLiveData<Bitmap?>()
    val newTeamImgPath = MutableLiveData<String>()

    private val _isTeamOrganizeSuccess = MutableLiveData<Event<Boolean>>()
    val isTeamOrganizeSuccess: LiveData<Event<Boolean>> get() = _isTeamOrganizeSuccess

    private val _isTeamDeleteSuccess = MutableLiveData<Event<Boolean>>()
    val isTeamDeleteSuccess: LiveData<Event<Boolean>> get() = _isTeamDeleteSuccess
    private val requestChangeTeamInfoTrace: Trace = Firebase.performance.newTrace("request_change_team_info_trace")

    fun getNewTeamInfo() = viewModelScope.launch {
        val teamInfo = getUserTeamInfoDetailUseCase()
        teamInfo?.let {
            _newTeam.postValue(it)
            return@launch
        }

        val curUserUId = getCurUserInfoUseCase().first
        val curUserInfo = getUserInfoUseCase(curUserUId).first

        val newTeam = Team(
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
            totalAge = curUserInfo.age ?: 0,
            totalYearsPlaying = curUserInfo.yearsPlaying ?: 0,
            totalAverage = curUserInfo.average ?: 0,
            priorityScore = 0
        )

        _newTeam.postValue(newTeam)
    }

    fun clearUserInfo() {
        _teamUsers.clear(true)
    }

    fun getTeamMembersInfo(membersUId: List<String>, leaderUId: String) = viewModelScope.launch {
        _teamUsers.replaceAll(membersUId.map {
            val curUserInfo = getUserInfoUseCase(it)
            Triple(curUserInfo.first, curUserInfo.second, leaderUId)
        }, true)
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
        requestChangeTeamInfoTrace.start()
        _newTeam.value?.let { organizeDetail ->
            if (organizeDetail.searchingLocations.isEmpty()
            // TODO 시간및 위치 설정 추가시 체크 조건 설정
//                || organizeDetail.searchingDays.isEmpty() || organizeDetail.searchingTimes.isEmpty()
            ) {
                _isTeamOrganizeSuccess.postValue(Event(false))
                return@let
            } else {
                requestTeamOrganizedUseCase(
                    // teamName, teamImageUrl, leaderUId, membersUId, headCount, searchingTimes, searchingLocations, openChatUrl만 설정
                    newTeamWithNoImage = _newTeam.value!!.copy(
                        teamImageUrl = organizeDetail.teamImageUrl.ifEmpty { "teams_default.png" },
                    ),
                    ImageUploadUtil.bitmapToFile(newTeamImageBitmap.value, newTeamImgPath.value)
                )?.let {
                    requestChangeTeamInfoTrace.putAttribute("team_uid",organizeDetail.teamUId)
                    requestChangeTeamInfoTrace.stop()
                    _isTeamOrganizeSuccess.postValue(Event(true))
                }
            }
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
        val teamToDelete = _newTeam.value
        teamToDelete?.let { team ->
            if (team.teamUId.isNotEmpty()) {
                try {
                    val isDeleted = deleteTeamUseCase(team.teamUId)
                    _isTeamDeleteSuccess.postValue(Event(isDeleted))
                } catch (e: Exception) {
                    e.printStackTrace()
                    _isTeamDeleteSuccess.postValue(Event(false))
                }
            } else {
                _isTeamDeleteSuccess.postValue(Event(true))
            }
        } ?: run {
            _isTeamDeleteSuccess.postValue(Event(true))
        }
    }
}