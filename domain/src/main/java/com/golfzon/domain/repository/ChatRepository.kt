package com.golfzon.domain.repository

import com.golfzon.domain.model.GroupMessage
import kotlinx.coroutines.flow.Flow

interface OnGrpMessageResponse{
    fun onSuccess(message: GroupMessage)
    fun onFailed(message: GroupMessage)
}
interface ChatRepository {
    suspend fun sendMessage(message: GroupMessage, listener: OnGrpMessageResponse)
    suspend fun getPastMessages(groupUId: String): Flow<List<GroupMessage>>
    suspend fun receiveMessages(groupUId: String, callback: (GroupMessage) -> Unit): () -> Unit
}