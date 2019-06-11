package com.cyrilw.cyriltools.base

import android.content.Context
import android.util.SparseArray
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView

abstract class BaseAdapter<T>(context: Context, data: ArrayList<T>, itemLayoutId: Int) :
    RecyclerView.Adapter<BaseAdapter.ViewHolder>() {

    protected val mContext: Context = context
    protected val mData: ArrayList<T> = data

    private val mItemLayoutId: Int = itemLayoutId

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return creatingViewHolder(parent, viewType)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        bindingViewHolder(holder, position)
    }

    override fun getItemCount(): Int = mData.size

    protected open fun onItemViewClick(itemView: View, position: Int) {
    }

    protected open fun creatingViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(mContext).inflate(mItemLayoutId, parent, false)
        val viewHolder = ViewHolder(itemView)
        viewHolder.itemView.setOnClickListener {
            val position = viewHolder.adapterPosition
            if (position != RecyclerView.NO_POSITION) {
                onItemViewClick(itemView, position)
            }
        }
        return viewHolder
    }

    abstract fun bindingViewHolder(holder: ViewHolder, position: Int)

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

        private val mView: SparseArray<View> = SparseArray()

        @Suppress("UNCHECKED_CAST")
        fun <T : View> getView(viewId: Int): T? {
            var view = mView[viewId]
            if (view == null) {
                view = itemView.findViewById(viewId)
                mView.put(viewId, view)
            }
            return view as T?
        }

    }

}