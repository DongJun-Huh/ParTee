package com.golfzon.group

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.core_ui.extension.toast
import com.golfzon.core_ui.map.WebViewTouchEventCallback
import com.golfzon.group.databinding.FragmentGroupCreateRoomScreenBinding
import dagger.hilt.android.AndroidEntryPoint
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

@AndroidEntryPoint
class GroupCreateRoomScreenFragment : Fragment() {
    private var binding by autoCleared<FragmentGroupCreateRoomScreenBinding>()
    private val groupViewModel by activityViewModels<GroupViewModel>()
    private val args by navArgs<GroupCreateRoomScreenFragmentArgs>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentGroupCreateRoomScreenBinding.inflate(inflater, container, false)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.groupUId = args.groupUId
        setMap()
        observePlaceInfo()
        setDateClickListener()
        setTimeClickListener()
        observeCreateSuccess()
    }

    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = groupViewModel
        }
    }

    private fun createGroupRoomScreen() {
        // TODO
    }

    private fun setDateClickListener() {
        binding.layoutCreateRoomScreenDate.setOnDebounceClickListener {
            val datePickerDialog = DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDayOfMonth ->
                    val selectedDate =
                        LocalDate.of(selectedYear, selectedMonth + 1, selectedDayOfMonth)
                    val currentDate = LocalDate.now()

                    if (selectedDate.minusDays(1).isBefore(currentDate)) {
                        // 0. 현재날짜보다 이전날짜를 선택한경우
                        this@GroupCreateRoomScreenFragment.toast(
                            message = getString(R.string.create_room_screen_select_date_fail),
                            isError = true
                        )
                    } else {
                        groupViewModel.createRoomScreenDateTime.postValue(
                            LocalDateTime.of(
                                selectedDate,
                                groupViewModel.createRoomScreenDateTime.value!!.toLocalTime()
                            )
                        )
                    }

                }, groupViewModel.createRoomScreenDateTime.value!!.year,
                groupViewModel.createRoomScreenDateTime.value!!.monthValue - 1,
                groupViewModel.createRoomScreenDateTime.value!!.dayOfMonth
            )
            datePickerDialog.show()
        }
    }

    private fun setTimeClickListener() {
        binding.tvCreateRoomScreenTime.setOnDebounceClickListener {
            val timePickerDialog = TimePickerDialog(
                requireContext(),
                { _, selectedHour, selectedMinute ->
                    groupViewModel.createRoomScreenDateTime.postValue(
                        LocalDateTime.of(
                            groupViewModel.createRoomScreenDateTime.value!!.toLocalDate(),
                            LocalTime.of(
                                selectedHour,
                                selectedMinute,
                            )
                        )
                    )
                }, groupViewModel.createRoomScreenDateTime.value!!.hour,
                groupViewModel.createRoomScreenDateTime.value!!.minute,
                true
            )
            timePickerDialog.show()
        }
    }


    private fun setMap() {
        val golfzonWebViewClient = object : WebViewClient() {
            override fun onPageFinished(view: WebView?, url: String?) {
                // 기존 페이지의 Back button element 제거
                view?.evaluateJavascript(
                    "javascript:(function() { " +
                            "var target = document.querySelector('.forweb');" +
                            "var observer = new MutationObserver(function(mutations) { " +
                            "   mutations.forEach(function(mutation) { " +
                            "       document.querySelector('.btn_back').classList.remove('btn_back')" +
                            "   });" +
                            "});" +
                            "observer.observe(target, { childList: true, subtree: true });" +
                            "})();", null
                )
                super.onPageFinished(view, url)
            }
        }

        binding.cardviewCreateRoomScreenPlaceMap.setTouchEventCallback(object :
            WebViewTouchEventCallback {
            override fun onEvent(event: MotionEvent?) {
                findNavController().navigate(
                    GroupCreateRoomScreenFragmentDirections.actionGroupCreateRoomScreenFragmentToMapFinderFragment()
                )
            }
        })
        binding.tvCreateRoomScreenPlace.setOnDebounceClickListener {
            findNavController().navigate(
                GroupCreateRoomScreenFragmentDirections.actionGroupCreateRoomScreenFragmentToMapFinderFragment()
            )
        }

        with(binding.webviewCreateRoomScreenPlaceMap) {
            isEnabled = false
            setBackgroundColor(0)
            webViewClient = golfzonWebViewClient
            settings.javaScriptEnabled = true
            loadUrl("https://m.golfzon.com/booking/#/booking/map/view/")
        }

        groupViewModel.createRoomScreenPlaceUId.observe(viewLifecycleOwner) { placeUId ->
            if (placeUId != null) {
                binding.webviewCreateRoomScreenPlaceMap.loadUrl("https://m.golfzon.com/booking/#/booking/map/view/${placeUId}")

                binding.cardviewCreateRoomScreenPlaceMap.setTouchEventCallback(object :
                    WebViewTouchEventCallback {
                    override fun onEvent(event: MotionEvent?) {
                        findNavController().navigate(
                            GroupCreateRoomScreenFragmentDirections.actionGroupCreateRoomScreenFragmentToMapFinderFragment(
                                recruitPlaceUId = placeUId,
                                isEdit = true
                            )
                        )
                    }
                })

                binding.tvCreateRoomScreenPlace.setOnDebounceClickListener {
                    findNavController().navigate(
                        GroupCreateRoomScreenFragmentDirections.actionGroupCreateRoomScreenFragmentToMapFinderFragment(
                            recruitPlaceUId = placeUId,
                            isEdit = true
                        )
                    )
                }
            }
        }
    }

    private fun observePlaceInfo() {
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("recruitPlaceName")
            ?.observe(viewLifecycleOwner) { placeName ->
                groupViewModel.createRoomScreenPlaceName.postValue(placeName)
            }
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("recruitPlaceUId")
            ?.observe(viewLifecycleOwner) { placeUId ->
                groupViewModel.createRoomScreenPlaceUId.postValue(placeUId)
            }
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("recruitPlaceRoadAddress")
            ?.observe(viewLifecycleOwner) { roadAddress ->
                groupViewModel.createRoomScreenPlaceRoadAddress.postValue(roadAddress)
            }
        findNavController().currentBackStackEntry?.savedStateHandle?.getLiveData<String>("recruitPlacePastAddress")
            ?.observe(viewLifecycleOwner) { pastAddress ->
                groupViewModel.createRoomScreenPlacePastAddress.postValue(pastAddress)
            }
    }

    private fun observeCreateSuccess() {
        groupViewModel.isCreateScreenRoomSuccess.observe(viewLifecycleOwner) { isSuccess ->
            if (isSuccess.getContentIfNotHandled() == true) {
                // TODO 글 작성 이후 과정 작성
            }
        }
    }
}