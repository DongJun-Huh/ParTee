package com.golfzon.team

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.RadioButton
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.golfzon.core_ui.DialogUtil
import com.golfzon.core_ui.DialogUtil.setDialogRadius
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.core_ui.extension.toast
import com.golfzon.team.databinding.FragmentTeamDaysTimesSetBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamDaysTimesSetFragment : DialogFragment() {
    private var binding by autoCleared<FragmentTeamDaysTimesSetBinding>() { }
    private val teamViewModel by activityViewModels<TeamViewModel>()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeamDaysTimesSetBinding.inflate(inflater, container, false)
        setDialogRadius(dialog!!)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeDaysAndTimes()
        setDaysTimesSaveListener()
    }

    override fun onResume() {
        super.onResume()
        DialogUtil.resizeDialogFragment(requireContext(), dialog!!)
    }

    private fun setDataBindingVariables() {
        binding.apply {
            vm = teamViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun initializeDaysAndTimes() {
        teamViewModel.newTeam.value?.let { newTeam ->
            newTeam.searchingDays?.let { curSearchingDays ->
                when (curSearchingDays) {
                    binding.rbTeamDaysWeekdays.text ->
                        binding.rbTeamDaysWeekdays.isChecked = true

                    binding.rbTeamDaysWeekend.text ->
                        binding.rbTeamDaysWeekend.isChecked = true
                }
            }

            newTeam.searchingTimes?.let { curSearchingTimes ->
                when (curSearchingTimes) {
                    binding.rbTeamTimesMorning.text ->
                        binding.rbTeamTimesMorning.isChecked = true

                    binding.rbTeamTimesAfternoon.text ->
                        binding.rbTeamTimesAfternoon.isChecked = true

                    binding.rbTeamTimesNight.text ->
                        binding.rbTeamTimesNight.isChecked = true

                    binding.rbTeamTimesDawn.text ->
                        binding.rbTeamTimesDawn.isChecked = true
                }
            }
        }
    }

    private fun setDaysTimesSaveListener() {
        with(binding) {
            btnTeamDaysTimesSave.setOnDebounceClickListener {
                if (binding.rgTeamDays.checkedRadioButtonId == -1) {
                    this@TeamDaysTimesSetFragment.toast(
                        message = getString(R.string.team_days_times_change_fail_not_select_days),
                        isError = true
                    )
                } else if (binding.rgTeamTimes.checkedRadioButtonId == -1) {
                    this@TeamDaysTimesSetFragment.toast(
                        message = getString(R.string.team_days_times_change_fail_not_select_time),
                        isError = true
                    )
                } else {
                    teamViewModel.setDaysAndTimes(
                        days = dialog!!.findViewById<RadioButton>(binding.rgTeamDays.checkedRadioButtonId).text.toString(),
                        times = dialog!!.findViewById<RadioButton>(binding.rgTeamTimes.checkedRadioButtonId).text.toString()
                    )
                    this@TeamDaysTimesSetFragment
                        .toast(message = getString(R.string.team_days_times_change_success_toast_message))
                    findNavController().navigateUp()
                }
            }
        }
    }
}