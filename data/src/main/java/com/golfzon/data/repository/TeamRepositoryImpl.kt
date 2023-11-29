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
}