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

fun User.toMap(): Map<String, Any?> {
    return mapOf(
        "email" to this.email,
        "nickname" to this.nickname,
        "age" to this.age,
        "yearsPlaying" to this.yearsPlaying,
        "average" to this.average,
        "introduceMessage" to this.introduceMessage,
        "profileImg" to this.profileImg,
    )
}

data class UserInfo (
    val teamInfo: TeamInfo,
    val groupsInfo: List<GroupInfo>,
    val recruitsInfo: List<RecruitsInfo>
)

data class TeamInfo (
    val teamUId: String?,
    val isOrganized: Boolean,
    val isLeader: Boolean
)

data class GroupInfo (
    val groupUId: String?
)

data class RecruitsInfo (
    val recruitsUId: String?
)