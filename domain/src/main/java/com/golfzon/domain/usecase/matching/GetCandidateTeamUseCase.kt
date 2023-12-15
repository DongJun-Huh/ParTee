package com.golfzon.domain.usecase.matching

import com.golfzon.domain.model.Days
import com.golfzon.domain.model.Times
import com.golfzon.domain.repository.MatchRepository
import javax.inject.Inject

class GetCandidateTeamUseCase @Inject constructor(private val matchRepository: MatchRepository) {
    suspend operator fun invoke(searchingHeadCount: Int, searchingDays: Days, searchingTimes: Times, reactedTeams: List<String>) =
        matchRepository.getCandidateTeams(searchingHeadCount = searchingHeadCount, searchingDays, searchingTimes, reactedTeams = reactedTeams)
}