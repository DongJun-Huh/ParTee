package com.golfzon.domain.repository

import com.golfzon.domain.model.GroupMessage

interface OnGrpMessageResponse{
    fun onSuccess(message: GroupMessage)
    fun onFailed(message: GroupMessage)
}
interface ChatRepository {
    suspend fun sendMessage(message: GroupMessage, listener: OnGrpMessageResponse)
    suspend fun receiveMessages(groupUId: String, callback: (List<GroupMessage>) -> Unit): () -> Unit
}