package com.cyrilw.cyriltools.adapter

import android.content.Context
import android.content.pm.ApplicationInfo
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.util.SparseBooleanArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.Switch
import android.widget.TextView
import androidx.core.util.forEach
import com.cyrilw.cyriltools.R
import com.cyrilw.cyriltools.base.BaseAdapter

class AppListAdapter(context: Context, data: ArrayList<PackageInfo>, itemLayoutId: Int) :
    BaseAdapter<PackageInfo>(context, data, itemLayoutId) {

    companion object {
        private const val TYPE_HEADER = 0
        private const val TYPE_NORMAL = 1
    }

    private val mManager: PackageManager = mContext.packageManager
    private val mSelected: SparseBooleanArray = SparseBooleanArray()

    private val mDataBackup: ArrayList<PackageInfo> by lazy { ArrayList<PackageInfo>() }

    private var isAllSelected: Boolean = false

    init {
        for (i in 0 until itemCount) {
            mSelected.put(i, false)
        }
        for (info in mData) {
            if (info.applicationInfo.flags and ApplicationInfo.FLAG_SYSTEM == 0) {
                mDataBackup.add(info)
            }
        }
    }

    override fun bindingViewHolder(holder: ViewHolder, position: Int) {
        val realPosition = position - 1
        holder.getView<ImageView>(R.id.app_icon)
            ?.setImageDrawable(mData[realPosition].applicationInfo.loadIcon(mManager))
        holder.getView<TextView>(R.id.app_name)?.text =
            mData[realPosition].applicationInfo.loadLabel(mManager)
        holder.getView<TextView>(R.id.app_pkg)?.text = mData[realPosition].packageName
        holder.itemView.isSelected = mSelected[realPosition]
    }

    override fun getItemViewType(position: Int): Int {
        return if (position == 0) TYPE_HEADER else TYPE_NORMAL
    }

    override fun creatingViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return if (viewType == TYPE_HEADER) {
            val headerView =
                LayoutInflater.from(mContext).inflate(R.layout.app_list_header, parent, false)
            val viewHolder = ViewHolder(headerView)
            viewHolder.getView<Switch>(R.id.show_system_apps)?.setOnCheckedChangeListener { _, _ ->
                swapDataSet()
                notifyDataSetChanged()
            }
            ViewHolder(headerView)
        } else {
            super.creatingViewHolder(parent, viewType)
        }
    }

    override fun onItemViewClick(itemView: View, position: Int) {
        if (position > 0) {
            mSelected.put(position - 1, !mSelected[position - 1])
            notifyItemChanged(position)
        }
    }

    fun selectAll() {
        isAllSelected = !isAllSelected
        for (i in 0 until itemCount) {
            mSelected.put(i, isAllSelected)
        }
        notifyDataSetChanged()
    }

    fun commit(): HashSet<PackageInfo> {
        val selected = HashSet<PackageInfo>()
        mSelected.forEach { key, value ->
            if (value) {
                selected.add(mData[key])
            }
        }
        return selected
    }

    fun reset() {
        mSelected.clear()
        for (i in 0 until itemCount) {
            mSelected.put(i, false)
        }
        isAllSelected = false
    }

    private fun swapDataSet() {
        val temp = ArrayList(mData)
        with(mData) {
            clear()
            addAll(mDataBackup)
        }
        with(mDataBackup) {
            clear()
            addAll(temp)
        }

        mSelected.clear()
        for (i in 0 until mData.size) {
            mSelected.put(i, false)
        }
    }

}