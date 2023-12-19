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
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.extension.setOnDebounceClickListener
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
    }

    private fun setDataBindingVariables() {
        binding.apply {
            vm = recruitViewModel
            lifecycleOwner = viewLifecycleOwner
        }
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
                    recruitViewModel.createRecruitDateTime.postValue(
                        LocalDateTime.of(
                            selectedYear,
                            selectedMonth + 1,
                            selectedDayOfMonth,
                            recruitViewModel.createRecruitDateTime.value!!.hour,
                            recruitViewModel.createRecruitDateTime.value!!.minute,
                        )
                    )
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
                    recruitViewModel.createRecruitEndDate.postValue(
                        LocalDate.of(
                            selectedYear,
                            selectedMonth + 1,
                            selectedDayOfMonth
                        )
                    )
                }, recruitViewModel.createRecruitEndDate.value!!.year,
                recruitViewModel.createRecruitEndDate.value!!.monthValue - 1,
                recruitViewModel.createRecruitEndDate.value!!.dayOfMonth
            )
            datePickerDialog.show()
        }
    }

    private fun setMapClickListener() {
        binding.tvRecruitCreatePlace.setOnDebounceClickListener {
            val action = RecruitCreateFragmentDirections.actionRecruitCreateFragmentToMapFinderFragment()
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
}