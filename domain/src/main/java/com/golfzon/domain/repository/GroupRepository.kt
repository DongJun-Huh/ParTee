package com.golfzon.domain.repository

import com.golfzon.domain.model.Group
import com.golfzon.domain.model.GroupScreenRoomInfo

interface GroupRepository {
    suspend fun requestCreateGroup(group: Group): String
    suspend fun getGroups(): List<Group>
    suspend fun getGroupDetail(groupUId: String): Group
    suspend fun createGroupScreenRoom(groupUId: String, screenRoomInfo: GroupScreenRoomInfo): Boolean
}