package com.golfzon.domain.usecase.matching

import com.golfzon.domain.repository.MatchRepository
import javax.inject.Inject

class GetReactedTeamUseCase @Inject constructor(private val matchRepository: MatchRepository) {
    suspend operator fun invoke() = matchRepository.getReactedTeams()
}