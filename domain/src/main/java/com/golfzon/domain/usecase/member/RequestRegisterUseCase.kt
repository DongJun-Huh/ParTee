package com.golfzon.domain.usecase.member

import com.golfzon.domain.repository.MemberRepository
import javax.inject.Inject

class RequestRegisterUseCase @Inject constructor(private val memberRepository: MemberRepository) {
    suspend operator fun invoke(UId: String, email: String) =
        memberRepository.requestRegisterUser(UId, email)
}