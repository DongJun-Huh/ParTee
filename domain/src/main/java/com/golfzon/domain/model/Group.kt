package com.golfzon.domain.model

import java.time.LocalDateTime

data class Group(
    val groupUId: String,
    val originalTeamsInfo: List<Team>,
    val headCount: Int,
    val membersUId: List<String>,
    val membersInfo: List<User>?= null,
    val locations: List<String>,
    val days: String,
    val times: String,
    val openChatUrl: String,
    val createdTimeStamp: Long,
    val screenRoomInfo: GroupScreenRoomInfo
)

data class GroupScreenRoomInfo(
    val screenRoomUId: String,
    val screenRoomPlaceName: String,
    val screenRoomPlaceUId: String,
    val screenRoomPlaceRoadAddress: String,
    val screenRoomPlacePastAddress: String,
    val screenRoomDateTime: LocalDateTime,
)

fun Group.toMap(): Map<String, Any?> {
    return mapOf(
        "groupUId" to this.groupUId,
        "originalTeamsInfo" to this.originalTeamsInfo,
        "headCount" to this.headCount,
        "membersUId" to this.membersUId,
        "locations" to this.locations,
        "days" to this.days,
        "times" to this.times,
        "openChatUrl" to this.openChatUrl,
        "createdTimeStamp" to this.createdTimeStamp,
        "screenRoomInfo" to this.screenRoomInfo,
    )
}

fun GroupScreenRoomInfo.toMap(): Map<String, Any?> {
    return mapOf(
        "screenRoomUId" to this.screenRoomUId,
        "screenRoomPlaceName" to this.screenRoomPlaceName,
        "screenRoomPlaceUId" to this.screenRoomPlaceUId,
        "screenRoomPlaceRoadAddress" to this.screenRoomPlaceRoadAddress,
        "screenRoomPlacePastAddress" to this.screenRoomPlacePastAddress,
        "screenRoomDateTime" to this.screenRoomDateTime
    )
}
