package com.golfzon.group

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.golfzon.core_ui.databinding.ItemGroupBinding
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.domain.model.Group

class GroupAdapter : ListAdapter<Group, GroupAdapter.GroupViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Group>() {
            override fun areItemsTheSame(
                oldItem: Group,
                newItem: Group
            ) =
                oldItem.originalTeamsInfo == newItem.originalTeamsInfo

            override fun areContentsTheSame(
                oldItem: Group,
                newItem: Group
            ): Boolean =
                oldItem == newItem
        }
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, group: Group)
    }

    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GroupViewHolder =
        GroupViewHolder(
            ItemGroupBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: GroupViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun submitList(list: MutableList<Group>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    inner class GroupViewHolder(private val binding: ItemGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(group: Group) {
            with(binding) {
                root.setOnDebounceClickListener {
                    listener?.onItemClick(it, group)
                }

                if (group.headCount != 0) {
                    this.group = group
                    this.screenRoomInfo = group.screenRoomInfo
                    if (group.screenRoomInfo?.screenRoomPlaceName?.isNotEmpty() == true)
                        binding.tvGroupLocation.text = group.screenRoomInfo.screenRoomPlaceName
                }
                if (group.originalTeamsInfo.size == 2) {
                    firstTeam = group.originalTeamsInfo[0]
                    secondTeam = group.originalTeamsInfo[1]
                }
            }
        }
    }
}