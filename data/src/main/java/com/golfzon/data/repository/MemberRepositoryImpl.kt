package com.golfzon.data.repository

import com.golfzon.domain.model.GroupInfo
import com.golfzon.domain.model.TeamInfo
import com.golfzon.domain.model.ThemeTeamInfo
import com.golfzon.domain.model.User
import com.golfzon.domain.model.UserInfo
import com.golfzon.domain.repository.MemberRepository
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.firebase.firestore.FirebaseFirestore
import javax.inject.Inject
import kotlin.coroutines.resume
import kotlin.coroutines.suspendCoroutine

class MemberRepositoryImpl @Inject constructor(
    private val firestore: FirebaseFirestore
) : MemberRepository {
    override suspend fun requestRegisterUser(UId: String, email: String): Boolean {
        return suspendCoroutine { continuation ->
            val newUser = hashMapOf(
                "email" to email,
                "nickname" to null,
                "age" to null,
                "yearsPlaying" to null,
                "average" to null,
                "introduceMessage" to null,
                "profileImg" to null
            )

            val registerTask = firestore.collection("users")
                .document(UId)
                .set(newUser)
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

                    firestore.collection("users")
                        .document(UId)
                        .collection("extraInfo")
                        .document("teamInfo")
                        .set(newUserTeamInfo)

                    firestore.collection("users")
                        .document(UId)
                        .collection("extraInfo")
                        .document("groupsInfo")
                        .set(newUserGroupsInfo)

                    firestore.collection("users")
                        .document(UId)
                        .collection("extraInfo")
                        .document("themeTeamsInfo")
                        .set(newUserThemeTeamsInfo)
                }
            registerTask
                .addOnSuccessListener {
                    continuation.resume(true)
                }
                .addOnFailureListener {
                    continuation.resume(false)
                }
        }
    }

    // Return Value: (최초가입여부, 유저정보)
    override suspend fun requestLogin(UId: String, email: String): Pair<Boolean, User?> {
        return suspendCoroutine { continuation ->
            var isUserExist = false
            var isUserInitialized = false

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
                        isUserExist = false
                        continuation.resume(Pair(false, null))
                    } else {
                        isUserExist = true
                        it.data?.let { userBasicInfo ->
                            if (userBasicInfo["email"] != null &&
                                userBasicInfo["nickname"] != null &&
                                userBasicInfo["age"] != null &&
                                userBasicInfo["yearsPlaying"] != null &&
                                userBasicInfo["average"] != null &&
                                userBasicInfo["introduceMessage"] != null &&
                                userBasicInfo["profileImg"] != null
                            ) {
                                isUserInitialized = true

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
                                continuation.resume(Pair(true, null))
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
                                continuation.resume(Pair(true, curUser))
                            }
                            .addOnFailureListener {
                                // TODO 실패시 별도 처리
                                continuation.resume(Pair(false, null))
                            }
                    }
                }
                .addOnFailureListener {
                    // TODO 실패시 별도 처리
                    continuation.resume(Pair(false, null))
                }
        }
    }
}