package com.golfzon.core_ui.navigation

interface DeeplinkProcessor {
    fun matches(deeplink: String): Boolean

    fun execute(deeplink: String)
}