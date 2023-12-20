package com.golfzon.domain.usecase.group

import com.golfzon.domain.model.GroupScreenRoomInfo
import com.golfzon.domain.repository.GroupRepository
import javax.inject.Inject

class CreateScreenRoomUseCase @Inject constructor(private val groupRepository: GroupRepository) {
    suspend operator fun invoke(groupUId: String, newGroupScreenInfo: GroupScreenRoomInfo) =
        groupRepository.createGroupScreenRoom(groupUId, newGroupScreenInfo)
}