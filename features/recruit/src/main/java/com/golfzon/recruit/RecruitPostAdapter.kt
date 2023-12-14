package com.golfzon.recruit

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.golfzon.core_ui.adapter.CandidateTeamMemberAdapter
import com.golfzon.core_ui.databinding.ItemRecruitPostBinding
import com.golfzon.core_ui.dp
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.domain.model.Recruit
import com.golfzon.domain.model.User

class RecruitPostAdapter :
    ListAdapter<Pair<Recruit, List<User>>, RecruitPostAdapter.RecruitPostViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Pair<Recruit, List<User>>>() {
            override fun areItemsTheSame(
                oldItem: Pair<Recruit, List<User>>,
                newItem: Pair<Recruit, List<User>>
            ) =
                oldItem.first.recruitUId == newItem.first.recruitUId

            override fun areContentsTheSame(
                oldItem: Pair<Recruit, List<User>>,
                newItem: Pair<Recruit, List<User>>
            ): Boolean =
                oldItem == newItem
        }
    }

    interface OnItemClickListener {
        fun onItemClick(view: View, recruitInfo: Pair<Recruit, List<User>>)
    }

    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecruitPostViewHolder =
        RecruitPostViewHolder(
            ItemRecruitPostBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: RecruitPostViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun submitList(list: MutableList<Pair<Recruit, List<User>>>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    inner class RecruitPostViewHolder(private val binding: ItemRecruitPostBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(recruitInfo: Pair<Recruit, List<User>>) {
            val recruitMemberAdapter = CandidateTeamMemberAdapter(32.dp, isCircleImage = true)
            with(binding) {
                root.setOnDebounceClickListener {
                    listener?.onItemClick(it, recruitInfo)
                }

                recruitDetail = recruitInfo.first
                rvRecruitPostUsers.adapter = recruitMemberAdapter
                recruitMemberAdapter.submitList(recruitInfo.second.toMutableList())
            }
        }
    }
}