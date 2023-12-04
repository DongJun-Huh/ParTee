package com.golfzon.team

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.golfzon.domain.model.User
import com.golfzon.team.databinding.ItemTeamInfoUserBinding

class TeamUserAdapter() :
    ListAdapter<Triple<User, Boolean, String>, TeamUserAdapter.TeamUserViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Triple<User, Boolean, String>>() {
            override fun areItemsTheSame(
                oldItem: Triple<User, Boolean, String>,
                newItem: Triple<User, Boolean, String>
            ) =
                oldItem.first.userUId == newItem.first.userUId

            override fun areContentsTheSame(
                oldItem: Triple<User, Boolean, String>,
                newItem: Triple<User, Boolean, String>
            ): Boolean =
                oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TeamUserViewHolder =
        TeamUserViewHolder(
            ItemTeamInfoUserBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: TeamUserViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun submitList(list: MutableList<Triple<User, Boolean, String>>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    inner class TeamUserViewHolder(private val binding: ItemTeamInfoUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: Triple<User, Boolean, String>) {
            setBindingSetVariable(user)
            Glide.with(binding.ivTeamInfoUser.context)
                .load("https://firebasestorage.googleapis.com/v0/b/partee-1ba05.appspot.com/o/users%2F${user.first.profileImg}?alt=media")
                .into(binding.ivTeamInfoUser)
        }

        private fun setBindingSetVariable(user: Triple<User, Boolean, String>) {
            with(binding) {
                setVariable(BR.user, user.first)
                setVariable(BR.isCurUser, user.second)
                setVariable(BR.isLeader, user.third == user.first.userUId)
                executePendingBindings()
            }
        }
    }
}