package com.golfzon.chat

import android.content.Context
import android.content.Intent
import com.golfzon.core_ui.navigation.DeeplinkProcessor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class ChatDeeplinkProcessor @Inject constructor(
    private val context: Context
) : DeeplinkProcessor {

    override fun matches(deeplink: String): Boolean {
        return deeplink.contains(context.getString(com.golfzon.core_ui.R.string.chat_deeplink_url))
    }

    override fun execute(deeplink: String) {
        val intent = Intent(context, ChatActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_HISTORY)
        when {
            deeplink.contains(context.getString(com.golfzon.core_ui.R.string.group_reservation_deeplink_url)) -> {
                val groupUId = deeplink.split("${context.getString(com.golfzon.core_ui.R.string.group_reservation_deeplink_url)}/").getOrNull(1)
                intent.putExtra("destination", "reservation")
                intent.putExtra("groupUId", groupUId)
            }
            deeplink.contains(context.getString(com.golfzon.core_ui.R.string.chat_deeplink_url)) -> {
                val groupUId = deeplink.split("${context.getString(com.golfzon.core_ui.R.string.chat_deeplink_url)}/").getOrNull(1)
                intent.putExtra("destination", "chat")
                intent.putExtra("groupUId", groupUId)
            }
        }
        context.startActivity(intent)
    }
}