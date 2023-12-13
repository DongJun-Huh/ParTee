package com.golfzon.domain.usecase.recruit

import com.golfzon.domain.model.Recruit
import com.golfzon.domain.repository.RecruitRepository
import javax.inject.Inject

class RequestCreateRecruitUseCase @Inject constructor(private val recruitRepository: RecruitRepository) {
    suspend operator fun invoke(recruitInfo: Recruit) =
        recruitRepository.createRecruitPost(recruitInfo = recruitInfo)
}