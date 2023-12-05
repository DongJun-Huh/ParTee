package com.golfzon.matching

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.matching.databinding.FragmentMatchingBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MatchingFragment : Fragment() {
    private var binding by autoCleared<FragmentMatchingBinding>()
    private val matchingViewModel by activityViewModels<MatchingViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentMatchingBinding.inflate(inflater, container, false)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        tempUserImageClickListener()
        tempCandidateReactionClickListener()
    }

    private fun setDataBindingVariables() {
        binding.apply {
            lifecycleOwner = viewLifecycleOwner
            vm = matchingViewModel
        }
    }

    private fun tempUserImageClickListener() {
        // TODO 기능 구현 후 삭제 메소드
        binding.ivMatchingCandidateUser1.setOnDebounceClickListener {
            binding.layoutMatchingCandidateTeam.visibility = View.GONE
            binding.layoutMatchingCandidateUser.visibility = View.VISIBLE
        }
        binding.ivMatchingCandidateUser2.setOnDebounceClickListener {
            binding.layoutMatchingCandidateTeam.visibility = View.GONE
            binding.layoutMatchingCandidateUser.visibility = View.VISIBLE
        }
        binding.ivMatchingCandidateUser3.setOnDebounceClickListener {
            binding.layoutMatchingCandidateTeam.visibility = View.GONE
            binding.layoutMatchingCandidateUser.visibility = View.VISIBLE
        }
    }

    private fun tempCandidateReactionClickListener() {
        // TODO 기능 구현 후 삭제 메소드
        binding.btnMatchingReacionsLike.setOnDebounceClickListener {
            findNavController().navigate(MatchingFragmentDirections.actionMatchingFragmentToMatchingSuccessFragment())
        }
    }
}
