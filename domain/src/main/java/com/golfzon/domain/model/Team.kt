package com.golfzon.domain.model

data class Team(
    val teamUId: String,
    val teamName: String,
    val teamImageUrl: String,
    val leaderUId: String,
    val membersUId: List<String>,
    val headCount: Int,
    val searchingHeadCount: Int,
    val searchingTimes: String,
    val searchingDays: String,
    val searchingLocations: List<String>,
    val openChatUrl: String,
    val totalAge: Int,
    val totalYearsPlaying: Int,
    val totalAverage: Int,
    var priorityScore: Int,
)