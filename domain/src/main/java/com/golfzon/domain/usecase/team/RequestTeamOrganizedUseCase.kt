package com.golfzon.domain.usecase.team

import com.golfzon.domain.model.Team
import com.golfzon.domain.repository.TeamRepository
import javax.inject.Inject

class RequestTeamOrganizedUseCase @Inject constructor(private val teamRepository: TeamRepository) {
    suspend operator fun invoke(newTeam: Team) =
        teamRepository.requestTeamOrganize(newTeam = newTeam)
}