package com.golfzon.domain.model

data class Group(
    val groupUId: String,
    val originalTeamsInfo: List<Team>,
    val headCount: Int,
    val membersId: List<String>,
    val locations: List<String>,
    val days: String,
    val times: String,
    val openChatUrl: String
)
