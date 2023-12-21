package com.golfzon.domain.usecase.recruit

import com.golfzon.domain.model.Times
import com.golfzon.domain.repository.RecruitRepository
import javax.inject.Inject

class GetRecruitsUseCase @Inject constructor(private val recruitRepository: RecruitRepository) {
    suspend operator fun invoke(
        sortDates: String = "latest",
        filterTimes: Times = Times.NONE,
        isConsecutiveStay: Boolean? = null,
        isCouple: Boolean? = null,
        isFreeFee: Boolean? = null
    ) = recruitRepository.getRecruits(
        sortDates,
        filterTimes,
        isConsecutiveStay,
        isCouple,
        isFreeFee
    )
}