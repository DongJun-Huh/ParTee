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
import kotlin.coroutines.resume
import javax.inject.Inject

class TeamRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val dataStore: DataStore<Preferences>
) : TeamRepository {
    override suspend fun getUserTeamInfoBrief(): TeamInfo {
        return suspendCancellableCoroutine { continuation ->
            var curUserUId = "saGsdRTdEfeJklbHjIBHGpGRZdj1"
            runBlocking {
                curUserUId = dataStore.readValue(
                    stringPreferencesKey("userUid"),
                    "saGsdRTdEfeJklbHjIBHGpGRZdj1"
                ) ?: ""
            }

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

                        CoroutineScope(Dispatchers.IO).launch {
                            with(dataStore) {
                                storeValue(stringPreferencesKey("teamUId"), curTeamBrief.teamUId)
                                storeValue(booleanPreferencesKey("isLeader"), curTeamBrief.isLeader)
                                storeValue(
                                    booleanPreferencesKey("isOrganized"),
                                    curTeamBrief.isOrganized
                                )
                            }
                        }

                        continuation.resume(curTeamBrief)
                    }
                }
        }
    }

    override suspend fun getUserTeamInfoDetail(): Team? {
        return suspendCancellableCoroutine { continuation ->
            var curTeamUId = ""
            runBlocking {
                val curTeamInfoBrief = getUserTeamInfoBrief()
                if (curTeamInfoBrief.teamUId == null) {
                    if (continuation.isActive) continuation.resume(null)
                } else curTeamUId = curTeamInfoBrief.teamUId!!
            }

            firestore.collection("teams")
                .document(curTeamUId)
                .get()
                .addOnSuccessListener { teamDetail ->
                    if (continuation.isActive) continuation.resume(
                        Team(
                            leaderUId = teamDetail["leaderUId"] as String,
                            membersUId = teamDetail["membersUId"] as List<String>,
                            headCount = (teamDetail["headCount"] as Long).toInt(),
                            searchingHeadCount = (teamDetail["searchingHeadCount"] as Long).toInt(),
                            searchingTimes = teamDetail["searchingTimes"] as String,
                            openChatUrl = teamDetail["openChatUrl"] as String
                        )
                    )
                }

        }
    }

    override suspend fun requestTeamOrganize(newTeam: Team): String =
        suspendCancellableCoroutine { continuation ->
            var curUserUId = ""
            runBlocking {
                curUserUId = dataStore.readValue(stringPreferencesKey("userUid"), "") ?: ""
            }

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

                    val leaderUserEditTask = firestore.collection("users")
                        .document(newTeam.leaderUId)
                        .collection("extraInfo")
                        .document("teamInfo")
                        .set(newTeamInfo.apply { "isLeader" to true })
                    tasks.add(leaderUserEditTask)
                    Tasks.whenAll(tasks)
                        .addOnSuccessListener {
                            if (continuation.isActive) continuation.resume(newTeamDocument.id)
                        }
                }
        }
}