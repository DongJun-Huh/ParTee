package com.golfzon.team

import android.content.Context
import android.content.Intent
import com.golfzon.core_ui.navigation.DeeplinkProcessor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class TeamDeeplinkProcessor @Inject constructor(
    private val context: Context
) : DeeplinkProcessor {

    override fun matches(deeplink: String): Boolean {
        return deeplink.contains(context.getString(com.golfzon.core_ui.R.string.team_deeplink_url))
    }

    override fun execute(deeplink: String) {
        val extraData = deeplink.split("${context.getString(com.golfzon.core_ui.R.string.team_deeplink_url)}/").getOrNull(1)
        val intent = Intent(context, TeamActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.putExtra("extraData",extraData)
        context.startActivity(intent)
    }
}