package com.golfzon.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.golfzon.data.TimestampDeserializer
import com.golfzon.data.extension.readValue
import com.golfzon.domain.model.Recruit
import com.golfzon.domain.repository.RecruitRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.suspendCancellableCoroutine
import java.time.LocalDateTime
import java.time.ZoneId
import java.util.Date
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException

class RecruitRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val dataStore: DataStore<Preferences>
) : RecruitRepository {
    override suspend fun createRecruitPost(recruitInfo: Recruit): Boolean {
        return suspendCancellableCoroutine { continuation ->
            CoroutineScope(Dispatchers.IO).launch {
                val curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "") ?: ""
                val gson = Gson()
                val json = gson.toJson(recruitInfo)
                val convertedRecruitInfo =
                    gson.fromJson<Map<String, Any>>(json, Map::class.java).toMutableMap().apply {
                        set(
                            "recruitDateTime", Timestamp(
                                Date.from(
                                    (recruitInfo.recruitDateTime).atZone(
                                        ZoneId.systemDefault()
                                    ).toInstant()
                                )
                            )
                        )
                        set(
                            "recruitEndDateTime", Timestamp(
                                Date.from(
                                    (recruitInfo.recruitEndDateTime).atZone(
                                        ZoneId.systemDefault()
                                    ).toInstant()
                                )
                            )
                        )
                        set("leaderUId", curUserUId)
                        set("membersUId", listOf(curUserUId))
                    }

                firestore.collection("recruits")
                    .add(convertedRecruitInfo)
                    .addOnSuccessListener { newRecruitInfo ->
                        firestore.collection("users")
                            .document(curUserUId)
                            .collection("extraInfo")
                            .document("recruitsInfo")
                            .update("recruitsUId", FieldValue.arrayUnion(newRecruitInfo.id))
                            .addOnSuccessListener {
                                if (continuation.isActive) continuation.resume(true)
                            }
                            .addOnFailureListener {
                                if (continuation.isActive) continuation.resumeWithException(it)
                            }
                    }
                    .addOnFailureListener {
                        if (continuation.isActive) continuation.resumeWithException(it)
                    }
            }
        }
    }

    override suspend fun getRecruits(): List<Recruit> {
        return suspendCancellableCoroutine { continuation ->
            val resultRecruits = mutableListOf<Recruit>()
            firestore.collection("recruits")
                .get()
                .addOnSuccessListener { recruits ->
                    for (recruit in recruits.documents) {
                        recruit.data?.let { recruitDetail ->
                            val gsonBuilder = GsonBuilder()
                            gsonBuilder.registerTypeAdapter(
                                LocalDateTime::class.java,
                                TimestampDeserializer()
                            )
                            val gson = gsonBuilder.create()
                            val json = gson.toJson(recruitDetail)
                            val recruitType = object : TypeToken<Recruit>() {}.type
                            var recruitInfo: Recruit = gson.fromJson(json, recruitType)
                            recruitInfo = recruitInfo.copy(recruitUId = recruit.id)
                            resultRecruits.add(recruitInfo.copy(recruitUId = recruit.id))
                        }
                    }
                    if (continuation.isActive) continuation.resume(resultRecruits)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun getRecruitDetail(recruitUId: String): Recruit {
        return suspendCancellableCoroutine { continuation ->
            firestore.collection("recruits")
                .document(recruitUId)
                .get()
                .addOnSuccessListener { recruitInfo ->
                    recruitInfo.data?.let { recruitDetail ->
                        val gsonBuilder = GsonBuilder()
                        gsonBuilder.registerTypeAdapter(
                            LocalDateTime::class.java,
                            TimestampDeserializer()
                        )
                        val gson = gsonBuilder.create()
                        val json = gson.toJson(recruitDetail)
                        val recruitType = object : TypeToken<Recruit>() {}.type
                        var recruitDetailToDisplay: Recruit = gson.fromJson(json, recruitType)
                        recruitDetailToDisplay = recruitDetailToDisplay.copy(recruitUId = recruitUId)
                        if (continuation.isActive) continuation.resume(recruitDetailToDisplay)
                    }
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resumeWithException(it)
                }
        }
    }

    override suspend fun participateRecruit(recruitUId: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            CoroutineScope(Dispatchers.IO).launch {
                val curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "") ?: ""
                val recruitRef = firestore.collection("recruits").document(recruitUId)

                recruitRef.get()
                    .addOnSuccessListener {
                        it.data?.let { recruitInfo ->
                            // 인원수가 모두 다 찼거나, 모집일자가 지난 경우
                            if ((recruitInfo["headCount"] as Long).toInt() >= (recruitInfo["searchingHeadCount"] as Long).toInt() ||
                                (recruitInfo["recruitEndDateTime"] as Timestamp).toDate()
                                    .toInstant().atZone(
                                    ZoneId.systemDefault()
                                ).toLocalDateTime().isBefore(LocalDateTime.now())
                            ) {
                                if (continuation.isActive) continuation.resume(false)
                            } else {
                                val tasks = mutableListOf<Task<*>>()
                                val memberAddTask = recruitRef.update(
                                    "membersUId",
                                    FieldValue.arrayUnion(curUserUId)
                                )
                                val headCountAddTask =
                                    recruitRef.update("headCount", FieldValue.increment(1L))
                                val userInfoUpdateTask =
                                    firestore.collection("users")
                                        .document(curUserUId)
                                        .collection("extraInfo")
                                        .document("recruitsInfo")
                                        .update("recruitsUId", FieldValue.arrayUnion(recruitUId))
                                tasks.add(memberAddTask)
                                tasks.add(headCountAddTask)
                                tasks.add(userInfoUpdateTask)

                                Tasks.whenAll(tasks)
                                    .addOnSuccessListener {
                                        if (continuation.isActive) continuation.resume(true)
                                    }
                                    .addOnFailureListener { exception ->
                                        if (continuation.isActive) continuation.resumeWithException(
                                            exception
                                        )
                                    }
                            }
                        }
                    }
            }
        }
    }
}