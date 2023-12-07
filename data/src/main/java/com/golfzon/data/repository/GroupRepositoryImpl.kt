package com.golfzon.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import com.golfzon.domain.model.Group
import com.golfzon.domain.repository.GroupRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.suspendCancellableCoroutine
import javax.inject.Inject
import kotlin.coroutines.resume

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

                    for (userUId in newGroup.membersId) {
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
}