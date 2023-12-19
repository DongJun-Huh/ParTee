package com.golfzon.recruit

import android.annotation.SuppressLint
import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.golfzon.core_ui.KeyBoardUtil.hideKeyboard
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.core_ui.extension.toast
import com.golfzon.recruit.databinding.FragmentRecruitCreateBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime

@AndroidEntryPoint
class RecruitCreateFragment : Fragment() {
    private var binding by autoCleared<FragmentRecruitCreateBinding>()
    private val recruitViewModel by activityViewModels<RecruitViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecruitCreateBinding.inflate(inflater, container, false)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setEditTextToScroll()
        setDateClickListener()
        setTimeClickListener()
        setEndDateClickListener()
        setMapClickListener()
        observePlaceInfo()
        observeCreatedRecruitId()
    }

    private fun setDataBindingVariables() {
        binding.apply {
            vm = recruitViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    override fun onStop() {
        with(binding) {
            etRecruitCreateFeeInput.hideKeyboard(requireContext())
            etRecruitCreateIntroduceMessage.hideKeyboard(requireContext())
        }
        super.onStop()
    }

    @SuppressLint("ClickableViewAccessibility")
    private fun setEditTextToScroll() {
        with(binding.etRecruitCreateIntroduceMessage) {
            movementMethod = ScrollingMovementMethod()
            setOnTouchListener { v, event ->
                if (v.id == R.id.et_recruit_create_introduce_message) {
                    v.parent.requestDisallowInterceptTouchEvent(true)
                    when (event.action and MotionEvent.ACTION_MASK) {
                        MotionEvent.ACTION_UP -> v.parent.requestDisallowInterceptTouchEvent(false)
                    }
                }
                false
            }
        }
    }

    private fun setDateClickListener() {
        binding.layoutRecruitCreateDate.setOnDebounceClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    val selectedDate =
                        LocalDate.of(selectedYear, selectedMonth + 1, selectedDayOfMonth)
                    val currentDate = LocalDate.now()

                    if (selectedDate.minusDays(2).isBefore(currentDate)) {
                        // 0. 현재날짜보다 이전날짜를 선택한경우
                        this@RecruitCreateFragment.toast(
                            message = getString(R.string.recruit_select_date_fail),
                            isError = true
                        )
                    } else {
                        if (selectedDate.isBefore(recruitViewModel.createRecruitEndDate.value!!)) {
                            if (selectedDate.minusDays(1).isAfter(LocalDate.now())) {
                                // 1. 선택한 날짜가 모집 마감날짜보다 이전이면서, 선택날짜 - 1이 지나지 않은경우
                                // 선택날짜 적용 + 마감날짜는 선택날짜-1 적용
                                recruitViewModel.createRecruitEndDate.postValue(
                                    selectedDate.minusDays(1)
                                )
                                recruitViewModel.createRecruitDateTime.postValue(
                                    LocalDateTime.of(
                                        selectedDate,
                                        recruitViewModel.createRecruitDateTime.value!!.toLocalTime()
                                    )
                                )
                            } else {
                                // 2. 선택한 날짜가 모집 마감날짜보다 이전이면서, 선택날짜 - 1이 지난 경우
                                // 선택불가
                                this@RecruitCreateFragment.toast(
                                    message = getString(R.string.recruit_select_date_fail),
                                    isError = true
                                )
                            }
                        } else {
                            // 3. 선택한 날짜가 모집 마감날짜보다 이후인 경우
                            recruitViewModel.createRecruitDateTime.postValue(
                                LocalDateTime.of(
                                    selectedDate,
                                    recruitViewModel.createRecruitDateTime.value!!.toLocalTime()
                                )
                            )
                        }
                    }

                }, recruitViewModel.createRecruitDateTime.value!!.year,
                recruitViewModel.createRecruitDateTime.value!!.monthValue - 1,
                recruitViewModel.createRecruitDateTime.value!!.dayOfMonth
            )
            datePickerDialog.show()
        }
    }

    private fun setTimeClickListener() {
        binding.tvRecruitCreateTime.setOnDebounceClickListener {
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, selectedHour, selectedMinute ->
                    recruitViewModel.createRecruitDateTime.postValue(
                        LocalDateTime.of(
                            recruitViewModel.createRecruitDateTime.value!!.year,
                            recruitViewModel.createRecruitDateTime.value!!.monthValue,
                            recruitViewModel.createRecruitDateTime.value!!.dayOfMonth,
                            selectedHour,
                            selectedMinute,
                        )
                    )
                }, recruitViewModel.createRecruitDateTime.value!!.hour,
                recruitViewModel.createRecruitDateTime.value!!.minute,
                true
            )
            timePickerDialog.show()
        }
    }

    private fun setEndDateClickListener() {
        binding.tvRecruitCreateEndDate.setOnDebounceClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    val selectedDate = LocalDate.of(
                        selectedYear,
                        selectedMonth + 1,
                        selectedDayOfMonth
                    )
                    if (selectedDate.minusDays(1).isBefore(LocalDate.now())) {
                        // 0. 현재날짜보다 이전날짜를 선택한경우
                        this@RecruitCreateFragment.toast(
                            message = getString(R.string.recruit_select_end_date_fail),
                            isError = true
                        )
                    } else {
                        if (selectedDate.minusDays(1L)
                                .isBefore(recruitViewModel.createRecruitDateTime.value!!.toLocalDate())
                            && selectedDate.until(recruitViewModel.createRecruitDateTime.value!!.toLocalDate()).days >= 1
                        ) {
                            // 1. 모집 마감날짜가 모집 시작날짜보다 이전인 경우
                            recruitViewModel.createRecruitEndDate.postValue(selectedDate)
                        } else {
                            // 2. 모집 마감날짜가 모집 시작날짜와 같거나 이후인 경우
                            this@RecruitCreateFragment.toast(
                                message = getString(R.string.recruit_end_date_guide),
                                isError = true
                            )
                        }
                    }
                }, recruitViewModel.createRecruitEndDate.value!!.year,
                recruitViewModel.createRecruitEndDate.value!!.monthValue - 1,
                recruitViewModel.createRecruitEndDate.value!!.dayOfMonth
            )
            datePickerDialog.show()
        }
    }

    private fun setMapClickListener() {
        binding.tvRecruitCreatePlace.setOnDebounceClickListener {
            val action =
                RecruitCreateFragmentDirections.actionRecruitCreateFragmentToMapFinderFragment()
            findNavController().navigate(action)
        }
    }

    private fun observePlaceInfo() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("recruitPlaceName")
            ?.observe(viewLifecycleOwner) { placeName ->
                recruitViewModel.createRecruitPlaceName.postValue(placeName)
            }
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("recruitPlaceUId")
            ?.observe(viewLifecycleOwner) { placeUId ->
                recruitViewModel.createRecruitPlaceUId.postValue(placeUId)
            }
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("recruitPlaceRoadAddress")
            ?.observe(viewLifecycleOwner) { roadAddress ->
                recruitViewModel.createRecruitPlaceRoadAddress.postValue(roadAddress)
            }
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("recruitPlacePastAddress")
            ?.observe(viewLifecycleOwner) { pastAddress ->
                recruitViewModel.createRecruitPlacePastAddress.postValue(pastAddress)
            }
    }

    private fun observeCreatedRecruitId() {
        recruitViewModel.createdRecruitId.observe(viewLifecycleOwner) { event ->
            event.getContentIfNotHandled()?.let { recruitId ->
                if (recruitId.isEmpty()) {
                    this@RecruitCreateFragment.toast(
                        message = getString(R.string.recruit_create_fail),
                        isError = true
                    )
                } else {
                    findNavController().navigate(
                        RecruitCreateFragmentDirections.actionRecruitCreateFragmentToRecruitDetailFragment(
                            recruitId
                        )
                    )
                }
            }
        }
    }
}