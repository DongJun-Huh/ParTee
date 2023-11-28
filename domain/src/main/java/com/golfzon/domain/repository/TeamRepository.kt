package com.golfzon.domain.repository

import com.golfzon.domain.model.Team

interface TeamRepository {
    suspend fun getTeamInfo(): Team
}