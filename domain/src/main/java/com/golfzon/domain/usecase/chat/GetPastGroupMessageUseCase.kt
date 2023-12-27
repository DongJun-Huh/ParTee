package com.golfzon.domain.usecase.chat

import com.golfzon.domain.repository.ChatRepository
import javax.inject.Inject

class GetPastGroupMessageUseCase @Inject constructor(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(groupUId: String) =
        chatRepository.getPastMessages(groupUId = groupUId)
}