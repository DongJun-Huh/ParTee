package com.golfzon.matching

import android.content.Context
import android.content.Intent
import com.golfzon.core_ui.navigation.DeeplinkProcessor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class MatchingDeeplinkProcessor@Inject constructor(
    private val context: Context
) : DeeplinkProcessor {

    override fun matches(deeplink: String): Boolean {
        return deeplink.contains(context.getString(com.golfzon.core_ui.R.string.matching_deeplink_url))
    }

    override fun execute(deeplink: String) {
        val intent = Intent(context, MatchingActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        intent.addFlags(Intent.FLAG_ACTIVITY_NO_ANIMATION)
        context.startActivity(intent)
    }
}