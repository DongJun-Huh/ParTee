package com.golfzon.domain.repository

import com.golfzon.domain.model.Team
import com.golfzon.domain.model.TeamInfo

interface TeamRepository {
    suspend fun getUserTeamInfoBrief(): TeamInfo
    suspend fun getUserTeamInfoDetail(): Team?
    suspend fun getTeamInfoDetail(teamUId: String): Team
    suspend fun requestTeamOrganize(newTeam: Team): String
}