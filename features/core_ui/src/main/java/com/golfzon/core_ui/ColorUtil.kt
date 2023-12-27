package com.golfzon.core_ui

val Int.getColorHex get() = String.format("#%06X", 0xFFFFFF and this)