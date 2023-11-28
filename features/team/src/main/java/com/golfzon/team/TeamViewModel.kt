package com.golfzon.team

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.golfzon.core_ui.Event
import com.golfzon.domain.model.Team
import com.golfzon.domain.usecase.team.GetUserTeamInfoUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class TeamViewModel @Inject constructor(
    private val getUserTeamInfoUseCase: GetUserTeamInfoUseCase
) : ViewModel() {
    private val _teamInfo = MutableLiveData<Event<Team>>()
    val teamInfo: LiveData<Event<Team>> get() = teamInfo

    suspend fun getTeamInfo() {
        getUserTeamInfoUseCase().let { curTeam ->
            _teamInfo.postValue(Event(curTeam))
        }
    }
}