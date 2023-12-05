package com.golfzon.domain.model

data class Group(
    val groupUId: String,
    val headCount: Int,
    val membersId: List<String>,
    val times: String,
    val openChatUrl: String
)
