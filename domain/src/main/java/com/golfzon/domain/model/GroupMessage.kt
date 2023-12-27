package com.golfzon.domain.model

data class GroupMessage(
    val id: String,
    var groupId: String,
    val from: String,
    val to: ArrayList<String>,
    val status: ArrayList<Int>, // 0: sending, 1: sent, 2: delivered, 3: seen
    val deliveryTime: ArrayList<Long>,
    val seenTime: ArrayList<Long>,
    val createdAt: Long,
    var type: ChatMessageType = ChatMessageType.TEXT,//0: text, 1: image, 2: reservation
    var textMessage: TextMessage = TextMessage(text = "Message delivery failed"),
    var placeUId: String = ""
)

data class TextMessage(val text: String)