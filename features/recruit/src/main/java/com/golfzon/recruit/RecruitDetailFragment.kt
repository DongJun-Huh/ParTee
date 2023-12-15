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
import com.golfzon.core_ui.adapter.itemDecoration.HorizontalMarginItemDecoration
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
                    this?.let        { recruitDetail ->
                        with(binding) {
                            this.recruitDetail = recruitDetail
                            tvRecruitDetailEndDateDDay.text =
                                if (Period.between(LocalDate.now(), recruitDetail.recruitEndDateTime.toLocalDate()).days < 0 ||
                                    recruitDetail.searchingHeadCount - recruitDetail.headCount <= 0
                                    ) getString(R.string.participate_end)
                                else "D-"+Period.between(LocalDate.now(), recruitDetail.recruitEndDateTime.toLocalDate()).days
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