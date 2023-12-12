package com.golfzon.group

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfzon.core_ui.ListLiveData
import com.golfzon.domain.model.Group
import com.golfzon.domain.usecase.group.GetGroupsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val getGroupsUseCase: GetGroupsUseCase
): ViewModel() {
    private val _groups = ListLiveData<Group>()
    val groups : ListLiveData<Group> get() = _groups

    fun getGroups() = viewModelScope.launch {
        getGroupsUseCase()?.let {
            _groups.replaceAll(it, true)
        }
    }
}