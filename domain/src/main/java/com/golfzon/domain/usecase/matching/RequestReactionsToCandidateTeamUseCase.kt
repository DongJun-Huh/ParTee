package com.golfzon.domain.usecase.matching

import com.golfzon.domain.model.Group
import com.golfzon.domain.repository.GroupRepository
import com.golfzon.domain.repository.MatchRepository
import com.golfzon.domain.repository.TeamRepository
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
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
                CoroutineScope(Dispatchers.IO).launch {
                    val curUserTeam = teamRepository.getUserTeamInfoDetail()!!
                    val candidateTeam = teamRepository.getTeamInfoDetail(candidateTeamUId)
                    groupRepository.requestCreateGroup(
                        Group(
                            groupUId = "",
                            originalTeamsInfo = listOf(curUserTeam, candidateTeam),
                            headCount = curUserTeam.headCount + candidateTeam.headCount,
                            membersUId = curUserTeam.membersUId + candidateTeam.membersUId,
                            locations = curUserTeam.searchingLocations.toSet()
                                .intersect(candidateTeam.searchingLocations).toList(),
                            days = candidateTeam.searchingDays,
                            times = candidateTeam.searchingTimes,
                            openChatUrl = curUserTeam.openChatUrl,
                            createdTimeStamp = System.currentTimeMillis()
                        )
                    )
                }
            }
            return@let isMatched
        }
}