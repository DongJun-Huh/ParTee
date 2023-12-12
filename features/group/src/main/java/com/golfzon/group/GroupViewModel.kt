package com.golfzon.group

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.golfzon.core_ui.Event
import com.golfzon.core_ui.ListLiveData
import com.golfzon.domain.model.Group
import com.golfzon.domain.usecase.group.GetGroupDetailUseCase
import com.golfzon.domain.usecase.group.GetGroupsUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class GroupViewModel @Inject constructor(
    private val getGroupsUseCase: GetGroupsUseCase,
    private val getGroupDetailUseCase: GetGroupDetailUseCase
): ViewModel() {
    private val _groups = ListLiveData<Group>()
    val groups : ListLiveData<Group> get() = _groups

    private val _curGroupDetail = MutableLiveData<Event<Group>>()
    val curGroupDetail : LiveData<Event<Group>> get() = _curGroupDetail

    fun getGroups() = viewModelScope.launch {
        getGroupsUseCase()?.let {
            _groups.replaceAll(it, true)
        }
    }

    fun getGroupDetail(groupUId: String) = viewModelScope.launch {
        getGroupDetailUseCase(groupUId).let { groupDetail ->
            _curGroupDetail.postValue(Event(groupDetail))
        }
    }
}