package com.golfzon.domain.repository

import com.golfzon.domain.model.Team

interface MatchRepository {
    suspend fun getCandidateTeams(searchingHeadCount: Int): List<Team>
}