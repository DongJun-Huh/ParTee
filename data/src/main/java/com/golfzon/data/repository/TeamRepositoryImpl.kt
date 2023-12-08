package com.golfzon.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
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

    override suspend fun getUserTeamInfoBrief(): TeamInfo {
        return suspendCancellableCoroutine { continuation ->
            var curUserUId = ""

            CoroutineScope(Dispatchers.IO).launch {
                curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "") ?: ""

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
                val curUserUId =
                    dataStore.readValue(stringPreferencesKey("userUId"), "") ?: ""
                getUserTeamInfoBrief().let {
                    if (it.teamUId == null) {
                        if (continuation.isActive) {
                            continuation.resume(null)
                        }
                    } else {

                        firestore.collection("teams")
                            .document(it.teamUId!!)
                            .get()
                            .addOnSuccessListener { teamDetail ->
                                if (continuation.isActive) {
                                    CoroutineScope(Dispatchers.IO).launch {
                                        with(dataStore) {
                                            storeValue(
                                                intPreferencesKey("teamHeadCount"),
                                                (teamDetail["headCount"] as Long).toInt()
                                            )
                                            storeValue(
                                                stringSetPreferencesKey("searchingLocations"),
                                                (teamDetail["searchingLocations"] as List<String>).toSet()
                                            )
                                            storeValue(
                                                stringPreferencesKey("searchingTimes"),
                                                (teamDetail["searchingTimes"] as String)
                                            )
                                            storeValue(
                                                stringPreferencesKey("searchingDays"),
                                                (teamDetail["searchingDays"] as String)
                                            )
                                            storeValue(
                                                stringPreferencesKey("teamUId"),
                                                (it.teamUId)
                                            )
                                        }
                                    }

                                    continuation.resume(
                                        Team(
                                            teamUId = it.teamUId!!,
                                            teamName = teamDetail["teamName"] as String,
                                            teamImageUrl = teamDetail["teamImageUrl"] as String,
                                            leaderUId = teamDetail["leaderUId"] as String,
                                            membersUId = teamDetail["membersUId"] as List<String>,
                                            headCount = (teamDetail["headCount"] as Long).toInt(),
                                            searchingHeadCount = (teamDetail["searchingHeadCount"] as Long).toInt(),
                                            searchingTimes = teamDetail["searchingTimes"] as String,
                                            searchingDays = teamDetail["searchingDays"] as String,
                                            searchingLocations = teamDetail["searchingLocations"] as List<String>,
                                            openChatUrl = teamDetail["openChatUrl"] as String,
                                            totalAge = (teamDetail["totalAge"] as Long).toInt(),
                                            totalAverage = (teamDetail["totalAverage"] as Long).toInt(),
                                            totalYearsPlaying = (teamDetail["totalYearsPlaying"] as Long).toInt(),
                                            priorityScore = 0
                                        )
                                    )
                                }
                            }
                    }
                }
            }
        }
    }

    override suspend fun getTeamInfoDetail(teamUId: String): Team {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection("teams")
                .document(teamUId)
                .get()
                .addOnSuccessListener {
                    it.data?.let { teamDetail ->
                        continuation.resume(
                            Team(
                                teamUId = teamUId,
                                teamName = teamDetail["teamName"] as String,
                                teamImageUrl = teamDetail["teamImageUrl"] as String,
                                leaderUId = teamDetail["leaderUId"] as String,
                                membersUId = teamDetail["membersUId"] as List<String>,
                                headCount = (teamDetail["headCount"] as Long).toInt(),
                                searchingHeadCount = (teamDetail["searchingHeadCount"] as Long).toInt(),
                                searchingDays = teamDetail["searchingDays"] as String,
                                searchingTimes = teamDetail["searchingTimes"] as String,
                                searchingLocations = teamDetail["searchingLocations"] as List<String>,
                                openChatUrl = teamDetail["openChatUrl"] as String,
                                totalAge = (teamDetail["totalAge"] as Long).toInt(),
                                totalAverage = (teamDetail["totalAverage"] as Long).toInt(),
                                totalYearsPlaying = (teamDetail["totalYearsPlaying"] as Long).toInt(),
                                priorityScore = 0
                            )
                        )
                    }
                }
        }
    }

    override suspend fun requestTeamOrganize(newTeamTemp: Team): String =
        suspendCancellableCoroutine { continuation ->
            var curUserUId = ""
            runBlocking {
                curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "") ?: ""
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

                                firestore.collection("likes")
                                    .document(newTeamDocument.id)
                                    .set(
                                        hashMapOf(
                                            "likes" to listOf<String>(),
                                            "dislikes" to listOf<String>()
                                        )
                                    )

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

    override suspend fun deleteTeam(teamUId: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            CoroutineScope(Dispatchers.IO).launch {
                firestore.collection("likes")
                    .document(teamUId)
                    .delete()
            }

            firestore.collection("teams")
                .document(teamUId)
                .get()
                .addOnSuccessListener { teamDocument ->
                    teamDocument.data?.let {
                        val teamMembers = (it["membersUId"] as List<String>)
                        val deleteTasks = mutableListOf<Task<*>>()
                        for (teamMember in teamMembers) {
                            val deleteTask = firestore.collection("users")
                                .document(teamMember)
                                .collection("extraInfo")
                                .document("teamInfo")
                                .update(
                                    mapOf(
                                        "isLeader" to false,
                                        "isOrganized" to false,
                                        "teamUId" to null
                                    )
                                )
                            deleteTasks.add(deleteTask)
                        }

                        Tasks.whenAll(deleteTasks)
                            .addOnSuccessListener {
                                CoroutineScope(Dispatchers.IO).launch {
                                    firestore.collection("teams")
                                        .document(teamUId)
                                        .delete()
                                }
                                if (continuation.isActive) continuation.resume(true)
                            }
                    }
                }
        }
    }
}