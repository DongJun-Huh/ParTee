package com.golfzon.data.repository

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.golfzon.data.common.FireStoreHelper.getUserCollection
import com.golfzon.data.common.FireStoreHelper.getUserDocument
import com.golfzon.data.common.FireStoreHelper.getUserGroupInfoDocument
import com.golfzon.data.common.FireStoreHelper.getUserRecruitInfoDocument
import com.golfzon.data.common.FireStoreHelper.getUserTeamInfoDocument
import com.golfzon.data.common.Lg
import com.golfzon.data.extension.readValue
import com.golfzon.data.extension.storeValue
import com.golfzon.data.extension.toDataClass
import com.golfzon.domain.model.GroupInfo
import com.golfzon.domain.model.RecruitsInfo
import com.golfzon.domain.model.TeamInfo
import com.golfzon.domain.model.User
import com.golfzon.domain.model.UserInfo
import com.golfzon.domain.model.toMap
import com.golfzon.domain.repository.MemberRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import javax.inject.Inject

class MemberRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val dataStore: DataStore<Preferences>
) : MemberRepository {
    override suspend fun requestRegisterUser(UId: String, email: String): Boolean =
        withContext(Dispatchers.IO) {
            dataStore.storeValue(stringPreferencesKey("userUId"), UId)

            try {
                val newUser = hashMapOf(
                    "email" to email,
                    "nickname" to null,
                    "age" to null,
                    "yearsPlaying" to null,
                    "average" to null,
                    "introduceMessage" to null,
                    "profileImg" to null
                )
                getUserDocument(firestore, UId).set(newUser).await()

                val newUserTeamInfo = hashMapOf(
                    "teamUId" to null,
                    "isLeader" to false,
                    "isOrganized" to false
                )
                val newUserGroupsInfo = hashMapOf("groupsUId" to listOf<String>())
                val newUserRecruitsInfo = hashMapOf("recruitsUId" to listOf<String>())

                Tasks.whenAll(
                    getUserTeamInfoDocument(firestore, UId).set(newUserTeamInfo),
                    getUserGroupInfoDocument(firestore, UId).set(newUserGroupsInfo),
                    getUserRecruitInfoDocument(firestore, UId).set(newUserRecruitsInfo)
                ).await()
                true
            } catch (e: Exception) {
                Lg.e(e)
                false
            }
        }

    override suspend fun requestLogin(UId: String, email: String): Pair<Boolean, User?> =
        // Return Value: (최초가입여부, 유저정보)
        withContext(Dispatchers.IO) {
            with(dataStore) {
                storeValue(stringPreferencesKey("userUId"), UId)
                storeValue(stringPreferencesKey("userEmail"), email)
            }

            try {
                val userDocument = getUserDocument(firestore, UId).get().await()
                val userDetail = userDocument.data
                    ?: return@withContext Pair(false, null) // 1. 미 가입 상태
                if (userDetail.values.any { it == null }) return@withContext Pair(
                    true,
                    null
                ) // 2. 최초 가입 후, 유저정보 미입력 상태
                Pair(true, userDetail.toDataClass<User>() as User) // 3. 최초 가입 후, 유저정보 입력 상태
            } catch (e: Exception) {
                Lg.e(e)
                Pair(false, null)
            }
        }

    override suspend fun requestSetUserInfo(user: User, userImg: File): Boolean =
        withContext(Dispatchers.IO) {
            val curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "") ?: ""
            val curUserEmail = dataStore.readValue(stringPreferencesKey("userEmail"), "") ?: ""

            val userImageExtension = userImg.path.split(".").last().ifEmpty { "jpg" }
            val userImagesRef = firebaseStorage.reference
                .child("users/${curUserUId}.${userImageExtension}")

            try {
                val setUserTask: Task<Void> = getUserDocument(firestore, curUserUId)
                    .set(user.copy(email = curUserEmail).toMap())
                val uploadTask = userImagesRef.putFile(Uri.fromFile(userImg))

                Tasks.whenAll(uploadTask, setUserTask).await()
                true
            } catch (e: Exception) {
                Lg.e(e)
                false
            }
        }

    override suspend fun getUsersInfo(nickname: String): List<User> = withContext(Dispatchers.IO) {
        val resultUsers = mutableListOf<User>()
        val curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "") ?: ""

        try {
            val users = getUserCollection(firestore)
                .whereEqualTo("nickname", nickname)
                .get()
                .await()

            users.documents
                .filter { user -> user.id != curUserUId}
                .map { userInfo ->
                    val userTeamInfo = getUserTeamInfoDocument(firestore, userInfo.id).get().await()
                    getUserDocument(firestore, userInfo.id).get().await()
                        .data?.let { curUserInfo ->
                            resultUsers.add(
                                (curUserInfo.toDataClass<User>() as User).copy(
                                    userUId = userInfo.id,
                                    userInfo = UserInfo(
                                        teamInfo = userTeamInfo.data?.toDataClass<TeamInfo>() as TeamInfo,
                                        groupsInfo = listOf<GroupInfo>(),
                                        recruitsInfo = listOf<RecruitsInfo>()
                                    )
                                )
                            )
                        }
                }

            resultUsers
        } catch (e: Exception) {
            Lg.e(e)
            emptyList<User>()
        }
    }

    override suspend fun getUserInfo(UId: String): Pair<User, Boolean> =
        // Return value: (유저정보, 현재유저여부)
        withContext(Dispatchers.IO) {
            val curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "") ?: ""
            val emptyUser = User(
                userUId = "",
                email = "",
                nickname = "",
                age = 0,
                yearsPlaying = 0,
                average = 0,
                introduceMessage = "",
                profileImg = "",
                userInfo = UserInfo(
                    teamInfo = TeamInfo(
                        teamUId = "",
                        isLeader = false,
                        isOrganized = false
                    ),
                    groupsInfo = listOf(),
                    recruitsInfo = listOf()
                )
            )

            try {
                val userDetail = getUserDocument(firestore, UId).get().await().data
                    ?: return@withContext Pair(emptyUser, false)
                val teamDetail = getUserTeamInfoDocument(firestore, UId).get().await().data
                    ?: return@withContext Pair(emptyUser, false)

                val curUser = (userDetail.toDataClass<User>() as User).copy(
                    userUId = UId,
                    userInfo = UserInfo(
                        teamInfo = teamDetail.toDataClass<TeamInfo>() as TeamInfo,
                        groupsInfo = listOf(),
                        recruitsInfo = listOf()
                    )
                )

                Pair(curUser, curUserUId == UId)
            } catch (e: Exception) {
                Lg.e(e)
                Pair(emptyUser, false)
            }
        }

    override suspend fun getCurUserInfo(): Triple<String, String, String> =
        withContext(Dispatchers.IO) {
            with(dataStore) {
                Triple(
                    readValue(stringPreferencesKey("userUId"), "") ?: "",
                    readValue(stringPreferencesKey("userEmail"), "") ?: "",
                    readValue(stringPreferencesKey("userNickname"), "") ?: ""
                )
            }
        }
}