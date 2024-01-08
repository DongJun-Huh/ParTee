package com.golfzon.recruit

import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.webkit.WebResourceRequest
import android.webkit.WebResourceResponse
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.bumptech.glide.Glide
import com.bumptech.glide.RequestManager
import com.golfzon.core_ui.adapter.CandidateTeamMemberAdapter
import com.golfzon.core_ui.adapter.itemDecoration.HorizontalMarginItemDecoration
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.dp
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.core_ui.getColorHex
import com.golfzon.core_ui.map.WebViewTouchEventCallback
import com.golfzon.recruit.databinding.FragmentRecruitDetailBinding
import dagger.hilt.android.AndroidEntryPoint
import java.io.IOException
import java.time.LocalDate
import java.time.Period

@AndroidEntryPoint
class RecruitDetailFragment : Fragment() {
    private var binding by autoCleared<FragmentRecruitDetailBinding> { onDestroyBindingView() }
    private val recruitViewModel by activityViewModels<RecruitViewModel>()
    private val args by navArgs<RecruitDetailFragmentArgs>()
    private var recruitDetailMembersAdapter: CandidateTeamMemberAdapter? = null
    private var glideRequestManager: RequestManager? = null

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentRecruitDetailBinding.inflate(inflater, container, false)
        glideRequestManager = Glide.with(this@RecruitDetailFragment)
        setDataBindingVariables()
        getRecruitDetail()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setRecruitDetailMembersAdapter()
        getRecruitDetailMembers()
        observeRecruitMembers()
        setParticipateClickListener()
        observeParticipateSuccess()
    }

    private fun onDestroyBindingView() {
        recruitDetailMembersAdapter = null
        glideRequestManager = null
    }

    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = recruitViewModel
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
                        with(binding) {
                            this.recruitDetail = recruitDetail
                            if (LocalDate.now()
                                    .isBefore(recruitDetail.recruitEndDateTime.toLocalDate()) ||
                                recruitDetail.searchingHeadCount - recruitDetail.headCount <= 0
                            ) {
                                tvRecruitDetailEndDateDDay.text = "D-" + Period.between(
                                    LocalDate.now(),
                                    recruitDetail.recruitEndDateTime.toLocalDate()
                                ).days
                                btnRecruitDetailParticipate.isEnabled = true
                            } else {
                                tvRecruitDetailEndDateDDay.text =
                                    getString(R.string.participate_end)
                                btnRecruitDetailParticipate.isEnabled = false
                            }
                        }
                        getRecruitMembers(recruitDetail.membersUId)
                        setMap(this.recruitPlaceUId, this.recruitPlaceRoadAddress)
                    }
                }
            }
        }
    }

    private fun getRecruitMembers(membersUId: List<String>) {
        recruitViewModel.getRecruitMembersInfo(membersUId)
    }

    private fun setRecruitDetailMembersAdapter() {
        recruitDetailMembersAdapter =
            CandidateTeamMemberAdapter(
                itemHeight = 52.dp,
                isCircleImage = true,
                requestManager = glideRequestManager!!
            )
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

    private fun setMap(placeUId: String, roadAddress: String) {
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
                val topBarTextColor =
                    ContextCompat.getColor(
                        requireContext(),
                        com.golfzon.core_ui.R.color.white
                    ).getColorHex
                val topBarTextSize = 14

                val (address1, address2) = roadAddress.split(" ").take(2)
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

        with(binding.recruitDetailPlaceMap) {
            isEnabled = false
            setBackgroundColor(0)
            webViewClient = golfzonWebViewClient
            settings.javaScriptEnabled = true
            loadUrl("https://m.golfzon.com/booking/#/booking/map/view/${placeUId}")
        }

        binding.cardviewRecruitDetailPlaceMap.setTouchEventCallback(object :
            WebViewTouchEventCallback {
            override fun onEvent(event: MotionEvent?) {
                findNavController().navigate(
                    RecruitDetailFragmentDirections.actionRecruitDetailFragmentToMapFinderFragment(
                        recruitPlaceUId = placeUId
                    )
                )
            }
        })
    }
}