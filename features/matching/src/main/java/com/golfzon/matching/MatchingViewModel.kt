package com.golfzon.matching

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfzon.core_ui.Event
import com.golfzon.core_ui.ListLiveData
import com.golfzon.domain.model.Days
import com.golfzon.domain.model.Team
import com.golfzon.domain.model.Times
import com.golfzon.domain.model.User
import com.golfzon.domain.usecase.matching.GetCandidateTeamUseCase
import com.golfzon.domain.usecase.matching.GetReactedTeamUseCase
import com.golfzon.domain.usecase.matching.RequestReactionsToCandidateTeamUseCase
import com.golfzon.domain.usecase.member.GetCurUserInfoUseCase
import com.golfzon.domain.usecase.member.GetUserInfoUseCase
import com.golfzon.domain.usecase.team.GetUserTeamInfoDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject
import kotlin.math.abs

@HiltViewModel
class MatchingViewModel @Inject constructor(
    private val getUserTeamInfoDetailUseCase: GetUserTeamInfoDetailUseCase,
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

    private val _curCandidateTeam = MutableLiveData<Event<MatchingCardModel>>()
    val curCandidateTeam: LiveData<Event<MatchingCardModel>> get() = _curCandidateTeam

    private val _curCandidateTeamMembers = ListLiveData<User>()
    val curCandidateTeamMembers: ListLiveData<User> get() = _curCandidateTeamMembers

    private val _nextCandidateTeamMembers = ListLiveData<User>()
    val nextCandidateTeamMembers: ListLiveData<User> get() = _nextCandidateTeamMembers

    private val _createdGroupId = MutableLiveData<Event<String>>()
    val createdGroupId: LiveData<Event<String>> get() = _createdGroupId

    private val _successTeamInfo = MutableLiveData<Team>()
    val successTeamInfo: LiveData<Team> get() = _successTeamInfo

    private val _candidateTeams = ListLiveData<Team>()
    val curCandidateTeamIndex = MutableLiveData<Int>(0)

    private val _isCandidateEnd = MutableLiveData<Event<Boolean>>()
    val isCandidateEnd: LiveData<Event<Boolean>> get() = _isCandidateEnd

    val curSearchingHeadCount = MutableLiveData<Int>(1)
    val curSearchingTimes = MutableLiveData<Times>(Times.NONE)
    val curSearchingTimesCheckedButtonId = MutableLiveData<Int>()
    val curSearchingDays = MutableLiveData<Days>(Days.NONE)
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
            isTimesChecked = it != Times.NONE
            checkInitialized()
        }
        addSource(curSearchingDays) {
            isDaysChecked = it != Days.NONE
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

    fun clearCurrentCandidateTeamMembers() {
        _curCandidateTeamMembers.clear(true)
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

    fun getNextCandidateTeamMembersInfo(membersUId: List<String>) = viewModelScope.launch {
        _nextCandidateTeamMembers.replaceAll(membersUId.map { getUserInfoUseCase(it).first }, true)
    }

    fun getCurrentUserInfo() = viewModelScope.launch {
        getCurUserInfoUseCase().let {
            _currentUserBasicInfo.postValue(it)
        }
    }

    fun getFilteredCandidateTeams() = viewModelScope.launch {
        getReactedTeamUseCase()?.let {
            filteringCandidateTeams(it)
        }
    }

    private val topCard
        get() = _candidateTeams.value!![curCandidateTeamIndex.value!! % _candidateTeams.value!!.size]
    private val bottomCard
        get() = _candidateTeams.value!![(curCandidateTeamIndex.value!! + 1) % _candidateTeams.value!!.size]

    private fun filteringCandidateTeams(reactedTeams: List<String>) = viewModelScope.launch {
        getCandidateTeamUseCase(
            curSearchingHeadCount.value ?: 1,
            curSearchingDays.value ?: Days.NONE,
            curSearchingTimes.value ?: Times.NONE,
            reactedTeams
        )?.let {
            val priorityOrderedTeams = it.calculatePriorityScores

            _candidateTeams.replaceAll(priorityOrderedTeams, true)
            if (it.isNotEmpty()) {
                updateCards()
                _isCurCandidateTeamExist.postValue(Event(true))
            } else {
                _isCurCandidateTeamExist.postValue(Event(false))
            }
        }
    }

    private fun updateCards() {
        _curCandidateTeam.postValue(
            Event(
                MatchingCardModel(
                    cardTop = topCard,
                    cardBottom = bottomCard
                )
            )
        )
    }

    private val List<Team>.calculatePriorityScores
        get() = this.map { candidateTeam ->
            candidateTeam.calculatePriorityScore(_teamInfoDetail.value!!)
        }.toMutableList().sortedByDescending { it.priorityScore }

    private fun Team.calculatePriorityScore(candidateTeam: Team): Team {
        val averageScore = calculateScore(candidateTeam.totalAverage, this.totalAverage, 10)
        val yearsPlayingScore =
            calculateScore(candidateTeam.totalYearsPlaying, this.totalYearsPlaying, 3)
        val locationScore = candidateTeam.searchingLocations.toSet()
            .intersect(this.searchingLocations.toSet()).size
        val ageScore = calculateScore(candidateTeam.totalAge, this.totalAge, 5)

        return this.copy(
            priorityScore = ageScore
                    + yearsPlayingScore
                    + averageScore
                    + locationScore
        )
    }

    private fun calculateScore(teamDetailValue: Int, candidateTeamValue: Int, divisor: Int): Int =
        (8 - (abs(teamDetailValue - candidateTeamValue) / divisor)).getPreventedMinusScore


    fun reactionsToCandidateTeam(isLike: Boolean) = viewModelScope.launch {
        val teamIndex = curCandidateTeamIndex.value ?: return@launch
        val teams = _candidateTeams.value ?: return@launch
        if (teamIndex >= teams.size) return@launch

        val currentTeam = teams[teamIndex]
        requestReactionsToCandidateTeamUseCase(currentTeam.teamUId, isLike)?.let { successInfo ->
            updateCandidateTeamIndex(teamIndex, teams)
            handleReactionResult(successInfo, currentTeam)
        }
    }

    private fun handleReactionResult(successInfo: String, currentCandidateTeam: Team) {
        if (successInfo.isNotEmpty()) {
            setSuccessTeamInfo(currentCandidateTeam)
            _createdGroupId.postValue(Event(successInfo))
        }
    }

    private fun setSuccessTeamInfo(currentTeam: Team) {
        _teamInfoDetail.value?.let { curUserTeam ->
            when {
                "전국" in curUserTeam.searchingLocations.toSet() -> currentTeam.searchingLocations
                "전국" in currentTeam.searchingLocations.toSet() -> curUserTeam.searchingLocations
                else -> curUserTeam.searchingLocations.toSet()
                    .intersect(currentTeam.searchingLocations.toSet()).toList()
            }
        }?.let { intersectedLocations ->
            _successTeamInfo.postValue(
                currentTeam.copy(searchingLocations = intersectedLocations.toList())
            )
        }
    }

    private fun updateCandidateTeamIndex(teamIndex: Int, teams: List<Team>) {
        if (teamIndex < teams.size - 1) {
            curCandidateTeamIndex.value = teamIndex + 1
            updateCards()
//            _curCandidateTeam.postValue(Event(teams[teamIndex + 1]))
            _isCandidateEnd.postValue(Event(false))
        } else {
            _isCandidateEnd.postValue(Event(true))
        }
    }

    fun updateHeadCount(delta: Int) {
        curSearchingHeadCount.value?.let { headCount ->
            val newCount =
                (headCount + delta).coerceIn(1, 4 - (teamInfoDetail.value?.headCount ?: 3))
            curSearchingHeadCount.value = newCount
        }
    }

    fun changeDays(checkedId: Int) {
        curSearchingDaysCheckedButtonId.value = checkedId
        curSearchingDays.value = when (checkedId) {
            R.id.rb_matching_filtering_days_weekdays -> Days.WEEKDAY
            R.id.rb_matching_filtering_days_weekend -> Days.WEEKEND
            else -> Days.NONE
        }
    }

    fun changeTimes(checkedId: Int) {
        curSearchingTimesCheckedButtonId.value = checkedId
        curSearchingTimes.value = when (checkedId) {
            R.id.rb_matching_filtering_times_morning -> Times.MORNING
            R.id.rb_matching_filtering_times_afternoon -> Times.AFTERNOON
            R.id.rb_matching_filtering_times_night -> Times.NIGHT
            R.id.rb_matching_filtering_times_dawn -> Times.DAWN
            else -> Times.NONE
        }
    }

    private val Int.getPreventedMinusScore: Int get() = if (this <= 0) 0 else this
}