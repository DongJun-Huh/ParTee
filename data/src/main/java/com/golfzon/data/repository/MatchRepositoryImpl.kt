package com.golfzon.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.golfzon.data.common.FireStoreHelper.getTeamCollection
import com.golfzon.data.common.FireStoreHelper.getTeamDisLikeDocument
import com.golfzon.data.common.FireStoreHelper.getTeamLikeDocument
import com.golfzon.data.common.Lg
import com.golfzon.data.extension.readValue
import com.golfzon.data.extension.toDataClass
import com.golfzon.domain.model.Team
import com.golfzon.domain.repository.MatchRepository
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class MatchRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val dataStore: DataStore<Preferences>
) : MatchRepository {
    override suspend fun getCandidateTeams(
        searchingHeadCount: Int,
        searchingDays: String,
        searchingTimes: String,
        reactedTeams: List<String>
    ): List<Team> = withContext(Dispatchers.IO) {
        val curTeamUId = dataStore.readValue(stringPreferencesKey("teamUId"), "") ?: ""
        val curTeamSearchingLocations =
            dataStore.readValue(stringSetPreferencesKey("searchingLocations"), setOf()) ?: setOf()

        try {
            val filteredCandidateTeams = getTeamCollection(firestore)
                .whereEqualTo("searchingDays", searchingDays)
                .whereEqualTo("searchingTimes", searchingTimes)
                .whereEqualTo("headCount", searchingHeadCount.toLong())
                .get()
                .await()

            filteredCandidateTeams.documents.filter { curCandidateTeam ->
                curCandidateTeam.id != curTeamUId
                        && !reactedTeams.contains(curCandidateTeam.id)
                        && isValidLocation(curCandidateTeam, curTeamSearchingLocations)
            }.mapNotNull { (it.data?.toDataClass<Team>() as Team).copy(teamUId = it.id) }
        } catch (e: Exception) {
            Lg.e(e)
            emptyList()
        }
    }

    private fun isValidLocation(
        candidateTeamDocumentSnapshot: DocumentSnapshot,
        curTeamSearchingLocations: Set<String>
    ): Boolean =
        (candidateTeamDocumentSnapshot.data?.get("searchingLocations")
                as? List<String>? ?: emptyList()
                ).let { candidateTeamLocations ->
                if (candidateTeamLocations.isEmpty()) return false

                curTeamSearchingLocations.contains("전국") || candidateTeamLocations.contains("전국") ||
                        curTeamSearchingLocations.intersect(candidateTeamLocations.toSet())
                            .isNotEmpty()
            }

    override suspend fun requestReactionsToCandidateTeam(
        candidateTeamUId: String,
        isLike: Boolean
    ): Boolean = withContext(Dispatchers.IO) {
        val curTeamUId = dataStore.readValue(stringPreferencesKey("teamUId"), "") ?: ""
        val curTeamLikesRef = getTeamLikeDocument(firestore, curTeamUId)
        val candidateLikesRef = getTeamDisLikeDocument(firestore, candidateTeamUId)

        try {
            val likes = (candidateLikesRef.get().await().data?.get("likes")
                    as? List<String> ?: emptyList()) ?: emptyList()

            when {
                isLike && likes.contains(curTeamUId) -> {
                    candidateLikesRef.update("likes", FieldValue.arrayRemove(curTeamUId)).await()
                    true
                }

                isLike -> {
                    curTeamLikesRef.update("likes", FieldValue.arrayUnion(candidateTeamUId)).await()
                    false
                }

                else -> {
                    curTeamLikesRef.update("dislikes", FieldValue.arrayUnion(candidateTeamUId))
                        .await()
                    false
                }
            }
        } catch (e: Exception) {
            Lg.e(e)
            false
        }
    }

    override suspend fun getReactedTeams(): List<String> = withContext(Dispatchers.IO) {
        val curTeamUId = dataStore.readValue(stringPreferencesKey("teamUId"), "") ?: ""

        try {
            val reactionInfo = getTeamLikeDocument(firestore, curTeamUId).get().await().data
            val likes =
                (reactionInfo?.get("likes") as? List<String> ?: emptyList()) ?: emptyList()
            val dislikes =
                (reactionInfo?.get("dislikes") as? List<String> ?: emptyList()) ?: emptyList()
            likes + dislikes
        } catch (e: Exception) {
            Lg.e(e)
            emptyList()
        }
    }
}