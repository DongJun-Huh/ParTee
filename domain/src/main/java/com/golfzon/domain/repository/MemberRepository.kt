package com.golfzon.domain.repository

import com.golfzon.domain.model.User

interface MemberRepository {
    suspend fun requestRegisterUser(UId: String, email: String): Boolean
    suspend fun requestLogin(UId: String, email: String): Pair<Boolean, User?>
    suspend fun requestSetUserInfo(UId: String, user: User): Boolean
}