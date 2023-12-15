package com.golfzon.data.repository

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.core.stringSetPreferencesKey
import com.golfzon.data.common.FireStoreHelper.getTeamCollection
import com.golfzon.data.common.FireStoreHelper.getTeamDocument
import com.golfzon.data.common.FireStoreHelper.getTeamLikeDocument
import com.golfzon.data.common.FireStoreHelper.getUserTeamInfoDocument
import com.golfzon.data.common.Lg
import com.golfzon.data.extension.readValue
import com.golfzon.data.extension.storeValue
import com.golfzon.data.extension.toDataClass
import com.golfzon.domain.model.Team
import com.golfzon.domain.model.TeamInfo
import com.golfzon.domain.model.toMap
import com.golfzon.domain.repository.TeamRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class TeamRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val dataStore: DataStore<Preferences>
) : TeamRepository {

    override suspend fun getUserTeamInfoBrief(): TeamInfo = withContext(Dispatchers.IO) {
        val curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "") ?: ""
        val emptyTeamInfo = TeamInfo(
            teamUId = null,
            isOrganized = false,
            isLeader = false
        )

        try {
            val userTeamInfoSnapshot = getUserTeamInfoDocument(firestore, curUserUId).get().await()
            val userTeamInfoBrief = userTeamInfoSnapshot.data?.toDataClass<TeamInfo>() as TeamInfo
            userTeamInfoBrief
        } catch (e: Exception) {
            Lg.e(e)
            emptyTeamInfo
        }
    }

    override suspend fun getUserTeamInfoDetail(): Team? = withContext(Dispatchers.IO) {
        try {
            getUserTeamInfoBrief().let { teamInfoBrief ->
                Lg.e(teamInfoBrief.toString())
                val curTeamUId = teamInfoBrief.teamUId ?: return@withContext null
                val teamDetail =
                    getTeamDocument(firestore, curTeamUId).get().await().data?.toDataClass<Team>()
                        ?.copy(teamUId = curTeamUId, priorityScore = 0)
                        ?: return@withContext null
                Lg.e(teamDetail.toString())
                with(dataStore) {
                    storeValue(stringPreferencesKey("teamUId"), curTeamUId)
                    storeValue(
                        stringSetPreferencesKey("searchingLocations"),
                        teamDetail.searchingLocations.toSet()
                    )
                }
                teamDetail
            }
        } catch (e: Exception) {
            Lg.e(e)
            null
        }
    }

    override suspend fun getTeamInfoDetail(teamUId: String): Team = withContext(Dispatchers.IO) {
        val emptyTeam = Team(
            teamUId = "",
            teamName = "",
            teamImageUrl = "",
            leaderUId = "",
            membersUId = listOf(),
            headCount = 0,
            searchingHeadCount = 0,
            searchingTimes = "",
            searchingDays = "",
            searchingLocations = listOf(),
            openChatUrl = "",
            totalAge = 0,
            totalYearsPlaying = 0,
            totalAverage = 0,
            priorityScore = 0
        )

        try {
            val teamDetail = getTeamDocument(firestore, teamUId).get().await().data
                ?.toDataClass<Team>()
                ?.copy(teamUId = teamUId, priorityScore = 0) ?: return@withContext emptyTeam
            teamDetail
        } catch (e: Exception) {
            Lg.e(e)
            emptyTeam
        }
    }

    override suspend fun requestTeamOrganize(newTeamWithNoImage: Team, teamImg: File?): String =
        withContext(Dispatchers.IO) {
            val curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "") ?: ""
            val tasks = mutableListOf<Task<*>>()
            try {
                val curUserTeamInfoBrief =
                    getUserTeamInfoDocument(firestore, curUserUId).get().await().data
                        ?.toDataClass<TeamInfo>()
                        ?: return@withContext ""

                val requestTeamUId =
                    if (curUserTeamInfoBrief.isOrganized) curUserTeamInfoBrief.teamUId
                    else getTeamCollection(firestore)
                        .add(newTeamWithNoImage.copy(leaderUId = curUserUId))
                        .await().id

                val requestTeamInfoBrief = TeamInfo(
                    teamUId = requestTeamUId,
                    isOrganized = true,
                    isLeader = false
                )

                val newTeam = if (teamImg != null) {
                    val teamImageExtension = teamImg.path?.split(".")?.last()?.ifEmpty { "jpg" }
                    val teamImagesRef = firebaseStorage.reference
                        .child("teams/${requestTeamUId}.${teamImageExtension}")

                    val uploadTeamImageTask = teamImagesRef.putFile(Uri.fromFile(teamImg))
                    tasks.add(uploadTeamImageTask)
                    Lg.e("${requestTeamUId}.${teamImageExtension}")

                    newTeamWithNoImage.copy(teamImageUrl = "${requestTeamUId}.${teamImageExtension}")
                } else {
                    newTeamWithNoImage
                }

                // TODO Exception 정의하고 새로 팀 만드는 부분으로 넘겨주기
                val requestTeamInfoSetTask = getTeamDocument(
                    firestore,
                    requestTeamUId
                        ?: throw Exception("Team is organized. But, Team UID is not initialized")
                ).set(newTeam)

                val requestTeamLikeSetTask = getTeamLikeDocument(firestore, requestTeamUId)
                    .set(
                        hashMapOf(
                            "likes" to listOf<String>(),
                            "dislikes" to listOf<String>()
                        )
                    )

                val userTeamMemberInfoSetTasks = newTeam.membersUId.map { memberUId ->
                    getUserTeamInfoDocument(firestore, memberUId)
                        .set(
                            if (memberUId != curUserUId) requestTeamInfoBrief.toMap()
                            else requestTeamInfoBrief.copy(isLeader = true).toMap()
                        )
                }

                tasks.add(requestTeamInfoSetTask)
                tasks.add(requestTeamLikeSetTask)
                tasks.addAll(userTeamMemberInfoSetTasks)

                Tasks.whenAll(tasks).await()
                requestTeamUId
            } catch (e: Exception) {
                Lg.e(e)
                ""
            }
        }

    override suspend fun deleteTeam(teamUId: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val teamInfo = getTeamDocument(firestore, teamUId).get().await().data
                ?.toDataClass<Team>()
                ?: return@withContext false

            val teamLikeInfoDeleteTask = getTeamLikeDocument(firestore, teamUId).delete()
            val userTeamInfoDeleteTasks = teamInfo.membersUId.map { memberUId ->
                getUserTeamInfoDocument(firestore, memberUId)
                    .update(TeamInfo(null, false, false).toMap())
            }
            val teamDeleteTask = getTeamDocument(firestore, teamUId).delete()
            val tasks = mutableListOf<Task<*>>()

            tasks.add(teamLikeInfoDeleteTask)
            tasks.add(teamDeleteTask)
            tasks.addAll(userTeamInfoDeleteTasks)

            Tasks.whenAll(tasks).await()
            true
        } catch (e: Exception) {
            Lg.e(e)
            false
        }
    }
}