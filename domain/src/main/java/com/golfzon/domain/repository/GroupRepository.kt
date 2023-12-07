package com.golfzon.domain.repository

import com.golfzon.domain.model.Group

interface GroupRepository {
    suspend fun requestCreateGroup(group: Group): String
}