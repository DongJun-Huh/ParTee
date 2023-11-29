package com.golfzon.domain.usecase.team

import com.golfzon.domain.repository.TeamRepository
import javax.inject.Inject

class GetUserTeamInfoBriefUseCase @Inject constructor(private val teamRepository: TeamRepository) {
    suspend operator fun invoke() =
        teamRepository.getUserTeamInfoBrief()
}