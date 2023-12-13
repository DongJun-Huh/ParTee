package com.golfzon.domain.repository

import com.golfzon.domain.model.Recruit

interface RecruitRepository {
    suspend fun createRecruitPost(recruitInfo: Recruit): Boolean
    suspend fun getRecruits(): List<Recruit>
}