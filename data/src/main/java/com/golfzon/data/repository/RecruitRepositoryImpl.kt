package com.golfzon.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.golfzon.data.common.FireStoreHelper.getRecruitCollection
import com.golfzon.data.common.FireStoreHelper.getRecruitDocument
import com.golfzon.data.common.FireStoreHelper.getUserRecruitInfoDocument
import com.golfzon.data.common.Lg
import com.golfzon.data.common.TimestampDeserializer
import com.golfzon.data.extension.readValue
import com.golfzon.data.extension.toDataClass
import com.golfzon.data.extension.toTimestamp
import com.golfzon.domain.model.Recruit
import com.golfzon.domain.repository.RecruitRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import javax.inject.Inject

class RecruitRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val dataStore: DataStore<Preferences>
) : RecruitRepository {
    override suspend fun createRecruitPost(recruitInfo: Recruit): Boolean =
        withContext(Dispatchers.IO) {
            val curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "") ?: ""

            try {
                val gson = Gson()
                val json = gson.toJson(recruitInfo)
                val convertedRecruitInfo =
                    gson.fromJson<Map<String, Any>>(json, Map::class.java).toMutableMap().apply {
                        set("recruitDateTime", recruitInfo.recruitDateTime.toTimestamp)
                        set("recruitEndDateTime", recruitInfo.recruitEndDateTime.toTimestamp)
                        set("leaderUId", curUserUId)
                        set("membersUId", listOf(curUserUId))
                    }

                val newRecruitInfo =
                    getRecruitCollection(firestore).add(convertedRecruitInfo).await()
                val userUpdateTask = getUserRecruitInfoDocument(firestore, curUserUId).update(
                    "recruitsUId",
                    FieldValue.arrayUnion(newRecruitInfo.id)
                )

                Tasks.whenAll(userUpdateTask).await()
                true
            } catch (e: Exception) {
                Lg.e(e)
                false
            }
        }

    override suspend fun getRecruits(): List<Recruit> = withContext(Dispatchers.IO) {
        val resultRecruits = mutableListOf<Recruit>()
        try {
            val recruits = getRecruitCollection(firestore).get().await()
            recruits.documents.map { recruitDocument ->
                recruitDocument.data?.let {
                    resultRecruits.add(
                        it.toDataClass<Recruit, LocalDateTime>(jsonDeserializer = TimestampDeserializer())
                    )
                }
            }

            resultRecruits
        } catch (e: Exception) {
            Lg.e(e)
            emptyList()
        }
    }

    override suspend fun getRecruitDetail(recruitUId: String): Recruit =
        withContext(Dispatchers.IO) {
            val emptyRecruit = Recruit(
                recruitUId = "",
                leaderUId = "",
                membersUId = listOf(),
                headCount = 0,
                searchingHeadCount = 0,
                recruitDateTime = LocalDateTime.now(),
                recruitPlace = "",
                recruitEndDateTime = LocalDateTime.now(),
                openChatUrl = "",
                fee = 0,
                isConsecutiveStay = false,
                isCouple = false,
                recruitIntroduceMessage = ""
            )

            try {
                val recruitDetail = getRecruitDocument(firestore, recruitUId).get().await().data
                recruitDetail?.let {
                    it.toDataClass<Recruit, LocalDateTime>(jsonDeserializer = TimestampDeserializer())
                        .copy(recruitUId = recruitUId)
                } ?: emptyRecruit
            } catch (e: Exception) {
                Lg.e(e)
                emptyRecruit
            }
        }

    override suspend fun participateRecruit(recruitUId: String): Boolean =
        withContext(Dispatchers.IO) {
            val curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "") ?: ""

            try {
                val tasks = mutableListOf<Task<*>>()
                val recruitDocument = getRecruitDocument(firestore, recruitUId).get().await().data
                recruitDocument?.let {
                    val recruitInfo =
                        it.toDataClass<Recruit, LocalDateTime>(jsonDeserializer = TimestampDeserializer())
                    recruitInfo.let { detail ->
                        // 인원수가 모두 다 찼거나, 모집일자가 지난 경우, 이미 참여한 경우
                        if (detail.headCount >= detail.searchingHeadCount
                            || detail.recruitEndDateTime.isBefore(LocalDateTime.now())
                            || detail.membersUId.contains(curUserUId)
                        ) return@withContext false

                        val memberAddTask = getRecruitDocument(firestore, recruitUId).update(
                            "membersUId",
                            FieldValue.arrayUnion(curUserUId)
                        )
                        val headCountAddTask = getRecruitDocument(firestore, recruitUId).update(
                            "headCount",
                            FieldValue.increment(1L)
                        )
                        val userInfoUpdateTask =
                            getUserRecruitInfoDocument(firestore, curUserUId).update(
                                "recruitsUId",
                                FieldValue.arrayUnion(recruitUId)
                            )
                        tasks.addAll(listOf(memberAddTask, headCountAddTask, userInfoUpdateTask))
                    }
                }

                Tasks.whenAll(tasks).await()
                true
            } catch (e: Exception) {
                Lg.e(e)
                false
            }
        }
}