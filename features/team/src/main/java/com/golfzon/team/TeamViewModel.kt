package com.golfzon.team

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfzon.domain.model.Team
import com.golfzon.domain.model.TeamInfo
import com.golfzon.domain.usecase.team.GetUserTeamInfoBriefUseCase
import com.golfzon.domain.usecase.team.GetUserTeamInfoDetailUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val getUserTeamInfoBriefUseCase: GetUserTeamInfoBriefUseCase,
    private val getUserTeamInfoDetailUseCase: GetUserTeamInfoDetailUseCase
) : ViewModel() {
    private val _teamInfoBrief = MutableLiveData<TeamInfo>()
    val teamInfoBrief: LiveData<TeamInfo> get() = _teamInfoBrief

    private val _teamInfoDetail = MutableLiveData<Team?>()
    val teamInfoDetail: LiveData<Team?> get() = _teamInfoDetail

    fun getTeamInfo() = viewModelScope.launch {
        getUserTeamInfoDetailUseCase().let { curTeam ->
            _teamInfoDetail.postValue(curTeam)
        }
    }
}