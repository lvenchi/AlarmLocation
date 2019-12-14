package com.example.alarmlocation.models

import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmlocation.R
import com.example.alarmlocation.databinding.AlarmLayoutBinding


class AlarmViewHolder(val alarmBinding: AlarmLayoutBinding) : RecyclerView.ViewHolder(alarmBinding.root) {

    fun bind(alarm: Alarm, clickListener: ItemClickListener) {
        val entry = itemView.findViewById<Button>(R.id.switch1)
        entry.setOnClickListener {
            clickListener.onItemClicked(alarm)
        }
    }

    interface ItemClickListener {
        fun onItemClicked(alarm: Alarm)
    }
}