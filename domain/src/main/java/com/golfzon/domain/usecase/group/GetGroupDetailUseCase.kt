package com.golfzon.domain.usecase.group

import com.golfzon.domain.repository.GroupRepository
import javax.inject.Inject

class GetGroupDetailUseCase  @Inject constructor(private val groupRepository: GroupRepository) {
    suspend operator fun invoke(groupUId: String) =
        groupRepository.getGroupDetail(groupUId = groupUId)
}