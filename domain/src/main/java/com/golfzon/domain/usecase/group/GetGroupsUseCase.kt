package com.golfzon.domain.usecase.group

import com.golfzon.domain.repository.GroupRepository
import javax.inject.Inject

class GetGroupsUseCase @Inject constructor(private val groupRepository: GroupRepository) {
    suspend operator fun invoke() =
        groupRepository.getGroups()
}