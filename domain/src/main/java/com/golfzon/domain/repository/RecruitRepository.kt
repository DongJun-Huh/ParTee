package com.golfzon.domain.repository

import com.golfzon.domain.model.Recruit
import com.golfzon.domain.model.Times

interface RecruitRepository {
    suspend fun createRecruitPost(recruitInfo: Recruit): String
    suspend fun getRecruits(
        sortDates: String = "latest",
        filterTimes: Times = Times.NONE,
//        filterLocation: String = "", // TODO 지점을 받아올 때 위치를 받아올 수 있다면, location으로 필터링 가능
        isConsecutiveStay: Boolean ?= null,
        isCouple: Boolean ?= null,
        isFreeFee: Boolean ?= null
    ): List<Recruit>
    suspend fun getRecruitDetail(recruitUId: String): Recruit
    suspend fun participateRecruit(recruitUId: String): Boolean
}