package com.golfzon.core_ui.navigation

interface DeeplinkHandler {
    fun process(deeplink: String): Boolean
}