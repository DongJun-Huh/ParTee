package com.golfzon.domain.usecase.matching

import com.golfzon.domain.repository.MatchRepository
import javax.inject.Inject

class RequestReactionsToCandidateTeamUseCase @Inject constructor(private val matchRepository: MatchRepository) {
    suspend operator fun invoke(candidateTeamUId: String, isLike: Boolean) =
        matchRepository.requestReactionsToCandidateTeam(candidateTeamUId = candidateTeamUId, isLike = isLike)
}