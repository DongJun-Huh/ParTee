package com.golfzon.team

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.golfzon.domain.model.User
import com.golfzon.team.databinding.ItemTeamInfoUserBinding

class TeamUserAdapter() : ListAdapter<User, TeamUserAdapter.TeamUserViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User) =
                oldItem.userUId == newItem.userUId

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
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

    override fun submitList(list: MutableList<User>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    inner class TeamUserViewHolder(private val binding: ItemTeamInfoUserBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            setBindingSetVariable(user)
//            Glide.with(binding.ivTeamInfoUser.context)
//                .load(user.profileImg)
//                .into(binding.ivTeamInfoUser)
        }

        private fun setBindingSetVariable(user: User) {
            with(binding) {
                setVariable(BR.user, user)
                executePendingBindings()
            }
        }
    }
}