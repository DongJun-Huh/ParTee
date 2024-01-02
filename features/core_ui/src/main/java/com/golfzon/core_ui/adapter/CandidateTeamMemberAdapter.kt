package com.golfzon.core_ui.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.RequestManager
import com.golfzon.core_ui.databinding.ItemCandidateTeamMemberBinding
import com.golfzon.core_ui.dp
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.domain.model.User
import com.google.android.material.shape.CornerFamily
import com.google.android.material.shape.ShapeAppearanceModel

class CandidateTeamMemberAdapter(
    private val itemHeight: Int = 100.dp,
    private val isCircleImage: Boolean = false,
    private val requestManager: RequestManager
) :
    ListAdapter<User, CandidateTeamMemberAdapter.CandidateTeamMemberViewHolder>(diffCallback) {
    var onRenderCompleted: (() -> Unit)? = null
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

    override fun onCurrentListChanged(
        previousList: MutableList<User>,
        currentList: MutableList<User>
    ) {
        super.onCurrentListChanged(previousList, currentList)
        onRenderCompleted?.invoke()
    }

    inner class CandidateTeamMemberViewHolder(private val binding: ItemCandidateTeamMemberBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            with(binding) {
                this.requestManager = this@CandidateTeamMemberAdapter.requestManager
                this.user = user
            }

            with(binding.ivCandidateTeamMember) {
                layoutParams.width = itemHeight
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