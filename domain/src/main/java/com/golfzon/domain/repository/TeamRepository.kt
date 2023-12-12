package com.golfzon.domain.repository

import com.golfzon.domain.model.Team
import com.golfzon.domain.model.TeamInfo
import java.io.File

interface TeamRepository {
    suspend fun getUserTeamInfoBrief(): TeamInfo
    suspend fun getUserTeamInfoDetail(): Team?
    suspend fun getTeamInfoDetail(teamUId: String): Team
    suspend fun requestTeamOrganize(newTeam: Team, teamImg: File? = null): String
    suspend fun deleteTeam(teamUId: String): Boolean
}