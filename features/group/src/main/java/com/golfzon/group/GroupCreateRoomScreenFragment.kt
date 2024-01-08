package com.golfzon.group

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.core_ui.extension.toast
import com.golfzon.core_ui.getColorHex
import com.golfzon.core_ui.map.WebViewTouchEventCallback
import com.golfzon.group.databinding.FragmentGroupCreateRoomScreenBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
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
        observePlaceInfo()
        setMap()
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
            override fun shouldInterceptRequest(
                view: WebView?,
                request: WebResourceRequest?
            ): WebResourceResponse? {
                val uri = request!!.url
                if (uri.toString().endsWith("pretendard_medium.otf")) {
                    try {
                        val stream = view?.context
                            ?.applicationContext?.assets
                            ?.open("fonts/pretendard_medium.otf")
                        return WebResourceResponse("fonts/otf", "UTF-8", stream)
                    } catch (e: IOException) {
                        e.printStackTrace() // 글꼴 파일 로드 실패 처리
                    }
                }
                return super.shouldInterceptRequest(view, request)
            }
            override fun onPageFinished(view: WebView?, url: String?) {
                val topBarColor = ContextCompat.getColor(
                    requireContext(),
                    com.golfzon.core_ui.R.color.gray_600_333636
                ).getColorHex
                val topBarTextColor = ContextCompat.getColor(
                    requireContext(),
                    com.golfzon.core_ui.R.color.white
                ).getColorHex
                val topBarTextSize = 14

                var address1: String = ""
                var address2: String = ""

                groupViewModel.createRoomScreenPlaceRoadAddress.value?.let {
                    val addresses = it.split(" ")
                    address1 = addresses[0]
                    address2 = addresses[1]
                }
                val addressTagAddScript =
                    "       var container = document.createElement('div');" +
                            "       container.style.display = 'flex';" +
                            "       container.style.alignItems = 'center';" +
                            "       container.style.justifyContent = 'center';" +
                            "" +
                            "       var newBox = document.createElement('div'); " +
                            "       newBox.style.display = 'flex';" +
                            "       newBox.style.alignItems = 'center';" +
                            "       newBox.style.justifyContent = 'center';" +
                            "       newBox.style.height = '24px';" +
                            "       newBox.style.backgroundColor = '#272929';" +
                            "       newBox.style.borderRadius = '8px';" +
                            "       newBox.style.margin = '0px 8px 0px 12px';" +
                            "       newBox.style.padding = '0px 4px 0px 4px';" +
                            "       newBox.style.border = '1px solid #9FF06E';" +
                            "" +
                            "       var textElement = document.createElement('span');" +
                            "       textElement.textContent = '${address1} ${address2}';" +
                            "       textElement.style.color = '#9FF06E';" +
                            "       textElement.style.fontWeight = 'bold';" +
                            "       textElement.style.fontSize = '10px';" +
                            "       newBox.appendChild(textElement);" +
                            "" +
                            "       var splitPlaceName = document.querySelector('h1').textContent.split(']');" +
                            "       document.querySelector('h1').textContent = splitPlaceName[splitPlaceName.length - 1];" +
                            "       var h1Element = document.querySelector('h1');" +
                            "       var h1Text = document.createElement('span');" +
                            "       h1Text.textContent = h1Element.textContent;" +
                            "       h1Element.textContent = '';" +
                            "       h1Element.appendChild(container);" +
                            "       container.appendChild(newBox);" +
                            "       container.appendChild(h1Text);"

                val loadFontScript =
                    "var pretendard = new FontFace('PretendardMedium', 'url(pretendard_medium.otf)');" +
                            "pretendard.load().then(function(loadedFont) {" +
                            "    document.fonts.add(loadedFont);" +
                            "    document.body.style.fontFamily = 'PretendardMedium';" +
                            "}).catch(function(error) {" +
                            "    console.log('Failed to load Pretendard font: ' + error);" +
                            "});"
                val customTopBarScript = "javascript:(function() { " +
                        "var target = document.querySelector('.forweb');" +
                        "var observer = new MutationObserver(function(mutations) { " +
                        "   mutations.forEach(function(mutation) { " +
                        "       document.querySelector('.fullsize_map').style.overflow = 'visible';" +
                        "       document.querySelector('.btn_back').classList.remove('btn_back');" +
                        "       document.querySelector('.l__sub').style.backgroundColor = '${topBarColor}';" +
                        "       document.querySelector('.l__sub').querySelector('h1').style.color = '${topBarTextColor}';" +
                        "       document.querySelector('h1').style.fontSize = '${topBarTextSize}px';" +
                        if (address1.isNotEmpty() && address2.isNotEmpty()) {
                            addressTagAddScript
                        } else {
                            ""
                        } +
                        "   });" +
                        "});" +
                        "observer.observe(target, { childList: true, subtree: true });" +
                        "})();"
                view?.let {
                    it.evaluateJavascript(loadFontScript, null)
                    it.evaluateJavascript(customTopBarScript, null)
                }
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
                groupViewModel.createRoomScreenPlaceName.postValue(placeName.split(']').last())
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
                groupViewModel.sendMessageWithReservation(groupUId = args.groupUId)
                findNavController().navigateUp()
            }
        }
    }
}