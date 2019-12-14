package com.example.alarmlocation


import android.content.Context
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.alarmlocation.databinding.AlarmLayoutBinding
import com.example.alarmlocation.databinding.FragmentHomeBinding
import com.example.alarmlocation.models.Alarm
import com.example.alarmlocation.models.AlarmViewHolder
import com.example.alarmlocation.viewmodels.HomeViewModel

import com.google.android.material.floatingactionbutton.FloatingActionButton

class Home : Fragment(), AlarmViewHolder.ItemClickListener{

    var homeViewModel: HomeViewModel? = null
    var adapter: AlarmAdapter? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        adapter = AlarmAdapter(activity!!, this)
        super.onCreate(savedInstanceState)
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        val homeFragmentBinding: FragmentHomeBinding =
            DataBindingUtil.inflate(inflater, R.layout.fragment_home, container, false)
        homeFragmentBinding.viewmodel = homeViewModel

        return homeFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        view.findViewById<FloatingActionButton>(R.id.fab).setOnClickListener {
            val dir = HomeDirections.actionHome2ToEditAlarm()
            Navigation.findNavController(activity!!, R.id.nav_host_fragment).navigate(dir)
        }
        val rview = view.findViewById<RecyclerView>(R.id.alarm_list).also {
            it?.layoutManager = LinearLayoutManager(context)
            it?.setHasFixedSize(false)
            it?.adapter = adapter
        }

        homeViewModel?.getAlarms()?.observe(this, Observer<List<Alarm>> {
            (rview.adapter as AlarmAdapter).setData(it)
        })

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onItemClicked(alarm: Alarm) {
        homeViewModel?.updateAlarm(alarm)

    }

    class AlarmAdapter(context: Context, private val itemClickListener: AlarmViewHolder.ItemClickListener)
        : RecyclerView.Adapter<AlarmViewHolder>() {

        private var alarmlist: List<Alarm>? = null
        private val layoutInflater = LayoutInflater.from(context)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {

            val alarmLayoutBinding: AlarmLayoutBinding = DataBindingUtil.inflate(
                layoutInflater, R.layout.alarm_layout,
                parent,
                false)

            return AlarmViewHolder(
                alarmLayoutBinding
            )
        }

        override fun getItemCount(): Int {
            return alarmlist?.size ?: 0
        }

        override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
            //holder.itemView.findViewById<TextView>(R.id.alarm_title).text =
                //alarmlist!![position].key
            val al = alarmlist!![position]
            holder.alarmBinding.alarm = al
            holder.bind(al, itemClickListener )

        }

        fun setData(newData: List<Alarm>) {

            alarmlist = newData
            notifyDataSetChanged()
        }

    }

}
