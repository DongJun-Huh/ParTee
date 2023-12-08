package com.golfzon.domain.usecase.team

import com.golfzon.domain.repository.TeamRepository
import javax.inject.Inject

class DeleteTeamUseCase @Inject constructor(private val teamRepository: TeamRepository) {
    suspend operator fun invoke(teamUId: String) =
        teamRepository.deleteTeam(teamUId = teamUId)
}