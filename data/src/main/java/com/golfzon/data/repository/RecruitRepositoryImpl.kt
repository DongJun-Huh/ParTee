package com.golfzon.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.golfzon.data.extension.readValue
import com.golfzon.domain.model.Recruit
import com.golfzon.domain.repository.RecruitRepository
import com.google.firebase.Timestamp
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
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
import kotlin.reflect.KVisibility
import kotlin.reflect.full.memberProperties

class RecruitRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val dataStore: DataStore<Preferences>
) : RecruitRepository {
    override suspend fun createRecruitPost(recruitInfo: Recruit): Boolean {
        return suspendCancellableCoroutine { continuation ->
            CoroutineScope(Dispatchers.IO).launch {
                val curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "") ?: ""
                val convertedRecruitInfo = Recruit::class.memberProperties
                    .filter { it.visibility == KVisibility.PUBLIC }
                    .associate {
                        when (it.name) {
                            "recruitDateTime", "recruitEndDateTime" -> {
                                it.name to
                                        Timestamp(
                                            Date.from(
                                                (it.call(recruitInfo) as LocalDateTime).atZone(
                                                    ZoneId.systemDefault()
                                                ).toInstant()
                                            )
                                        )
                            }

                            "leaderUId" -> {
                                it.name to curUserUId
                            }

                            "membersUId" -> it.name to listOf(curUserUId)
                            else -> {
                                it.name to it.call(recruitInfo)
                            }
                        }
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
}