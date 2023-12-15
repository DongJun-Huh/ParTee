package com.golfzon.core_ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.golfzon.core_ui.ImageUploadUtil
import com.golfzon.core_ui.ImageUploadUtil.loadImageFromFirebaseStorage
import com.golfzon.core_ui.databinding.ItemTeamInfoUserBinding
import com.golfzon.domain.model.User

class TeamUserAdapter(private val isWhiteTheme: Boolean = false) :
    ListAdapter<Triple<User, Boolean, String>, TeamUserAdapter.TeamUserViewHolder>(diffCallback) {
    // User: 표시할 유저 정보, Boolean: 현재 유저가 본인인지 유무, String: 현재 팀장 Id
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
            binding.ivTeamInfoUser.loadImageFromFirebaseStorage(
                imageUId = user.first.profileImg ?: "",
                imageType = ImageUploadUtil.ImageType.USER
            )
        }

        private fun setBindingSetVariable(user: Triple<User, Boolean, String>) {
            with(binding) {
                this.user = user.first
                this.isCurUser = user.second
                this.isLeader = user.third == user.first.userUId
                this.isWhiteTheme = this@TeamUserAdapter.isWhiteTheme
                executePendingBindings()
            }
        }
    }
}