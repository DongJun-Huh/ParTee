package com.golfzon.domain.model

data class Team(
    val teamUId: String,
    val leaderUId: String,
    val membersUId: List<String>,
    val headCount: Int,
    val searchingHeadCount: Int,
    val searchingTimes: String,
    val openChatUrl: String
)