package com.example.alarmlocation


import android.app.ActionBar
import android.content.Context
import android.os.Bundle
import android.view.*
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.view.ActionMode
import androidx.fragment.app.Fragment
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.ListFragment
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProviders
import androidx.navigation.Navigation
import androidx.paging.PagedList
import androidx.paging.PagedListAdapter
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
    var recyclerView: RecyclerView? = null
    var selectedItems = ArrayList<Alarm>()
    var actionMode : ActionMode? = null

    val callback = object : ActionMode.Callback{
        override fun onActionItemClicked(mode: ActionMode?, item: MenuItem?): Boolean {
            return when (item?.itemId) {
                R.id.delete_context -> {
                    deleteAlarms()
                    mode?.finish() // Action picked, so close the CAB
                    true
                }
                else -> false
            }
        }

        override fun onCreateActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            val inflater: MenuInflater? = mode?.menuInflater
            inflater?.inflate(R.menu.context_menu, menu)
            actionMode = mode
            return true
        }

        override fun onPrepareActionMode(mode: ActionMode?, menu: Menu?): Boolean {
            return false
        }

        override fun onDestroyActionMode(mode: ActionMode?) {
            (recyclerView?.adapter as AlarmAdapter).disableEditMode()
            actionMode = null
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        homeViewModel = ViewModelProviders.of(this).get(HomeViewModel::class.java)
        adapter = AlarmAdapter(this, this, homeViewModel!!)
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
            actionMode?.finish()
            val dir = HomeDirections.actionHome2ToEditAlarm()
            Navigation.findNavController(activity!!, R.id.nav_host_fragment).navigate(dir)
        }
        recyclerView = view.findViewById<RecyclerView>(R.id.alarm_list).also {
            it?.layoutManager = LinearLayoutManager(context)
            it?.setHasFixedSize(false)
            it?.adapter = adapter
        }

        homeViewModel?.getAlarms()?.observe(this, Observer<PagedList<Alarm>> {
            (recyclerView?.adapter as AlarmAdapter).submitList(it)
        })

        super.onViewCreated(view, savedInstanceState)
    }

    override fun onItemClicked(alarm: Alarm) {
        homeViewModel?.updateAlarm(alarm)
    }

    override fun onItemLongClicked(alarm: Alarm){
        (activity as AppCompatActivity?)?.startSupportActionMode(callback)
        (recyclerView?.adapter as AlarmAdapter).enableEditMode()
    }

    override fun onCheckBoxClick(alarm: Alarm) {
        if(selectedItems.contains(alarm)){
            selectedItems.remove(alarm)
        } else selectedItems.add(alarm)
    }

    override fun viewAlarmDetails(alarm: Alarm) {
        val dir = HomeDirections.actionHome2ToEditExistingAlarm(alarm.key)
        Navigation.findNavController(activity!!, R.id.nav_host_fragment).navigate(dir)
    }

    fun deleteAlarms(){
        for ( alarm in selectedItems ) {
            homeViewModel?.removeAlarms(alarm)
        }
        selectedItems.clear()
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<Alarm>() {
            override fun areItemsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
                return oldItem.key == newItem.key
            }

            override fun areContentsTheSame(oldItem: Alarm, newItem: Alarm): Boolean {
                return oldItem.key == newItem.key && oldItem.isActive == newItem.isActive
            }
        }
    }

    class AlarmAdapter(
        private val parentFragment: Fragment,
        private val itemClickListener: AlarmViewHolder.ItemClickListener,
        private val viewModel: HomeViewModel )
        : PagedListAdapter<Alarm, AlarmViewHolder>(DIFF_CALLBACK) {

        private val layoutInflater = LayoutInflater.from(parentFragment.activity!!)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): AlarmViewHolder {

            val alarmLayoutBinding: AlarmLayoutBinding = DataBindingUtil.inflate(
                layoutInflater, R.layout.alarm_layout,
                parent,
                false)
            alarmLayoutBinding.viewmodel= viewModel
            alarmLayoutBinding.lifecycleOwner = parentFragment
            return AlarmViewHolder(
                alarmLayoutBinding, parentFragment
            )
        }

        override fun onBindViewHolder(holder: AlarmViewHolder, position: Int) {
            val al = getItem(position)
            if(al!= null) {
                holder.alarmBinding.alarm = al
                holder.bind(al, itemClickListener)
            }
        }

        fun enableEditMode(){
            viewModel.isListEdit.postValue(View.VISIBLE)
        }

        fun disableEditMode(){
            viewModel.isListEdit.postValue(View.GONE)
        }
    }
}
