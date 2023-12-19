package com.golfzon.recruit

import androidx.lifecycle.LiveData
import androidx.lifecycle.MediatorLiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfzon.core_ui.Event
import com.golfzon.core_ui.ListLiveData
import com.golfzon.domain.model.Recruit
import com.golfzon.domain.model.User
import com.golfzon.domain.usecase.member.GetUserInfoUseCase
import com.golfzon.domain.usecase.recruit.GetRecruitDetailUseCase
import com.golfzon.domain.usecase.recruit.GetRecruitsUseCase
import com.golfzon.domain.usecase.recruit.RequestCreateRecruitUseCase
import com.golfzon.domain.usecase.recruit.RequestParticipateRecruitUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import javax.inject.Inject

@HiltViewModel
class RecruitViewModel @Inject constructor(
    private val getUserInfoUseCase: GetUserInfoUseCase,
    private val createRecruitUseCase: RequestCreateRecruitUseCase,
    private val getRecruitsUseCase: GetRecruitsUseCase,
    private val getRecruitDetailUseCase: GetRecruitDetailUseCase,
    private val participateRecruitUseCase: RequestParticipateRecruitUseCase
) : ViewModel() {
    private val _createdRecruitId = MutableLiveData<Event<String>>()
    val createdRecruitId: LiveData<Event<String>> get() = _createdRecruitId

    private val _recruits = ListLiveData<Recruit>()
    val recruits: ListLiveData<Recruit> get() = _recruits

    private val _curRecruitDetail = MutableLiveData<Event<Recruit>>()
    val curRecruitDetail: LiveData<Event<Recruit>> get() = _curRecruitDetail

    private val _recruitsMembers = ListLiveData<List<Pair<User, Boolean>>>()
    val recruitsMembers: ListLiveData<List<Pair<User, Boolean>>> get() = _recruitsMembers
    private val _recruitsDisplayInfo =
        MediatorLiveData<List<Pair<Recruit, List<User>>>>().apply {
            addSource(_recruits) { updateRecruitsDisplayInfo() }
            addSource(_recruitsMembers) { updateRecruitsDisplayInfo() }
        }
    val recruitsDisplayInfo: MediatorLiveData<List<Pair<Recruit, List<User>>>> get() = _recruitsDisplayInfo

    private val _recruitMembers = ListLiveData<User>()
    val recruitMembers: ListLiveData<User> get() = _recruitMembers

    private val _isParticipateSuccess = MutableLiveData<Event<Boolean>>()
    val isParticipateSuccess: LiveData<Event<Boolean>> get() = _isParticipateSuccess

    var createRecruitDateTime = MutableLiveData<LocalDateTime>(LocalDateTime.now().plusDays(7L))
    var createRecruitEndDate = MutableLiveData<LocalDate>(LocalDate.now().plusDays(6L))
    var createRecruitPlaceName = MutableLiveData<String>()
    var createRecruitPlaceUId = MutableLiveData<String>()
    var createRecruitPlaceRoadAddress = MutableLiveData<String>()
    var createRecruitPlacePastAddress = MutableLiveData<String>()
    var createRecruitFee = MutableLiveData<String>()
    val createRecruitHeadCount = MutableLiveData<Int>()
    val createRecruitHeadCountCheckedButtonId = MutableLiveData<Int>()
    val createRecruitIsConsecutiveStay = MutableLiveData<Boolean>()
    val createRecruitIsConsecutiveStayCheckedButtonId = MutableLiveData<Int>()
    val createRecruitIsCouple = MutableLiveData<Boolean>()
    val createRecruitIsCoupleCheckedButtonId = MutableLiveData<Int>()
    val creteRecruitIntroduceMessage = MutableLiveData<String>()

    fun getRecruitsMembersInfo(recruitsMembersUId: List<List<String>>) = viewModelScope.launch {
        _recruitsMembers.replaceAll(
            recruitsMembersUId.map { recruitMembers ->
                recruitMembers.map {
                    val curUserInfo = getUserInfoUseCase(it)
                    Pair(curUserInfo.first, curUserInfo.second)
                }
            },
            true
        )
    }

    fun getRecruitMembersInfo(recruitMembersUId: List<String>) = viewModelScope.launch {
        _recruitMembers.replaceAll(
            recruitMembersUId.map {
                val curUserInfo = getUserInfoUseCase(it)
                curUserInfo.first
            },
            true
        )
    }

    fun createRecruit() = viewModelScope.launch {
        createRecruitUseCase(
            Recruit(
                recruitUId = "",
                leaderUId = "",
                membersUId = emptyList(),
                headCount = 1,
                searchingHeadCount = createRecruitHeadCount.value ?: 0,
                recruitDateTime = createRecruitDateTime.value ?: LocalDateTime.now(),
                recruitPlaceName = createRecruitPlaceName.value ?: "",
                recruitPlaceUId = createRecruitPlaceUId.value ?: "",
                recruitPlaceRoadAddress = createRecruitPlaceRoadAddress.value ?: "",
                recruitPlacePastAddress = createRecruitPlacePastAddress.value ?: "",
                recruitEndDateTime = LocalDateTime.of(
                    createRecruitEndDate.value,
                    LocalTime.of(0, 0)
                ) ?: LocalDateTime.now(),
                openChatUrl = "",
                fee = createRecruitFee.value?.toInt() ?: 0,
                isConsecutiveStay = createRecruitIsConsecutiveStay.value ?: false,
                isCouple = createRecruitIsCouple.value ?: false,
                recruitIntroduceMessage = creteRecruitIntroduceMessage.value ?: "",
            )
        ).let { recruitId ->
            _createdRecruitId.postValue(Event(recruitId))
        }
    }

    fun getRecruits() = viewModelScope.launch {
        getRecruitsUseCase().let {
            _recruits.replaceAll(it, true)
        }
    }

    fun getRecruitDetail(recruitUId: String) = viewModelScope.launch {
        getRecruitDetailUseCase(recruitUId).let {
            _curRecruitDetail.postValue(Event(it))
        }
    }

    fun participateRecruit(recruitUId: String) = viewModelScope.launch {
        participateRecruitUseCase(recruitUId = recruitUId).let { isSuccess ->
            _isParticipateSuccess.postValue(Event(isSuccess))
        }
    }

    private fun updateRecruitsDisplayInfo() {
        val recruitsList = _recruits.value ?: return
        val membersList = _recruitsMembers.value ?: return

        val updatedDisplayInfo = recruitsList.mapIndexed { index, recruit ->
            Pair(recruit, membersList.getOrNull(index)
                ?.map { it.first }
                ?: emptyList()
            )
        }
        _recruitsDisplayInfo.value = updatedDisplayInfo
    }

    fun updateConsecutiveStay(checkedId: Int) {
        createRecruitIsConsecutiveStayCheckedButtonId.value = checkedId
        createRecruitIsConsecutiveStay.value = when (checkedId) {
            R.id.rb_recruit_create_consecutive_stay_true -> true
            R.id.rb_recruit_create_consecutive_stay_false -> false
            else -> null
        }
    }

    fun updateHeadcount(checkedId: Int) {
        createRecruitHeadCountCheckedButtonId.value = checkedId
        createRecruitHeadCount.value = when (checkedId) {
            R.id.rb_recruit_create_searching_head_count_1 -> 1
            R.id.rb_recruit_create_searching_head_count_2 -> 2
            R.id.rb_recruit_create_searching_head_count_3 -> 3
            else -> null
        }
    }

    fun updateCouple(checkedId: Int) {
        createRecruitIsCoupleCheckedButtonId.value = checkedId
        createRecruitIsCouple.value = when (checkedId) {
            R.id.rb_recruit_create_couple_true -> true
            R.id.rb_recruit_create_couple_false -> false
            else -> null
        }
    }
}