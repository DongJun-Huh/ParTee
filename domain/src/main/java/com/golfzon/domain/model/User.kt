package com.golfzon.domain.model

data class User (
    val userUId: String,
    val email: String,
    val nickname: String?,
    val age: Int?,
    val yearsPlaying: Int?,
    val average: Int?,
    val introduceMessage: String?,
    val profileImg: String?,
    val userInfo: UserInfo
)

data class UserInfo (
    val teamInfo: TeamInfo,
    val groupsInfo: List<GroupInfo>,
    val themeTeamsInfo: List<ThemeTeamInfo>
)

data class TeamInfo (
    val teamUId: String?,
    val isOrganized: Boolean,
    val isLeader: Boolean
)

data class GroupInfo (
    val groupUId: String?
)

data class ThemeTeamInfo (
    val themeTeamUId: String?
)