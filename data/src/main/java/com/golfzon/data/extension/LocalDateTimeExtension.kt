package com.golfzon.data.extension

import com.google.firebase.Timestamp
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date

val LocalDateTime.toTimestamp
    get() = Timestamp(
        Date.from(
            this.atZone(ZoneId.systemDefault()).toInstant()
        )
    )