package com.golfzon.domain.usecase.member

import com.golfzon.domain.model.User
import com.golfzon.domain.repository.MemberRepository
import javax.inject.Inject

class RequestSetUserInfoUseCase @Inject constructor(private val memberRepository: MemberRepository) {
    suspend operator fun invoke(UId: String, userInfo: User) =
        memberRepository.requestSetUserInfo(UId, userInfo)
}