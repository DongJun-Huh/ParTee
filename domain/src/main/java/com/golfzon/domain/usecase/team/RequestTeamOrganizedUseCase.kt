package com.golfzon.domain.usecase.team

import com.golfzon.domain.model.Team
import com.golfzon.domain.repository.TeamRepository
import java.io.File
import javax.inject.Inject

class RequestTeamOrganizedUseCase @Inject constructor(private val teamRepository: TeamRepository) {
    suspend operator fun invoke(newTeamWithNoImage: Team, teamImg: File?) =
        teamRepository.requestTeamOrganize(newTeamWithNoImage, teamImg)
}