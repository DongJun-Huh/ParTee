package com.golfzon.matching

import android.widget.RadioGroup
import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
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
import kotlin.math.abs

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

    private val _teamInfoDetail = MutableLiveData<Team?>()
    val teamInfoDetail: LiveData<Team?> get() = _teamInfoDetail

    private val _teamUsers = ListLiveData<Triple<User, Boolean, String>>()
    val teamUsers: ListLiveData<Triple<User, Boolean, String>> get() = _teamUsers

    private val _isCurCandidateTeamExist = MutableLiveData<Event<Boolean>>()
    val isCurCandidateTeamExist: LiveData<Event<Boolean>> get() = _isCurCandidateTeamExist

    private val _curCandidateTeam = MutableLiveData<Event<Team>>()
    val curCandidateTeam: LiveData<Event<Team>> get() = _curCandidateTeam

    private val _curCandidateTeamMembers = ListLiveData<User>()
    val curCandidateTeamMembers: ListLiveData<User> get() = _curCandidateTeamMembers

    private val _isSuccessMatching = MutableLiveData<Event<Boolean>>()
    val isSuccessMatching: LiveData<Event<Boolean>> get() = _isSuccessMatching

    private val _successTeamInfo = MutableLiveData<Team>()
    val successTeamInfo: LiveData<Team> get() = _successTeamInfo

    private val _candidateTeams = ListLiveData<Team>()
    val candidateTeams: ListLiveData<Team> get() = _candidateTeams
    val curCandidateTeamIndex = MutableLiveData<Int>(0)

    // TODO candidateTeams의 사이즈 == curCandidateTeamIndex -1 이 되면 MatchingFragment에서 더이상 후보가 존재하지 않음 노출 구현 및 리액션 기능 비활성화 처리
    private val _isCandidateEnd = MutableLiveData<Event<Boolean>>()
    val isCandidateEnd: LiveData<Event<Boolean>> get() = _isCandidateEnd

    val curSearchingHeadCount = MutableLiveData<Int>(1)
    val curSearchingTimes = MutableLiveData<String>()
    val curSearchingTimesCheckedButtonId = MutableLiveData<Int>()
    val curSearchingDays = MutableLiveData<String>()
    val curSearchingDaysCheckedButtonId = MutableLiveData<Int>()
    private val _isConditionChecked = MediatorLiveData<Event<Boolean>>().apply {
        var isTimesChecked = false
        var isDaysChecked = false

        val checkInitialized: () -> Unit = {
            if (curSearchingTimes.isInitialized && curSearchingDays.isInitialized) {
                value = Event(isDaysChecked && isTimesChecked)
            }
        }

        addSource(curSearchingTimes) {
            isTimesChecked = it != ""
            checkInitialized()
        }
        addSource(curSearchingDays) {
            isDaysChecked = it != ""
            checkInitialized()
        }
    }
    val isConditionChecked: LiveData<Event<Boolean>> get() = _isConditionChecked

    fun getTeamInfo() = viewModelScope.launch {
        getUserTeamInfoDetailUseCase().let { curTeam ->
            _teamInfoDetail.postValue(curTeam)
        }
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

    fun getCandidateTeamMembersInfo(membersUId: List<String>) = viewModelScope.launch {
        _curCandidateTeamMembers.replaceAll(membersUId.map { getUserInfoUseCase(it).first }, true)
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
        getCandidateTeamUseCase(
            curSearchingHeadCount.value ?: 1,
            curSearchingDays.value ?: "",
            curSearchingTimes.value ?: "",
            reactedTeams
        )?.let {
            val priorityOrderedTeams = it.map { candidateTeam ->
                val averageScore =
                    (8 - (abs(_teamInfoDetail.value!!.totalAverage - candidateTeam.totalAverage) / 10)).getPreventedMinusScore
                val yearsPlayingScore =
                    (8 - (abs(_teamInfoDetail.value!!.totalYearsPlaying - candidateTeam.totalYearsPlaying) / 3)).getPreventedMinusScore
                val locationScore = candidateTeam.searchingLocations.toSet()
                    .intersect(_teamInfoDetail.value!!.searchingLocations.toSet()).size
                val ageScore =
                    (8 - (abs(_teamInfoDetail.value!!.totalAge - candidateTeam.totalAge) / 5)).getPreventedMinusScore
                candidateTeam.copy(
                    priorityScore = ageScore + yearsPlayingScore + averageScore + locationScore
                )
            }.toMutableList().sortedByDescending { it.priorityScore }

            _candidateTeams.replaceAll(priorityOrderedTeams, true)
            if (it.isNotEmpty()) {
                _curCandidateTeam.postValue(Event(priorityOrderedTeams[0]))
                _isCurCandidateTeamExist.postValue(Event(true))
            } else {
                _isCurCandidateTeamExist.postValue(Event(false))
            }
        }
    }

    fun reactionsToCandidateTeam(isLike: Boolean) = viewModelScope.launch {
        if (curCandidateTeamIndex.value!! < _candidateTeams.value!!.size) {
            requestReactionsToCandidateTeamUseCase(
                candidateTeamUId =
                _candidateTeams.value!!.get(curCandidateTeamIndex.value!!).teamUId, isLike = isLike
            )?.let { isSuccess ->
                _isSuccessMatching.postValue(Event(isSuccess))
                if (isSuccess) {
                    _successTeamInfo.postValue(
                        _curCandidateTeam.value?.peekContent()?.copy(
                            searchingLocations = _teamInfoDetail.value?.searchingLocations?.toSet()
                                ?.intersect(
                                    (_curCandidateTeam.value?.peekContent()?.searchingLocations
                                        ?: listOf()).toSet()
                                )
                                ?.toList() ?: listOf()
                        )
                    )
                }

                if (curCandidateTeamIndex.value!! != _candidateTeams.value!!.size - 1) {
                    curCandidateTeamIndex.value = curCandidateTeamIndex.value!! + 1
                    _curCandidateTeam.postValue(Event(_candidateTeams.value!![curCandidateTeamIndex.value!!]))
                    _isCandidateEnd.postValue(Event(false))
                } else {
                    _isCandidateEnd.postValue(Event(true))
                }
            }
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

    fun changeDays(group: RadioGroup, checkedId: Int) {
        curSearchingDaysCheckedButtonId.value = checkedId
        curSearchingDays.value = when (checkedId) {
            R.id.rb_matching_filtering_days_weekdays -> "주중"
            R.id.rb_matching_filtering_days_weekend -> "주말"
            else -> ""
        }
    }

    fun changeTimes(group: RadioGroup, checkedId: Int) {
        curSearchingTimesCheckedButtonId.value = checkedId
        curSearchingTimes.value = when (checkedId) {
            R.id.rb_matching_filtering_times_morning -> "오전"
            R.id.rb_matching_filtering_times_afternoon -> "오후"
            R.id.rb_matching_filtering_times_night -> "야간"
            R.id.rb_matching_filtering_times_dawn -> "새벽"
            else -> ""
        }
    }

    private val Int.getPreventedMinusScore: Int get() = if (this <= 0) 0 else this
}