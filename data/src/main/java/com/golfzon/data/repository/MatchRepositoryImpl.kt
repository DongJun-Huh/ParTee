package com.golfzon.data.repository

import android.util.Log
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.golfzon.data.extension.readValue
import com.golfzon.domain.model.Team
import com.golfzon.domain.repository.MatchRepository
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class MatchRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val dataStore: DataStore<Preferences>
) : MatchRepository {
    override suspend fun getCandidateTeams(searchingHeadCount: Int): List<Team> {
        return suspendCancellableCoroutine { continuation ->
            var curUserUId = ""
            var curTeamUId = ""
            var curTeamHeadCount = 0 // 상대방이 몇명 찾고있는지는 조건에 안넣기로..
            var curTeamSearchingLocations = setOf<String>()
            var curTeamSearchingTimes = ""
            var curTeamSearchingDays = ""
            val resultTeams = mutableListOf<Team>()

            runBlocking {
                curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "") ?: ""
                curTeamUId = dataStore.readValue(stringPreferencesKey("teamUId"), "") ?: ""
                curTeamHeadCount = dataStore.readValue(intPreferencesKey("teamHeadCount"), 0) ?: 0
                curTeamSearchingLocations =
                    dataStore.readValue(stringSetPreferencesKey("searchingLocations"), setOf())
                        ?: setOf()
                curTeamSearchingTimes =
                    dataStore.readValue(stringPreferencesKey("searchingTimes"), "") ?: ""
                curTeamSearchingDays =
                    dataStore.readValue(stringPreferencesKey("searchingDays"), "") ?: ""
            }

            firestore.collection("teams")
                .get()
                .addOnSuccessListener { searchedTeams ->
                    for (team in searchedTeams.documents) {
                        if (curTeamUId != team.id) {
                            team.data?.let { teamDetail ->
                                if ((teamDetail["headCount"] as Long).toInt() == searchingHeadCount
                                    // 겹치는 지역이 존재하거나, 두 팀 중 한 팀이라도 지역에 관계없다면 통과
                                    && (curTeamSearchingLocations.contains("전국")
                                            || (teamDetail["searchingLocations"]!! as List<String>).contains(
                                        "전국"
                                    )
                                            || curTeamSearchingLocations.intersect(teamDetail["searchingLocations"]!! as List<String>)
                                        .isNotEmpty())
                                    && (teamDetail["searchingTimes"] as String) == curTeamSearchingTimes
                                    && (teamDetail["searchingDays"] as String) == curTeamSearchingDays
                                ) {
                                    Log.e("REPOSITORYIMPL", "${teamDetail}")
                                    resultTeams.add(
                                        Team(
                                            teamUId = team.id,
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
                                        )
                                    )
                                }
                            }
                        }
                    }

                    continuation.resume(resultTeams)
                }
        }
    }

    override suspend fun requestReactionsToCandidateTeam(
        candidateTeamUId: String,
        isLike: Boolean
    ): Boolean {
        var curTeamUId = ""

        runBlocking {
            curTeamUId = dataStore.readValue(stringPreferencesKey("teamUId"), "") ?: ""
        }

        return suspendCancellableCoroutine { continuation ->
            val curTeamLikesRef = firestore.collection("likes").document(curTeamUId)
            val candidateLikesRef = firestore.collection("likes").document(candidateTeamUId)

            candidateLikesRef.get().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    val data = task.result?.data
                    val likes = (data?.get("likes") as? List<String>) ?: emptyList()

                    if (isLike) {
                        if (likes.contains(curTeamUId)) {
                            candidateLikesRef.update("likes", FieldValue.arrayRemove(curTeamUId))
                            if (continuation.isActive) continuation.resume(true)
                        } else {
                            curTeamLikesRef.update("likes", FieldValue.arrayUnion(candidateTeamUId))
                            if (continuation.isActive) continuation.resume(false)
                        }
                    } else {
                        curTeamLikesRef.update("dislikes", FieldValue.arrayUnion(candidateTeamUId))
                        if (continuation.isActive) continuation.resume(false)
                    }
                } else {
                    // Handle the error case
                    continuation.resumeWithException(
                        task.exception ?: RuntimeException("Unknown error")
                    )
                }
            }
        }
    }
}