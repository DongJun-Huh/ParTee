package com.golfzon.team

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import androidx.core.view.children
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.golfzon.core_ui.DialogUtil.resizeDialogFragment
import com.golfzon.core_ui.DialogUtil.setDialogRadius
import com.golfzon.core_ui.autoCleared
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.core_ui.extension.toast
import com.golfzon.team.databinding.FragmentTeamLocationSetBinding
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class TeamLocationSetFragment : DialogFragment() {
    private var binding: FragmentTeamLocationSetBinding by autoCleared<FragmentTeamLocationSetBinding>()
    private val teamViewModel by activityViewModels<TeamViewModel>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentTeamLocationSetBinding.inflate(inflater, container, false)
        setDialogRadius(dialog!!)
        setDataBindingVariables()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initializeSearchingPlaces()
        setLocationCheckListener()
        setLocationSaveListener()
    }

    override fun onResume() {
        super.onResume()
        resizeDialogFragment(requireContext(), dialog!!)
    }

    private fun setDataBindingVariables() {
        binding.apply {
            vm = teamViewModel
            lifecycleOwner = viewLifecycleOwner
        }
    }

    private fun initializeSearchingPlaces() {
        teamViewModel.newTeam.value?.searchingLocations?.let { curSearchingPlaces ->
            for (placeName in curSearchingPlaces) {
                binding.layoutTeamLocationSetPlaces.children.forEach { curCheckBox ->
                    if ((curCheckBox as CheckBox).text == placeName) curCheckBox.isChecked = true
                }
            }
        }
    }

    private fun setLocationSaveListener() {
        with(binding) {
            btnTeamLocationSetSave.setOnDebounceClickListener {
                val curSearchingPlaces = mutableListOf<String>()

                layoutTeamLocationSetPlaces.children.forEach { curCheckBox ->
                    if ((curCheckBox as CheckBox).isChecked) curSearchingPlaces.add(curCheckBox.text.toString())
                }

                if (curSearchingPlaces.isEmpty()) {
                    this@TeamLocationSetFragment.toast(
                        message = getString(R.string.team_location_change_fail_not_select),
                        isError = true
                    )
                } else if (curSearchingPlaces.size > 8) {
                    this@TeamLocationSetFragment.toast(
                        message = getString(R.string.team_location_change_fail_limit),
                        isError = true
                    )
                } else {
                    teamViewModel.setLocation(curSearchingPlaces)
                    this@TeamLocationSetFragment
                        .toast(message = getString(R.string.team_location_change_success_toast_message))
                    findNavController().navigateUp()
                }
            }
        }
    }

    private fun setLocationCheckListener() {
        with(binding) {
            cbTeamLocationSetNationwide.setOnCheckedChangeListener { _, isChecked ->
                layoutTeamLocationSetPlaces.children.forEach { curCheckBox ->
                    if (isChecked && curCheckBox != cbTeamLocationSetNationwide)
                        (curCheckBox as CheckBox).isChecked = false
                }
            }

            layoutTeamLocationSetPlaces.children.forEach { curCheckBox ->
                if (curCheckBox != cbTeamLocationSetNationwide) {
                    (curCheckBox as CheckBox).setOnCheckedChangeListener { curView, isChecked ->
                        if (isChecked) cbTeamLocationSetNationwide.isChecked = false
                    }
                }
            }
        }
    }
}