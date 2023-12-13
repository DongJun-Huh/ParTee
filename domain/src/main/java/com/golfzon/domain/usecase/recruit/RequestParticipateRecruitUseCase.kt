package com.golfzon.domain.usecase.recruit

import com.golfzon.domain.repository.RecruitRepository
import javax.inject.Inject

class RequestParticipateRecruitUseCase @Inject constructor(private val recruitRepository: RecruitRepository) {
    suspend operator fun invoke(recruitUId: String) =
        recruitRepository.participateRecruit(recruitUId)
}