package com.golfzon.recruit

import android.content.Context
import android.content.Intent
import com.golfzon.core_ui.navigation.DeeplinkProcessor
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class RecruitDeeplinkProcessor @Inject constructor(
    private val context: Context
) : DeeplinkProcessor {

    override fun matches(deeplink: String): Boolean {
        return deeplink.contains(context.getString(com.golfzon.core_ui.R.string.recruit_deeplink_url))
    }

    override fun execute(deeplink: String) {
        val intent = Intent(context, RecruitActivity::class.java)
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        context.startActivity(intent)
    }
}