package com.golfzon.domain.usecase.chat

import com.golfzon.domain.model.GroupMessage
import com.golfzon.domain.repository.ChatRepository
import javax.inject.Inject

class GetGroupMessageUseCase @Inject constructor(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(groupUId: String, onDataUpdate: (GroupMessage) -> Unit) {
        chatRepository.receiveMessages(groupUId = groupUId, callback = onDataUpdate)
    }
}