package com.golfzon.data.repository

import android.net.Uri
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.stringPreferencesKey
import com.golfzon.data.extension.readValue
import com.golfzon.data.extension.storeValue
import com.golfzon.domain.model.GroupInfo
import com.golfzon.domain.model.TeamInfo
import com.golfzon.domain.model.ThemeTeamInfo
import com.golfzon.domain.model.User
import com.golfzon.domain.model.UserInfo
import com.golfzon.domain.repository.MemberRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.suspendCancellableCoroutine
import java.io.File
import javax.inject.Inject
import kotlin.coroutines.resume

class MemberRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val firebaseStorage: FirebaseStorage,
    private val dataStore: DataStore<Preferences>
) : MemberRepository {
    // TODO 임시로 curUserUId 기본값으로 들어가있는 saGsdRTdEfeJklbHjIBHGpGRZdj1 삭제
    override suspend fun requestRegisterUser(UId: String, email: String): Boolean {
        return suspendCancellableCoroutine { continuation ->
            val newUser = hashMapOf(
                "email" to email,
                "nickname" to null,
                "age" to null,
                "yearsPlaying" to null,
                "average" to null,
                "introduceMessage" to null,
                "profileImg" to null
            )
            val userDocumentReference = firestore.collection("users")
                .document(UId)
            val setUserTask: Task<Void> = userDocumentReference.set(newUser)
            setUserTask
                .addOnSuccessListener {
                    val newUserTeamInfo = hashMapOf(
                        "teamUId" to null,
                        "isLeader" to false,
                        "isOrganized" to false
                    )
                    val newUserGroupsInfo = hashMapOf(
                        "groupsUId" to listOf<String>()
                    )
                    val newUserThemeTeamsInfo = hashMapOf(
                        "themeTeamUId" to listOf<String>()
                    )
                    val extraInfoCollection = userDocumentReference.collection("extraInfo")
                    val setTeamInfoTask =
                        extraInfoCollection.document("teamInfo").set(newUserTeamInfo)
                    val setGroupsInfoTask =
                        extraInfoCollection.document("groupsInfo").set(newUserGroupsInfo)
                    val setThemeTeamsInfoTask =
                        extraInfoCollection.document("themeTeamsInfo").set(newUserThemeTeamsInfo)

                    CoroutineScope(Dispatchers.IO).launch {
                        dataStore.storeValue(stringPreferencesKey("userUId"), UId)
                    }

                    Tasks.whenAll(setTeamInfoTask, setGroupsInfoTask, setThemeTeamsInfoTask)
                        .addOnSuccessListener {
                            if (continuation.isActive) continuation.resume(true)
                        }
                        .addOnFailureListener {
                            if (continuation.isActive) continuation.resume(false)
                        }
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(false)
                }
        }
    }

    // Return Value: (최초가입여부, 유저정보)
    override suspend fun requestLogin(UId: String, email: String): Pair<Boolean, User?> {
        return suspendCancellableCoroutine { continuation ->
            var curUser = User(
                userUId = UId,
                email = email,
                nickname = null,
                age = null,
                yearsPlaying = null,
                average = null,
                introduceMessage = null,
                profileImg = null,
                UserInfo(
                    teamInfo = TeamInfo(
                        teamUId = null,
                        isOrganized = false,
                        isLeader = false
                    ),
                    groupsInfo = listOf(),
                    themeTeamsInfo = listOf()
                )
            )
            val tasks = mutableListOf<Task<*>>()

            // 최초 회원가입 여부 확인 및 정보 입력여부 확인 후 2가지 조건 통과시, User 정보 받아옴
            firestore.collection("users")
                .document(UId)
                .get()
                .addOnSuccessListener {
                    if (it.data == null) {
                        // 1. 가입되어 있지 않은 경우
                        if (continuation.isActive) continuation.resume(Pair(false, null))
                    } else {
                        it.data?.let { userBasicInfo ->
                            if (userBasicInfo["email"] != null &&
                                userBasicInfo["nickname"] != null &&
                                userBasicInfo["age"] != null &&
                                userBasicInfo["yearsPlaying"] != null &&
                                userBasicInfo["average"] != null &&
                                userBasicInfo["introduceMessage"] != null &&
                                userBasicInfo["profileImg"] != null
                            ) {
                                curUser = curUser.copy(
                                    userUId = curUser.userUId,
                                    email = userBasicInfo["email"] as String,
                                    nickname = userBasicInfo["nickname"] as String,
                                    age = (userBasicInfo["age"] as Long).toInt(),
                                    yearsPlaying = (userBasicInfo["yearsPlaying"] as Long).toInt(),
                                    average = (userBasicInfo["average"] as Long).toInt(),
                                    introduceMessage = userBasicInfo["introduceMessage"] as String,
                                    profileImg = userBasicInfo["profileImg"] as String,
                                )
                            } else {
                                // 2. 기본 정보가 입력되어 있지 않은 경우
                                if (continuation.isActive) continuation.resume(Pair(true, null))
                            }
                        }

                        // Team 정보 받아옴
                        val teamInfoTask = firestore.collection("users")
                            .document(UId)
                            .collection("extraInfo")
                            .document("teamInfo")
                            .get()
                            .addOnSuccessListener { userTeamDetails ->
                                userTeamDetails?.let { userTeamDetail ->
                                    curUser = curUser.copy(
                                        userInfo = curUser.userInfo.copy(
                                            teamInfo = TeamInfo(
                                                teamUId = userTeamDetail.get("teamUId") as String?,
                                                isLeader = userTeamDetail.get("isLeader") as Boolean,
                                                isOrganized = userTeamDetail.get("isOrganized") as Boolean
                                            )
                                        )
                                    )
                                }
                            }
                        tasks.add(teamInfoTask)

                        // Group 정보 받아옴
                        val groupsInfoTask = firestore.collection("users")
                            .document(UId)
                            .collection("extraInfo")
                            .document("groupsInfo")
                            .get()
                            .addOnSuccessListener { userGroupDetail ->
                                val groups = userGroupDetail.get("groupsUId") as List<String>?
                                groups?.let { groups ->
                                    curUser = curUser.copy(
                                        userInfo = curUser.userInfo.copy(
                                            groupsInfo = groups.map { GroupInfo(it) }
                                        )
                                    )
                                }
                            }
                        tasks.add(groupsInfoTask)

                        // ThemeTeam 정보 받아옴
                        val themeTeamsInfo = firestore.collection("users")
                            .document(UId)
                            .collection("extraInfo")
                            .document("themeTeamInfo")
                            .get()
                            .addOnSuccessListener { userThemeTeamDetail ->
                                val themeTeams =
                                    userThemeTeamDetail.get("themeTeamUId") as List<String>?
                                themeTeams?.let { themeTeams ->
                                    curUser = curUser.copy(
                                        userInfo = curUser.userInfo.copy(
                                            themeTeamsInfo = themeTeams.map { ThemeTeamInfo(it) }
                                        )
                                    )
                                }
                            }
                        tasks.add(themeTeamsInfo)

                        Tasks.whenAll(tasks)
                            .addOnSuccessListener {
                                // 3. 가입되어 있고, 모든 정보가 입력되어 있는 상태인 경우 정보를 돌려줌
                                CoroutineScope(Dispatchers.IO).launch {
                                    dataStore.storeValue(stringPreferencesKey("userUId"), UId)
                                    dataStore.storeValue(stringPreferencesKey("userEmail"), email)
                                }
                                if (continuation.isActive) continuation.resume(Pair(true, curUser))
                            }
                            .addOnFailureListener {
                                // TODO 실패시 별도 처리
                                if (continuation.isActive) continuation.resume(Pair(false, null))
                            }
                    }
                }
                .addOnFailureListener {
                    // TODO 실패시 별도 처리
                    if (continuation.isActive) continuation.resume(Pair(false, null))
                }
        }
    }

    override suspend fun requestSetUserInfo(user: User, userImg: File): Boolean {
        return suspendCancellableCoroutine { continuation ->
            var curUserUId = ""
            var curUserEmail = ""
            runBlocking {
                curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "saGsdRTdEfeJklbHjIBHGpGRZdj1") ?: ""
                curUserEmail = dataStore.readValue(stringPreferencesKey("userEmail"), "saGsdRTdEfeJklbHjIBHGpGRZdj1") ?: ""
            }

            val storageRef = firebaseStorage.reference
            val userImageExtension =
                if (userImg.path.split(".").last().isNotEmpty()) userImg.path.split(".")
                    .last() else "jpg"
            val userImagesRef = storageRef.child("${curUserUId}.${userImageExtension}")
            val newUser = hashMapOf(
                "nickname" to user.nickname,
                "email" to curUserEmail,
                "age" to user.age,
                "yearsPlaying" to user.yearsPlaying,
                "average" to user.average,
                "introduceMessage" to user.introduceMessage,
                "profileImg" to "${curUserUId}.${userImageExtension}"
            )
            val userDocumentReference = firestore.collection("users")
                .document(curUserUId)

            val tasks = mutableListOf<Task<*>>()
            val setUserTask: Task<Void> = userDocumentReference.set(newUser)
            val uploadTask = userImagesRef.putFile(Uri.fromFile(userImg))
            tasks.add(setUserTask)
            tasks.add(uploadTask)

            Tasks.whenAll(tasks)
                .addOnSuccessListener {
                    if (continuation.isActive) continuation.resume(true)
                }
                .addOnFailureListener {
                    if (continuation.isActive) continuation.resume(false)
                }
        }
    }

    override suspend fun getUsersInfo(nickname: String): List<User> =
        suspendCancellableCoroutine { continuation ->
            val resultUsers = mutableListOf<User>()
            var curUserUId = ""
            runBlocking {
                curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "saGsdRTdEfeJklbHjIBHGpGRZdj1") ?: ""
            }

            firestore.collection("users")
                .get()
                .addOnSuccessListener { users ->
                    val searchTasks = mutableListOf<Task<*>>()
                    for (user in users.documents) {
                        if (user.id != curUserUId) {
                            user.data?.let { userDetail ->
                                if (userDetail["nickname"] == nickname) {
                                    val searchTask = user.reference
                                        .collection("extraInfo")
                                        .document("teamInfo")
                                        .get()
                                        .addOnSuccessListener {
                                            it.data?.let { teamInfo ->
                                                val curUser = User(
                                                    userUId = user.id,
                                                    email = userDetail["email"] as String,
                                                    nickname = userDetail["nickname"] as String?,
                                                    age = (userDetail["age"] as Long?)?.toInt(),
                                                    yearsPlaying = (userDetail["yearsPlaying"] as Long?)?.toInt(),
                                                    average = (userDetail["average"] as Long?)?.toInt(),
                                                    introduceMessage = userDetail["introduceMessage"] as String?,
                                                    profileImg = userDetail["profileImg"] as String?,
                                                    userInfo = UserInfo(
                                                        teamInfo = TeamInfo(
                                                            teamUId = teamInfo["teamUId"] as String?,
                                                            isLeader = teamInfo["isLeader"] as Boolean,
                                                            isOrganized = teamInfo["isOrganized"] as Boolean
                                                        ),
                                                        groupsInfo = listOf(),
                                                        themeTeamsInfo = listOf()
                                                    )
                                                )
                                                resultUsers.add(curUser)
                                            }
                                        }
                                    searchTasks.add(searchTask)
                                }
                            }
                        }
                    }

                    // 모든 검색이 끝나면 resume 처리
                    Tasks.whenAll(searchTasks).addOnSuccessListener {
                        continuation.resume(resultUsers)
                    }
                }
        }

    override suspend fun getUserInfo(UId: String): Pair<User, Boolean> =
        suspendCancellableCoroutine { continuation ->
            var curUserUId = ""
            runBlocking {
                curUserUId = dataStore.readValue(stringPreferencesKey("userUId"), "saGsdRTdEfeJklbHjIBHGpGRZdj1") ?: ""
            }

            firestore.collection("users")
                .document(UId)
                .get()
                .addOnSuccessListener { user ->
                    user.data?.let { userDetail ->
                        user.reference
                            .collection("extraInfo")
                            .document("teamInfo")
                            .get()
                            .addOnSuccessListener {
                                it.data?.let { teamInfo ->
                                    val curUser = User(
                                        userUId = user.id,
                                        email = userDetail["email"] as String,
                                        nickname = userDetail["nickname"] as String?,
                                        age = (userDetail["age"] as Long?)?.toInt(),
                                        yearsPlaying = (userDetail["yearsPlaying"] as Long?)?.toInt(),
                                        average = (userDetail["average"] as Long?)?.toInt(),
                                        introduceMessage = userDetail["introduceMessage"] as String?,
                                        profileImg = userDetail["profileImg"] as String?,
                                        userInfo = UserInfo(
                                            teamInfo = TeamInfo(
                                                teamUId = teamInfo["teamUId"] as String?,
                                                isLeader = teamInfo["isLeader"] as Boolean,
                                                isOrganized = teamInfo["isOrganized"] as Boolean
                                            ),
                                            groupsInfo = listOf(),
                                            themeTeamsInfo = listOf()
                                        )
                                    )
                                    continuation.resume(Pair(curUser, curUserUId == UId))
                                }
                            }
                    }
                }
        }
}