package com.golfzon.domain.repository

import com.golfzon.domain.model.Days
import com.golfzon.domain.model.Team
import com.golfzon.domain.model.Times

interface MatchRepository {
    suspend fun getCandidateTeams(searchingHeadCount: Int, searchingDays: Days, searchingTimes: Times, reactedTeams: List<String>): List<Team>
    suspend fun requestReactionsToCandidateTeam(candidateTeamUId: String, isLike: Boolean): Boolean
    suspend fun getReactedTeams(): List<String>
}