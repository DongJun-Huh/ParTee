package com.golfzon.domain.model

import java.time.LocalDateTime

data class Recruit(
    val recruitUId: String,
    val leaderUId: String,
    val membersUId: List<String>,
    val headCount: Int,
    val searchingHeadCount: Int,
    val recruitDateTime: LocalDateTime,
    val recruitPlaceName: String,
    val recruitPlaceUId: String,
    val recruitPlaceRoadAddress: String,
    val recruitPlacePastAddress: String,
    val recruitEndDateTime: LocalDateTime,
    val openChatUrl: String,
    val fee: Int,
    val isConsecutiveStay: Boolean,
    val isCouple: Boolean,
    val recruitIntroduceMessage: String,
)