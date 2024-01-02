package com.golfzon.data.repository

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.golfzon.data.common.FireStoreHelper.getGroupDocument
import com.golfzon.data.common.FireStoreHelper.getGroupMessagesCollection
import com.golfzon.data.common.Lg
import com.golfzon.data.extension.readValue
import com.golfzon.data.extension.toDataClass
import com.golfzon.domain.model.Group
import com.golfzon.domain.model.GroupMessage
import com.golfzon.domain.repository.ChatRepository
import com.golfzon.domain.repository.OnGrpMessageResponse
import com.google.firebase.firestore.DocumentChange
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChatRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val dataStore: DataStore<Preferences>
) : ChatRepository {
    override suspend fun sendMessage(message: GroupMessage, listener: OnGrpMessageResponse) {
        CoroutineScope(Dispatchers.IO).launch {
            val curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "") ?: ""
            val group = getGroupDocument(firestore, groupUId = message.groupId).get()
                .await().data?.toDataClass<Group>()

            val groupSize = group?.membersUId?.size ?: 0
            val statusList = ArrayList<Int>()
            val deliveryTimeList = ArrayList<Long>()
            val seenTime = ArrayList<Long>()
            for (index in 0 until groupSize) {
                statusList.add(0)
                deliveryTimeList.add(0L)
                seenTime.add(0L)
            }

            val editedMessage = message.copy(
                from = curUserUId,
                to = group!!.membersUId.map { it } as ArrayList,
                status = statusList,
                deliveryTime = deliveryTimeList,
                seenTime = seenTime,
            )

            val groupMessages =
                getGroupMessagesCollection(groupUId = editedMessage.groupId, firestore = firestore)

            groupMessages.document(editedMessage.createdAt.toString())
                .set(editedMessage, SetOptions.merge())
                .addOnSuccessListener {
                    listener.onSuccess(editedMessage)
                }.addOnFailureListener {
                    editedMessage.status[0] = 4
                    listener.onFailed(editedMessage)
                }
        }
    }

    override suspend fun getPastMessages(groupUId: String) = flow {
        try {
            val documents =
                getGroupMessagesCollection(groupUId = groupUId, firestore = firestore).get().await()
            val messages = documents.documents.mapNotNull { it.data?.toDataClass<GroupMessage>() }
            emit(messages)
        } catch (e: Exception) {
            Lg.e(e)
            emit(emptyList<GroupMessage>())
        }
    }.flowOn(Dispatchers.IO)

    override suspend fun receiveMessages(
        groupUId: String,
        callback: (GroupMessage) -> Unit
    ): () -> Unit =
        withContext(Dispatchers.IO) {
            val registration =
                getGroupMessagesCollection(groupUId = groupUId, firestore = firestore)
                    .addSnapshotListener { snapshots, error ->
                        if (error != null || snapshots == null || snapshots.metadata.isFromCache) {
                            return@addSnapshotListener
                        }

                        for (shot in snapshots.documentChanges) {
                            if (shot.type == DocumentChange.Type.ADDED ||
                                shot.type == DocumentChange.Type.MODIFIED
                            ) {
                                val message = shot.document.data.toDataClass<GroupMessage>()
                                Lg.e(message.textMessage.text)
                                callback(message)
                            }
                        }
                    }

            return@withContext fun() { registration.remove() }
        }
}