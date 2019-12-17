package com.example.alarmlocation.models

import android.view.View
import android.widget.Button
import android.widget.CheckBox
import androidx.lifecycle.LifecycleOwner
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmlocation.R
import com.example.alarmlocation.databinding.AlarmLayoutBinding


class AlarmViewHolder(val alarmBinding: AlarmLayoutBinding,
                      private val lifecycleOwner: LifecycleOwner)
    : RecyclerView.ViewHolder(alarmBinding.root) {

    fun bind(alarm: Alarm, clickListener: ItemClickListener) {
        val entry = itemView.findViewById<Button>(R.id.switch1)

        entry.setOnClickListener {
            clickListener.onItemClicked(alarm)
        }

        itemView.findViewById<CheckBox>(R.id.radio_button).also { check ->
            check.setOnClickListener {
                clickListener.onCheckBoxClick(alarm)
            }
            alarmBinding.viewmodel?.isListEdit?.observe(lifecycleOwner, androidx.lifecycle.Observer {
                if(it == View.GONE){
                    check.isChecked = false
                }
            })

            itemView.setOnClickListener {
                if(alarmBinding.viewmodel?.isListEdit?.value == View.VISIBLE) {
                    check.isChecked = check.isChecked.not()
                    clickListener.onCheckBoxClick(alarm)
                } else{
                    clickListener.viewAlarmDetails(alarm)
                }
            }

            itemView.setOnLongClickListener {
                check.isChecked = true
                clickListener.onCheckBoxClick(alarm)
                clickListener.onItemLongClicked(alarm)
                true
            }

        }
    }

    interface ItemClickListener {
        fun onItemClicked(alarm: Alarm)
        fun onItemLongClicked(alarm: Alarm)
        fun onCheckBoxClick(alarm: Alarm)
        fun viewAlarmDetails(alarm: Alarm)
    }
}