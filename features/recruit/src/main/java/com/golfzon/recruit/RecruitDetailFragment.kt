package com.golfzon.recruit

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.golfzon.core_ui.HorizontalMarginItemDecoration
import com.golfzon.core_ui.adapter.CandidateTeamMemberAdapter
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.dp
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.recruit.databinding.FragmentRecruitDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.Period
import java.time.format.DateTimeFormatter
import java.time.format.TextStyle
import java.time.temporal.ChronoUnit
import java.util.Locale

@AndroidEntryPoint
class RecruitDetailFragment : Fragment() {
    private var binding by autoCleared<FragmentRecruitDetailBinding> { onDestroyBindingView() }
    private val recruitViewModel by activityViewModels<RecruitViewModel>()
    private val args by navArgs<RecruitDetailFragmentArgs>()
    private var recruitDetailMembersAdapter: CandidateTeamMemberAdapter? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecruitDetailBinding.inflate(inflater, container, false)
        setDataBindingVariables()
        getRecruitDetail()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecruitDetailMembersAdapter()
        getRecruitDetailMembers()
        observeRecruitMembers()
        setBackClickListener()
        setParticipateClickListener()
        observeParticipateSuccess()
    }

    private fun onDestroyBindingView() {
        recruitDetailMembersAdapter = null
    }

    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = recruitViewModel
        }
    }
    private fun setBackClickListener() {
        binding.btnRecruitDetailAppbarBack.setOnDebounceClickListener {
            findNavController().navigateUp()
        }
    }

    private fun getRecruitDetail() {
        recruitViewModel.getRecruitDetail(args.recruitUId)
    }

    private fun getRecruitDetailMembers() {
        recruitViewModel.curRecruitDetail.observe(viewLifecycleOwner) { curRecruitDetailValue ->
            with(curRecruitDetailValue.getContentIfNotHandled()) {
                if (this != null) {
                    this?.let { recruitDetail ->
//                        binding.recruitDetail = recruitDetail
                        with(binding) {
                            layoutRecruitDetailBadgeCouple.isVisible = recruitDetail.isCouple
                            layoutRecruitDetailBadgeSoonEnd.isVisible = LocalDateTime.now().until(recruitDetail.recruitDateTime, ChronoUnit.DAYS) < 5
                            layoutRecruitDetailBadgeConsecutive.isVisible = recruitDetail.isConsecutiveStay
                            layoutRecruitDetailBadgeMoney.isVisible = recruitDetail.fee == 0
                            tvRecruitDetailDate.text = recruitDetail.recruitDateTime.getDayOfWeek().getDisplayName(
                                TextStyle.FULL, Locale.KOREAN)
                            tvRecruitDetailDay.text = recruitDetail.recruitDateTime.dayOfMonth.toString()
                            tvRecruitDetailTime.text = recruitDetail.recruitDateTime.format(DateTimeFormatter.ofPattern("a hh:mm"))
                            tvRecruitDetailPlace.text = recruitDetail.recruitPlace
                            tvRecruitDetailEndDate.text =
                                recruitDetail.recruitEndDateTime.format(DateTimeFormatter.ofPattern("MM월 dd일"))
                            tvRecruitDetailEndDateDDay.text =
                                if (Period.between(LocalDate.now(), recruitDetail.recruitEndDateTime.toLocalDate()).days < 0) "모집마감"
                                else "D-"+Period.between(LocalDate.now(), recruitDetail.recruitEndDateTime.toLocalDate()).days
                            tvRecruitDetailFee.text = "${NumberFormat.getCurrencyInstance(Locale.KOREA).format(recruitDetail.fee)}원"
                            tvRecruitDetailConsecutiveStay.text = if (recruitDetail.isConsecutiveStay) "O" else "X"
                            tvRecruitDetailLeftHeadCount.text =
                                if (recruitDetail.searchingHeadCount - recruitDetail.headCount <= 0)
                                    "모집이 마감되었어요!"
                                else "모집 인원이 ${recruitDetail.searchingHeadCount - recruitDetail.headCount}명 남았어요!"
                            tvRecruitDetailIntroduceMessage.text = recruitDetail.recruitIntroduceMessage
                            tvRecruitDetailPlace.text = recruitDetail.recruitPlace
                            tvRecruitDetailParticipateLeftTime
                                .visibility = if (LocalDateTime.now().until(recruitDetail.recruitEndDateTime, ChronoUnit.HOURS) < 1 &&
                                recruitDetail.recruitEndDateTime.isAfter(LocalDateTime.now())
                            ) View.VISIBLE else View.GONE
                            tvRecruitDetailParticipateLeftTime
                                .text = "모집 마감까지 ${LocalDateTime.now().until(recruitDetail.recruitEndDateTime, ChronoUnit.MINUTES)}:${LocalDateTime.now().until(recruitDetail.recruitEndDateTime, ChronoUnit.SECONDS)} 남았어요"
                            tvRecruitDetailPlaceTitle.text = recruitDetail.recruitPlace
                        }
                        getRecruitMembers(recruitDetail.membersUId)
                    }
                }
            }
        }
    }

    private fun getRecruitMembers(membersUId: List<String>) {
        recruitViewModel.getRecruitMembersInfo(membersUId)
    }

    private fun setRecruitDetailMembersAdapter() {
        recruitDetailMembersAdapter = CandidateTeamMemberAdapter(itemHeight = 52.dp,isCircleImage = true)
        binding.rvRecruitDetailParticipants.apply {
            adapter = recruitDetailMembersAdapter
            addItemDecoration(HorizontalMarginItemDecoration(8.dp))
        }
    }

    private fun observeRecruitMembers() {
        recruitViewModel.recruitMembers.observe(viewLifecycleOwner) { recruitMembers ->
            recruitDetailMembersAdapter?.submitList(recruitMembers)
        }
    }

    private fun setParticipateClickListener() {
        binding.btnRecruitDetailParticipate.setOnDebounceClickListener {
            recruitViewModel.participateRecruit(recruitUId = args.recruitUId)
        }
    }

    private fun observeParticipateSuccess() {
        recruitViewModel.isParticipateSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess.getContentIfNotHandled() == true) {
                getRecruitDetail()
            }
        }
    }
}