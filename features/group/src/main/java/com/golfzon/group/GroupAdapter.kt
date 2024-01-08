package com.golfzon.group

import android.text.Spannable
import android.text.SpannableString
import android.text.style.ForegroundColorSpan
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.golfzon.core_ui.R
import com.golfzon.core_ui.databinding.ItemGroupBinding
import com.golfzon.core_ui.databinding.ItemGroupRecruitBinding
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.domain.model.Group
import com.google.android.material.imageview.ShapeableImageView

class GroupAdapter(private val requestManager: RequestManager) :
    ListAdapter<Group, RecyclerView.ViewHolder>(diffCallback) {
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

        private const val TYPE_MATCHED = 0
        private const val TYPE_RECRUITED = 1
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, group: Group)
        fun onRecruitItemClick(view: View, group: Group)
    }

    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            TYPE_MATCHED -> GroupMatchedViewHolder(
                ItemGroupBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )

            else -> GroupRecruitedViewHolder(
                ItemGroupRecruitBinding.inflate(
                    LayoutInflater.from(parent.context),
                    parent,
                    false
                )
            )
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is GroupMatchedViewHolder)
            holder.bind(getItem(position))
        else if (holder is GroupRecruitedViewHolder)
            holder.bind(getItem(position))
    }

    override fun submitList(list: MutableList<Group>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    override fun getItemViewType(position: Int): Int =
        if (getItem(position).originalTeamsInfo.isEmpty()) TYPE_RECRUITED else TYPE_MATCHED

    inner class GroupMatchedViewHolder(private val binding: ItemGroupBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(group: Group) {
            with(binding) {
                this.requestManager = this@GroupAdapter.requestManager
                root.setOnDebounceClickListener {
                    listener?.onItemClick(it, group)
                }

                if (group.headCount != 0) {
                    setPeopleCount(group.headCount)
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

        private fun setPeopleCount(membersCount: Int) {
            val groupCountToString = membersCount.toString()
            val countSpan = SpannableString(binding.tvGroupMembersCount.context.getString(R.string.people_count, membersCount))
            countSpan.setSpan(
                ForegroundColorSpan(
                    binding.tvGroupMembersCount.context.resources.getColor(
                        R.color.primary_A4EF69,
                        null
                    )
                ),
                countSpan.indexOf(groupCountToString),
                countSpan.indexOf(groupCountToString) + groupCountToString.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.tvGroupMembersCount.text = countSpan
        }
    }

    inner class GroupRecruitedViewHolder(private val binding: ItemGroupRecruitBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(group: Group) {
            with(binding) {
                this.requestManager = requestManager
                root.setOnDebounceClickListener {
                    listener?.onRecruitItemClick(it, group)
                }

                if (group.headCount != 0) {
                    setPeopleCount(group.headCount)
                    this.group = group
                    this.screenRoomInfo = group.screenRoomInfo
                    if (group.screenRoomInfo.screenRoomPlaceName.isNotEmpty())
                        binding.tvGroupLocation.text = group.screenRoomInfo.screenRoomPlaceName

                    group.membersInfo?.mapIndexedNotNull { index, memberInfo ->
                        val curMemberView = binding.layoutGroupRecruitMembers.getChildAt(index)
                        if (curMemberView is LinearLayout) {
                            curMemberView.isVisible = true
                            curMemberView.children.forEach { child ->
                                if (child is ShapeableImageView) {
                                    this@GroupAdapter.requestManager
                                        .load("https://storage.googleapis.com/partee-1ba05.appspot.com/users/resized/120_${memberInfo.profileImg}")
                                        .into(child)
                                }
                            }
                        }
                    }
                }
            }
        }

        private fun setPeopleCount(membersCount: Int) {
            val groupCountToString = membersCount.toString()
            val countSpan = SpannableString(binding.tvGroupRecruitMembersCount.context.getString(R.string.people_count, membersCount))
            countSpan.setSpan(
                ForegroundColorSpan(
                    binding.tvGroupRecruitMembersCount.context.resources.getColor(
                        R.color.primary_A4EF69,
                        null
                    )
                ),
                countSpan.indexOf(groupCountToString),
                countSpan.indexOf(groupCountToString) + groupCountToString.length,
                Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
            )
            binding.tvGroupRecruitMembersCount.text = countSpan
        }
    }
}