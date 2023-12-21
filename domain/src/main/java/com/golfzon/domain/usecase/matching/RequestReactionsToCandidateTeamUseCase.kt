package com.golfzon.domain.usecase.matching

import com.golfzon.domain.model.Group
import com.golfzon.domain.model.GroupScreenRoomInfo
import com.golfzon.domain.repository.GroupRepository
import com.golfzon.domain.repository.MatchRepository
import com.golfzon.domain.repository.TeamRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import javax.inject.Inject

class RequestReactionsToCandidateTeamUseCase @Inject constructor(
    private val matchRepository: MatchRepository,
    private val groupRepository: GroupRepository,
    private val teamRepository: TeamRepository
) {
    suspend operator fun invoke(candidateTeamUId: String, isLike: Boolean) =
        matchRepository.requestReactionsToCandidateTeam(
            candidateTeamUId = candidateTeamUId,
            isLike = isLike
        ).let { isMatched ->
            if (isMatched) {
                try {
                    val curUserTeam = teamRepository.getUserTeamInfoDetail() ?: throw Exception(
                        "GET current user team information failed"
                    )
                    val candidateTeam = teamRepository.getTeamInfoDetail(candidateTeamUId)

                    val groupUId = groupRepository.requestCreateGroup(
                        Group(
                            groupUId = "",
                            originalTeamsInfo = listOf(curUserTeam, candidateTeam),
                            headCount = curUserTeam.headCount + candidateTeam.headCount,
                            membersUId = curUserTeam.membersUId + candidateTeam.membersUId,
                            locations = when {
                                "전국" in curUserTeam.searchingLocations.toSet() -> candidateTeam.searchingLocations
                                "전국" in candidateTeam.searchingLocations.toSet() -> curUserTeam.searchingLocations
                                else -> curUserTeam.searchingLocations.toSet()
                                    .intersect(candidateTeam.searchingLocations.toSet()).toList()
                            },
                            days = candidateTeam.searchingDays,
                            times = candidateTeam.searchingTimes,
                            openChatUrl = curUserTeam.openChatUrl,
                            createdTimeStamp = System.currentTimeMillis(),
                            screenRoomInfo = GroupScreenRoomInfo(
                                screenRoomUId = "",
                                screenRoomPlaceName = "",
                                screenRoomPlaceUId = "",
                                screenRoomPlaceRoadAddress = "",
                                screenRoomPlacePastAddress = "",
                                screenRoomDateTime = LocalDateTime.now()
                            )
                        )
                    )
                    return@let groupUId
                } catch (e: Exception) {
                    return@let ""
                }
            }
            return@let ""
        }
}