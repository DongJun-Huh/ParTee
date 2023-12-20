package com.golfzon.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.golfzon.data.common.FireStoreHelper.getGroupCollection
import com.golfzon.data.common.FireStoreHelper.getGroupDocument
import com.golfzon.data.common.FireStoreHelper.getUserGroupInfoDocument
import com.golfzon.data.common.Lg
import com.golfzon.data.common.TimestampDeserializer
import com.golfzon.data.extension.readValue
import com.golfzon.data.extension.toDataClass
import com.golfzon.data.extension.toTimestamp
import com.golfzon.domain.model.Group
import com.golfzon.domain.model.GroupScreenRoomInfo
import com.golfzon.domain.model.toMap
import com.golfzon.domain.repository.GroupRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

class GroupRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dataStore: DataStore<Preferences>
) : GroupRepository {
    override suspend fun requestCreateGroup(newGroup: Group): String = withContext(Dispatchers.IO) {
        // LocalDateTime -> TimeStamp 변환
        val newDocumentRef =
            getGroupCollection(firestore).add(newGroup.toMap().toMutableMap().apply {
                set(
                    "screenRoomInfo", newGroup.screenRoomInfo.toMap()
                        .toMutableMap().apply {
                            set(
                                "screenRoomDateTime",
                                newGroup.screenRoomInfo.screenRoomDateTime.toTimestamp
                            )
                        }
                )
            }).await()
        newGroup.originalTeamsInfo.forEach { team ->
            team.membersUId.forEach { memberUId ->
                getUserGroupInfoDocument(firestore, memberUId)
                    .update("groupsUId", FieldValue.arrayUnion(newDocumentRef.id)).await()
            }
        }

        newDocumentRef.id
    }


    override suspend fun getGroups(): List<Group> = withContext(Dispatchers.IO) {
        val curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "") ?: ""
        val groupsUId = try {
            getUserGroupInfoDocument(firestore, curUserUId)
                .get()
                .await()
                .data?.get("groupsUId") as? List<String> ?: emptyList()
        } catch (e: Exception) {
            Lg.e(e)
            emptyList()
        }

        val groupsInfo = groupsUId.mapNotNull { groupUId ->
            try {
                val group = getGroupDocument(firestore, groupUId).get().await()
                group.data?.toDataClass<Group, LocalDateTime>(jsonDeserializer = TimestampDeserializer())
                    ?.copy(groupUId = groupUId)
            } catch (e: Exception) {
                Lg.e(e)
                null
            }
        }.sortedByDescending { it.createdTimeStamp }

        groupsInfo
    }


    override suspend fun getGroupDetail(groupUId: String): Group = withContext(Dispatchers.IO) {
        try {
            // TimeStamp -> LocalDateTime 변환
            getGroupDocument(firestore, groupUId).get().await()
                .data?.toDataClass<Group, LocalDateTime>(jsonDeserializer = TimestampDeserializer()) as Group
        } catch (e: Exception) {
            throw e
        }
    }

    override suspend fun createGroupScreenRoom(
        groupUId: String,
        screenRoomInfo: GroupScreenRoomInfo
    ): Boolean = withContext(Dispatchers.IO) {
        try {
            val convertedScreenRoomInfo = screenRoomInfo.toMap().toMutableMap().apply {
                this["screenRoomDateTime"] = screenRoomInfo.screenRoomDateTime.toTimestamp
            }

            firestore.collection("groups").document(groupUId)
                .update("screenRoomInfo", convertedScreenRoomInfo).await()
            true
        } catch (e: Exception) {
            Lg.e(e)
            false
        }
    }
}
