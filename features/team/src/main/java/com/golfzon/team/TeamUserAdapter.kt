package com.golfzon.team

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.golfzon.domain.model.User
import com.golfzon.team.databinding.ItemTeamInfoUserBinding

class TeamUserAdapter() : ListAdapter<Pair<User, Boolean>, TeamUserAdapter.TeamUserViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<Pair<User, Boolean>>() {
            override fun areItemsTheSame(oldItem: Pair<User, Boolean>, newItem: Pair<User, Boolean>) =
                oldItem.first.userUId == newItem.first.userUId

            override fun areContentsTheSame(oldItem: Pair<User, Boolean>, newItem: Pair<User, Boolean>): Boolean =
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

    override fun submitList(list: MutableList<Pair<User, Boolean>>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    inner class TeamUserViewHolder(private val binding: ItemTeamInfoUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: Pair<User, Boolean>) {
            setBindingSetVariable(user)
            Glide.with(binding.ivTeamInfoUser.context)
                .load("https://firebasestorage.googleapis.com/v0/b/partee-1ba05.appspot.com/o/users%2F${user.first.profileImg}?alt=media")
                .into(binding.ivTeamInfoUser)
        }

        private fun setBindingSetVariable(user: Pair<User, Boolean>) {
            with(binding) {
                setVariable(BR.user, user.first)
                setVariable(BR.isCurUser, user.second)
                executePendingBindings()
            }
        }
    }
}