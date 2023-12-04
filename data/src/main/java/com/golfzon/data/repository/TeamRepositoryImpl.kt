package com.golfzon.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import com.golfzon.data.extension.readValue
import com.golfzon.data.extension.storeValue
import com.golfzon.domain.model.Team
import com.golfzon.domain.model.TeamInfo
import com.golfzon.domain.repository.TeamRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import kotlin.coroutines.resume
import javax.inject.Inject

class TeamRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val dataStore: DataStore<Preferences>
) : TeamRepository {

    // TODO 임시로 curUserUId 기본값으로 들어가있는 saGsdRTdEfeJklbHjIBHGpGRZdj1 삭제
    override suspend fun getUserTeamInfoBrief(): TeamInfo {
        return suspendCancellableCoroutine { continuation ->
            var curUserUId = "saGsdRTdEfeJklbHjIBHGpGRZdj1"

            CoroutineScope(Dispatchers.IO).launch {
                curUserUId = dataStore.readValue(
                    stringPreferencesKey("userUId"),
                    "saGsdRTdEfeJklbHjIBHGpGRZdj1"
                ) ?: ""

                firestore.collection("users")
                    .document(curUserUId)
                    .collection("extraInfo")
                    .document("teamInfo")
                    .get()
                    .addOnSuccessListener { teamBrief ->
                        teamBrief.data?.let {
                            val curTeamBrief = TeamInfo(
                                teamUId = it["teamUId"] as String?,
                                isLeader = it["isLeader"] as Boolean,
                                isOrganized = it["isOrganized"] as Boolean
                            )

                            // 데이터 저장을 바로 진행
                            CoroutineScope(Dispatchers.IO).launch {
                                withContext(Dispatchers.Main) {
                                    with(dataStore) {
                                        storeValue(
                                            stringPreferencesKey("teamUId"),
                                            curTeamBrief.teamUId
                                        )
                                        storeValue(
                                            booleanPreferencesKey("isLeader"),
                                            curTeamBrief.isLeader
                                        )
                                        storeValue(
                                            booleanPreferencesKey("isOrganized"),
                                            curTeamBrief.isOrganized
                                        )
                                    }
                                    if (continuation.isActive) continuation.resume(curTeamBrief)
                                }
                            }
                        }
                    }
            }
        }
    }

    override suspend fun getUserTeamInfoDetail(): Team? {
        return suspendCancellableCoroutine { continuation ->
            CoroutineScope(Dispatchers.IO).launch {
                val curUserUId = dataStore.readValue(
                    stringPreferencesKey("userUId"),
                    "saGsdRTdEfeJklbHjIBHGpGRZdj1"
                ) ?: ""
                getUserTeamInfoBrief().let {
                    if (it.teamUId == null) {
                        if (continuation.isActive) {
                            continuation.resume(
                                Team(
                                    teamName = "팀 이름",
                                    teamImageUrl = "",
                                    leaderUId = curUserUId,
                                    membersUId = listOf(curUserUId),
                                    headCount = 1,
                                    searchingTimes = "",
                                    searchingLocations = listOf(),
                                    openChatUrl = "",
                                    searchingHeadCount = 0
                                )
                            )
                        }
                    } else {

                        firestore.collection("teams")
                            .document(it.teamUId!!)
                            .get()
                            .addOnSuccessListener { teamDetail ->
                                if (continuation.isActive) {
                                    continuation.resume(
                                        Team(
                                            teamName = teamDetail["teamName"] as String,
                                            teamImageUrl = teamDetail["teamImageUrl"] as String,
                                            leaderUId = teamDetail["leaderUId"] as String,
                                            membersUId = teamDetail["membersUId"] as List<String>,
                                            headCount = (teamDetail["headCount"] as Long).toInt(),
                                            searchingHeadCount = (teamDetail["searchingHeadCount"] as Long).toInt(),
                                            searchingTimes = teamDetail["searchingTimes"] as String,
                                            searchingLocations = teamDetail["searchingLocations"] as List<String>,
                                            openChatUrl = teamDetail["openChatUrl"] as String
                                        )
                                    )
                                }
                            }
                    }
                }
            }
        }
    }

    override suspend fun requestTeamOrganize(newTeamTemp: Team): String =
        suspendCancellableCoroutine { continuation ->
            var curUserUId = ""
            runBlocking {
                curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "saGsdRTdEfeJklbHjIBHGpGRZdj1") ?: ""
            }

            // TODO 이미지 설정 기능 추가시 제거
            val newTeam = newTeamTemp.copy(
                teamImageUrl = "TEMP URL"
            )

            firestore.collection("users")
                .document(curUserUId)
                .collection("extraInfo")
                .document("teamInfo")
                .get()
                .addOnSuccessListener { curUserTeamInfo ->
                    if (curUserTeamInfo.data!!["isOrganized"] as Boolean) {
                        firestore.collection("teams")
                            .document(curUserTeamInfo.data!!["teamUId"] as String)
                            .set(newTeam)
                            .addOnSuccessListener {
                                // TODO 하단과 동일한 Boilerplate Code 제거
                                val newTeamInfo = hashMapOf(
                                    "teamUId" to curUserTeamInfo.data!!["teamUId"] as String,
                                    "isLeader" to false,
                                    "isOrganized" to true
                                )

                                val tasks = mutableListOf<Task<*>>()
                                for (addedUser in newTeam.membersUId) {
                                    val memberUserEditTask = firestore.collection("users")
                                        .document(addedUser)
                                        .collection("extraInfo")
                                        .document("teamInfo")
                                        .set(newTeamInfo)
                                    tasks.add(memberUserEditTask)
                                }
                                newTeamInfo["isLeader"] = true

                                val leaderUserEditTask = firestore.collection("users")
                                    .document(newTeam.leaderUId)
                                    .collection("extraInfo")
                                    .document("teamInfo")
                                    .set(newTeamInfo.apply { "isLeader" to true })
                                tasks.add(leaderUserEditTask)
                                Tasks.whenAll(tasks)
                                    .addOnSuccessListener {
                                        if (continuation.isActive) continuation.resume(
                                            curUserTeamInfo.data!!["teamUId"] as String
                                        )
                                    }
                            }
                    } else {
                        firestore.collection("teams")
                            .add(newTeam.copy(leaderUId = curUserUId))
                            .addOnSuccessListener { newTeamDocument ->
                                val newTeamInfo = hashMapOf(
                                    "teamUId" to newTeamDocument.id,
                                    "isLeader" to false,
                                    "isOrganized" to true
                                )

                                val tasks = mutableListOf<Task<*>>()
                                for (addedUser in newTeam.membersUId) {
                                    val memberUserEditTask = firestore.collection("users")
                                        .document(addedUser)
                                        .collection("extraInfo")
                                        .document("teamInfo")
                                        .set(newTeamInfo)
                                    tasks.add(memberUserEditTask)
                                }
                                newTeamInfo["isLeader"] = true

                                val leaderUserEditTask = firestore.collection("users")
                                    .document(newTeam.leaderUId)
                                    .collection("extraInfo")
                                    .document("teamInfo")
                                    .set(newTeamInfo.apply { "isLeader" to true })
                                tasks.add(leaderUserEditTask)
                                Tasks.whenAll(tasks)
                                    .addOnSuccessListener {
                                        if (continuation.isActive) continuation.resume(
                                            newTeamDocument.id
                                        )
                                    }
                            }
                    }
                }
        }
}