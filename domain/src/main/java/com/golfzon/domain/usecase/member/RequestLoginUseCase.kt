package com.golfzon.domain.usecase.member

import com.golfzon.domain.repository.MemberRepository
import javax.inject.Inject

class RequestLoginUseCase @Inject constructor(private val memberRepository: MemberRepository) {
    suspend operator fun invoke(UId: String, email: String) =
        memberRepository.requestLogin(UId = UId, email = email)
}