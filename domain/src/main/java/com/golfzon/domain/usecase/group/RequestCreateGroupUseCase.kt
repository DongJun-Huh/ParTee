package com.golfzon.domain.usecase.group

import com.golfzon.domain.model.Group
import com.golfzon.domain.repository.GroupRepository
import javax.inject.Inject

class RequestCreateGroupUseCase @Inject constructor(private val groupRepository: GroupRepository) {
    suspend operator fun invoke(group: Group) =
        groupRepository.requestCreateGroup(group = group)
}