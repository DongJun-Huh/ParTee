package com.golfzon.chat

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.recyclerview.widget.RecyclerView
import com.golfzon.core_ui.databinding.ItemChatHeaderBinding
import com.golfzon.core_ui.extension.setOnDebounceClickListener
import com.golfzon.domain.model.Group
import java.time.format.DateTimeFormatter

class ChatHeaderAdapter(private val groupUId: String, private val groupDetail: Group?) :
    RecyclerView.Adapter<ChatHeaderAdapter.ChatHeaderViewHolder>() {

    interface OnItemClickListener {
        fun reservationScreen(groupUId: String, pos: Int)
    }

    private var listener: OnItemClickListener? = null
    fun setOnItemClickListener(listener: OnItemClickListener) {
        this.listener = listener
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatHeaderViewHolder =
        ChatHeaderViewHolder(
            ItemChatHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
        )

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: ChatHeaderViewHolder, position: Int) {
        holder.bind(groupUId, position)
    }

    inner class ChatHeaderViewHolder(private val binding: ItemChatHeaderBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(groupUId: String, position: Int) {
            binding.btnChatHeaderReservation.setOnDebounceClickListener {
                listener?.reservationScreen(groupUId, position)
            }
            groupDetail?.let {
                with(binding) {
                    layoutChatHeaderNavigateReservation.isVisible = it.screenRoomInfo.screenRoomPlaceName.isEmpty()
                    layoutChatHeaderReservationInfo.isVisible = it.screenRoomInfo.screenRoomPlaceName.isNotEmpty()
                    tvChatHeaderReservationLocation.text = it.screenRoomInfo.screenRoomPlaceName
                    tvChatHeaderReservationTime.text =
                        it.screenRoomInfo.screenRoomDateTime.format(DateTimeFormatter.ofPattern("MM월 dd일 EEEE HH:mm"))
                }
            }
        }
    }
}