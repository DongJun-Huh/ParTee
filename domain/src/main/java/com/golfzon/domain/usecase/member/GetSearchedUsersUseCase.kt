package com.golfzon.domain.usecase.member

import com.golfzon.domain.repository.MemberRepository
import javax.inject.Inject

class GetSearchedUsersUseCase @Inject constructor(private val memberRepository: MemberRepository) {
    suspend operator fun invoke(nickname: String) =
        memberRepository.getUsersInfo(nickname = nickname)
}