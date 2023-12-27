package com.golfzon.domain.usecase.chat

import com.golfzon.domain.model.GroupMessage
import com.golfzon.domain.repository.ChatRepository
import com.golfzon.domain.repository.OnGrpMessageResponse
import javax.inject.Inject

class SendGroupMessageUseCase @Inject constructor(private val chatRepository: ChatRepository) {
    suspend operator fun invoke(message: GroupMessage, listener: OnGrpMessageResponse) =
        chatRepository.sendMessage(message = message, listener = listener)
}