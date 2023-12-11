package com.golfzon.domain.repository

import com.golfzon.domain.model.Team

interface MatchRepository {
    suspend fun getCandidateTeams(searchingHeadCount: Int, searchingDays: String, searchingTimes: String, reactedTeams: List<String>): List<Team>
    suspend fun requestReactionsToCandidateTeam(candidateTeamUId: String, isLike: Boolean): Boolean
    suspend fun getReactedTeams(): List<String>
}