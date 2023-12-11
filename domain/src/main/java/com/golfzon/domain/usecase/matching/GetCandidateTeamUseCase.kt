package com.golfzon.domain.usecase.matching

import com.golfzon.domain.repository.MatchRepository
import javax.inject.Inject

class GetCandidateTeamUseCase @Inject constructor(private val matchRepository: MatchRepository) {
    suspend operator fun invoke(searchingHeadCount: Int, searchingDays: String, searchingTimes: String, reactedTeams: List<String>) =
        matchRepository.getCandidateTeams(searchingHeadCount = searchingHeadCount, searchingDays, searchingTimes, reactedTeams = reactedTeams)
}