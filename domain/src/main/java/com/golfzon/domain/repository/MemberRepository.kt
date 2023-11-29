package com.golfzon.domain.repository

import com.golfzon.domain.model.User
import java.io.File

interface MemberRepository {
    suspend fun requestRegisterUser(UId: String, email: String): Boolean
    suspend fun requestLogin(UId: String, email: String): Pair<Boolean, User?>
    suspend fun requestSetUserInfo(user: User, userImg: File): Boolean
    suspend fun getUsersInfo(nickname: String): List<User>
}