package com.golfzon.team

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.domain.model.User
import com.golfzon.team.databinding.ItemSearchUserResultBinding

class SearchUserResultAdapter() : ListAdapter<User, SearchUserResultAdapter.SearchUserResultViewHolder>(diffCallback) {
    companion object {
        private val diffCallback = object : DiffUtil.ItemCallback<User>() {
            override fun areItemsTheSame(oldItem: User, newItem: User) =
                oldItem.userUId == newItem.userUId

            override fun areContentsTheSame(oldItem: User, newItem: User): Boolean =
                oldItem == newItem
        }
    }
    interface OnItemClickListener {
        fun onItemClick(v: View, user: User)
    }

    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SearchUserResultViewHolder =
        SearchUserResultViewHolder(
            ItemSearchUserResultBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun onBindViewHolder(holder: SearchUserResultViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    override fun submitList(list: MutableList<User>?) {
        super.submitList(list?.let { ArrayList(it) })
    }

    inner class SearchUserResultViewHolder(private val binding: ItemSearchUserResultBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(user: User) {
            setBindingSetVariable(user)
            Glide.with(binding.ivSearchUserResult.context)
                .load("https://firebasestorage.googleapis.com/v0/b/partee-1ba05.appspot.com/o/users%2F${user.profileImg}?alt=media")
                .into(binding.ivSearchUserResult)

            binding.root.setOnDebounceClickListener {
                listener?.onItemClick(it, user)
            }
        }

        private fun setBindingSetVariable(user: User) {
            with(binding) {
                setVariable(BR.user, user)
                executePendingBindings()
            }
        }
    }
}