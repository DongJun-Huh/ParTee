package com.golfzon.domain.usecase.member

import com.golfzon.domain.repository.MemberRepository
import javax.inject.Inject

class GetCurUserInfoUseCase @Inject constructor(private val memberRepository: MemberRepository) {
    suspend operator fun invoke() =
        memberRepository.getCurUserInfo()
}