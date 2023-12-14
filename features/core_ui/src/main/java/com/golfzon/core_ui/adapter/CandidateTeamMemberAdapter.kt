package com.golfzon.core_ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.golfzon.core_ui.databinding.ItemCandidateTeamMemberBinding
import com.golfzon.core_ui.dp
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.domain.model.User
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel

class CandidateTeamMemberAdapter(
    private val itemHeight: Int = 100.dp,
    private val isCircleImage: Boolean = false
) :
    ListAdapter<User, CandidateTeamMemberAdapter.CandidateTeamMemberViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(
                oldItem: User,
                newItem: User
            ) =
                oldItem.userUId == newItem.userUId

            override fun areContentsTheSame(
                oldItem: User,
                newItem: User
            ): Boolean =
                oldItem == newItem
        }
    }

    interface OnItemClickListener {
        fun onItemClick(imageView: ImageView, user: User)
    }

    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): CandidateTeamMemberViewHolder =
        CandidateTeamMemberViewHolder(
            ItemCandidateTeamMemberBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: CandidateTeamMemberViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun submitList(list: MutableList<User>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    inner class CandidateTeamMemberViewHolder(private val binding: ItemCandidateTeamMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            with(binding.ivCandidateTeamMember) {
                layoutParams.width = itemHeight
                Glide.with(this.context)
                    .load("https://firebasestorage.googleapis.com/v0/b/partee-1ba05.appspot.com/o/users%2F${user.profileImg}?alt=media")
                    .into(this)
                if (isCircleImage) {
                    shapeAppearanceModel =
                        ShapeAppearanceModel.builder()
                            .setAllCorners(CornerFamily.ROUNDED, itemHeight / 2f)
                            .build()
                }
                setOnDebounceClickListener {
                    listener?.onItemClick(it as ImageView, user)
                }
            }
        }
    }
}