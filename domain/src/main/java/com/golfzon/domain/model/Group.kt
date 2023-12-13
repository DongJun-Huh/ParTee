package com.golfzon.domain.model

data class Group(
    val groupUId: String,
    val originalTeamsInfo: List<Team>,
    val headCount: Int,
    val membersUId: List<String>,
    val locations: List<String>,
    val days: String,
    val times: String,
    val openChatUrl: String,
    val createdTimeStamp: Long,
)
