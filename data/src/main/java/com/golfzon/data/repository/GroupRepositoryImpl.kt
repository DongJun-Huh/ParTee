package com.golfzon.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.golfzon.data.extension.asDeferred
import com.golfzon.data.extension.readValue
import com.golfzon.domain.model.Group
import com.golfzon.domain.model.Team
import com.golfzon.domain.repository.GroupRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class GroupRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val dataStore: DataStore<Preferences>
) : GroupRepository {
    override suspend fun requestCreateGroup(newGroup: Group): String {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection("groups")
                .add(newGroup)
                .addOnSuccessListener { newDocumentRef ->
                    val tasks = mutableListOf<Task<*>>()
                    val teamFirstSetTask = newDocumentRef.collection("originTeamsInfo")
                        .document("teamFirst")
                        .set(newGroup.originalTeamsInfo[0])
                    val teamSecondSetTask = newDocumentRef.collection("originTeamsInfo")
                        .document("teamSecond")
                        .set(newGroup.originalTeamsInfo[1])

                    for (userUId in newGroup.membersUId) {
                        val userGroupAddTask = firestore.collection("users")
                            .document(userUId)
                            .collection("extraInfo")
                            .document("groupsInfo")
                            .update("groupsUId", FieldValue.arrayUnion(newDocumentRef.id))
                        tasks.add(userGroupAddTask)
                    }
                    tasks.add(teamFirstSetTask)
                    tasks.add(teamSecondSetTask)

                    Tasks.whenAll(tasks).addOnSuccessListener {
                        if (continuation.isActive) continuation.resume(newDocumentRef.id)
                    }
                }
        }
    }

    override suspend fun getGroups(): List<Group> {
        return suspendCancellableCoroutine { continuation ->
            val resultGroups = mutableListOf<Group>()
            var curUserUId = ""
            runBlocking {
                curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "") ?: ""
            }

            firestore.collection("users")
                .document(curUserUId)
                .collection("extraInfo")
                .document("groupsInfo")
                .get()
                .addOnSuccessListener {
                    it.data?.let { groupsInfo ->
                        val groups = groupsInfo["groupsUId"] as List<String>
                        for (group in groups) {
                            val groupRef = firestore.collection("groups").document(group)
                            val originalTeamsRef = groupRef.collection("originTeamsInfo")

                            // TODO CallBack Hell 해결
                            val getGroupTask = groupRef.get().addOnCompleteListener { task ->
                                if (task.isSuccessful) {
                                    val data = task.result?.data
                                    val days = (data?.get("days") as? String) ?: ""
                                    val times = (data?.get("times") as? String) ?: ""
                                    val headCount = (data?.get("headCount") as? Long)?.toInt() ?: 0
                                    val locations =
                                        (data?.get("locations") as? List<String>) ?: emptyList()
                                    val membersUId = (data?.get("membersUId") as? List<String>)
                                        ?: emptyList()
                                    val createdTimeStamp =
                                        (data?.get("createdTimeStamp") as? Long) ?: 0
                                    var curGroupInfo = Group(
                                        groupUId = group,
                                        originalTeamsInfo = listOf(),
                                        headCount = headCount,
                                        membersUId = membersUId,
                                        locations = locations,
                                        days = days,
                                        times = times,
                                        openChatUrl = "",
                                        createdTimeStamp = createdTimeStamp
                                    )

                                    originalTeamsRef.document("teamFirst").get()
                                        .addOnCompleteListener { task ->
                                            if (task.isSuccessful) {
                                                val data = task.result?.data
                                                val headCount =
                                                    (data?.get("headCount") as? Long)?.toInt() ?: 0
                                                val leaderUId =
                                                    (data?.get("leaderUId") as? String) ?: ""
                                                val membersUId =
                                                    (data?.get("membersUId") as? List<String>)
                                                        ?: emptyList()
                                                val teamImageUrl =
                                                    (data?.get("teamImageUrl") as? String) ?: ""
                                                val teamName =
                                                    (data?.get("teamName") as? String) ?: ""
                                                val totalAge =
                                                    (data?.get("totalAge") as? Long)?.toInt() ?: 0
                                                val totalAverage =
                                                    (data?.get("totalAverage") as? Long)?.toInt()
                                                        ?: 0
                                                val totalYearsPlaying =
                                                    (data?.get("totalYearsPlaying") as? Long)?.toInt()
                                                        ?: 0

                                                curGroupInfo = curGroupInfo.copy(
                                                    originalTeamsInfo = curGroupInfo.originalTeamsInfo + listOf(
                                                        Team(
                                                            teamUId = "",
                                                            teamName = teamName,
                                                            teamImageUrl = teamImageUrl,
                                                            leaderUId = leaderUId,
                                                            membersUId = membersUId,
                                                            headCount = headCount,
                                                            searchingHeadCount = 0,
                                                            searchingTimes = "",
                                                            searchingDays = "",
                                                            searchingLocations = listOf(),
                                                            openChatUrl = "",
                                                            totalAge = totalAge,
                                                            totalYearsPlaying = totalYearsPlaying,
                                                            totalAverage = totalAverage,
                                                            priorityScore = 0
                                                        )
                                                    )
                                                )
                                                originalTeamsRef.document("teamSecond").get()
                                                    .addOnCompleteListener { task ->
                                                        if (task.isSuccessful) {
                                                            val data = task.result?.data
                                                            val headCount =
                                                                (data?.get("headCount") as? Long)?.toInt()
                                                                    ?: 0
                                                            val leaderUId =
                                                                (data?.get("leaderUId") as? String)
                                                                    ?: ""
                                                            val membersUId =
                                                                (data?.get("membersUId") as? List<String>)
                                                                    ?: emptyList()
                                                            val teamImageUrl =
                                                                (data?.get("teamImageUrl") as? String)
                                                                    ?: ""
                                                            val teamName =
                                                                (data?.get("teamName") as? String)
                                                                    ?: ""
                                                            val totalAge =
                                                                (data?.get("totalAge") as? Long)?.toInt()
                                                                    ?: 0
                                                            val totalAverage =
                                                                (data?.get("totalAverage") as? Long)?.toInt()
                                                                    ?: 0
                                                            val totalYearsPlaying =
                                                                (data?.get("totalYearsPlaying") as? Long)?.toInt()
                                                                    ?: 0

                                                            curGroupInfo = curGroupInfo.copy(
                                                                originalTeamsInfo = curGroupInfo.originalTeamsInfo + listOf(
                                                                    Team(
                                                                        teamUId = "",
                                                                        teamName = teamName,
                                                                        teamImageUrl = teamImageUrl,
                                                                        leaderUId = leaderUId,
                                                                        membersUId = membersUId,
                                                                        headCount = headCount,
                                                                        searchingHeadCount = 0,
                                                                        searchingTimes = "",
                                                                        searchingDays = "",
                                                                        searchingLocations = listOf(),
                                                                        openChatUrl = "",
                                                                        totalAge = totalAge,
                                                                        totalYearsPlaying = totalYearsPlaying,
                                                                        totalAverage = totalAverage,
                                                                        priorityScore = 0
                                                                    )
                                                                )
                                                            )

                                                            resultGroups.add(curGroupInfo)
                                                            if (resultGroups.size == groups.size) {
                                                                if (continuation.isActive) continuation.resume(
                                                                    resultGroups.sortedBy { group ->
                                                                        group.createdTimeStamp
                                                                    }
                                                                )
                                                            }
                                                        } else {
                                                            if (continuation.isActive) continuation.resumeWithException(
                                                                task.exception
                                                                    ?: RuntimeException("Unknown error")
                                                            )
                                                        }
                                                    }

                                            } else {
                                                if (continuation.isActive) continuation.resumeWithException(
                                                    task.exception
                                                        ?: RuntimeException("Unknown error")
                                                )
                                            }
                                        }
                                } else {
                                    if (continuation.isActive) continuation.resumeWithException(
                                        task.exception ?: RuntimeException("Unknown error")
                                    )
                                }
                            }
                        }
                    }
                }
        }
    }

    override suspend fun getGroupDetail(groupUId: String): Group {
        return suspendCancellableCoroutine { continuation ->
            val groupRef = firestore.collection("groups").document(groupUId)
            val originalTeamsRef = groupRef.collection("originTeamsInfo")

            // TODO CallBack Hell 해결
            groupRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val data = task.result?.data
                    val days = (data?.get("days") as? String) ?: ""
                    val times = (data?.get("times") as? String) ?: ""
                    val headCount = (data?.get("headCount") as? Long)?.toInt() ?: 0
                    val locations =
                        (data?.get("locations") as? List<String>) ?: emptyList()
                    val membersUId = (data?.get("membersUId") as? List<String>)
                        ?: emptyList()
                    val createdTimeStamp =
                        (data?.get("createdTimeStamp") as? Long) ?: 0
                    var curGroupInfo = Group(
                        groupUId = groupUId,
                        originalTeamsInfo = listOf(),
                        headCount = headCount,
                        membersUId = membersUId,
                        locations = locations,
                        days = days,
                        times = times,
                        openChatUrl = "",
                        createdTimeStamp = createdTimeStamp
                    )

                    originalTeamsRef.document("teamFirst").get()
                        .addOnCompleteListener { task ->
                            if (task.isSuccessful) {
                                val data = task.result?.data
                                val headCount = (data?.get("headCount") as? Long)?.toInt() ?: 0
                                val leaderUId =
                                    (data?.get("leaderUId") as? String) ?: ""
                                val membersUId =
                                    (data?.get("membersUId") as? List<String>)
                                        ?: emptyList()
                                val teamImageUrl =
                                    (data?.get("teamImageUrl") as? String) ?: ""
                                val teamName = (data?.get("teamName") as? String) ?: ""
                                val totalAge = (data?.get("totalAge") as? Long)?.toInt() ?: 0
                                val totalAverage =
                                    (data?.get("totalAverage") as? Long)?.toInt() ?: 0
                                val totalYearsPlaying =
                                    (data?.get("totalYearsPlaying") as? Long)?.toInt() ?: 0

                                curGroupInfo = curGroupInfo.copy(
                                    originalTeamsInfo = curGroupInfo.originalTeamsInfo + listOf(
                                        Team(
                                            teamUId = "",
                                            teamName = teamName,
                                            teamImageUrl = teamImageUrl,
                                            leaderUId = leaderUId,
                                            membersUId = membersUId,
                                            headCount = headCount,
                                            searchingHeadCount = 0,
                                            searchingTimes = "",
                                            searchingDays = "",
                                            searchingLocations = listOf(),
                                            openChatUrl = "",
                                            totalAge = totalAge,
                                            totalYearsPlaying = totalYearsPlaying,
                                            totalAverage = totalAverage,
                                            priorityScore = 0
                                        )
                                    )
                                )

                                originalTeamsRef.document("teamSecond").get()
                                    .addOnCompleteListener { task ->
                                        if (task.isSuccessful) {
                                            val data = task.result?.data
                                            val headCount =
                                                (data?.get("headCount") as? Long)?.toInt() ?: 0
                                            val leaderUId =
                                                (data?.get("leaderUId") as? String) ?: ""
                                            val membersUId =
                                                (data?.get("membersUId") as? List<String>)
                                                    ?: emptyList()
                                            val teamImageUrl =
                                                (data?.get("teamImageUrl") as? String) ?: ""
                                            val teamName =
                                                (data?.get("teamName") as? String) ?: ""
                                            val totalAge =
                                                (data?.get("totalAge") as? Long)?.toInt() ?: 0
                                            val totalAverage =
                                                (data?.get("totalAverage") as? Long)?.toInt() ?: 0
                                            val totalYearsPlaying =
                                                (data?.get("totalYearsPlaying") as? Long)?.toInt()
                                                    ?: 0

                                            curGroupInfo = curGroupInfo.copy(
                                                originalTeamsInfo = curGroupInfo.originalTeamsInfo + listOf(
                                                    Team(
                                                        teamUId = "",
                                                        teamName = teamName,
                                                        teamImageUrl = teamImageUrl,
                                                        leaderUId = leaderUId,
                                                        membersUId = membersUId,
                                                        headCount = headCount,
                                                        searchingHeadCount = 0,
                                                        searchingTimes = "",
                                                        searchingDays = "",
                                                        searchingLocations = listOf(),
                                                        openChatUrl = "",
                                                        totalAge = totalAge,
                                                        totalYearsPlaying = totalYearsPlaying,
                                                        totalAverage = totalAverage,
                                                        priorityScore = 0
                                                    )
                                                )
                                            )

                                            if (continuation.isActive) continuation.resume(
                                                curGroupInfo
                                            )
                                        } else {
                                            if (continuation.isActive) continuation.resumeWithException(
                                                task.exception
                                                    ?: RuntimeException("Unknown error")
                                            )
                                        }
                                    }
                            } else {
                                if (continuation.isActive) continuation.resumeWithException(
                                    task.exception ?: RuntimeException("Unknown error")
                                )
                            }
                        }
                } else {
                    if (continuation.isActive) continuation.resumeWithException(
                        task.exception ?: RuntimeException("Unknown error")
                    )
                }
            }
        }
    }
}
