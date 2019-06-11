package com.cyrilw.cyriltools.adapter

import android.content.Context
import android.view.View
import android.widget.TextView
import com.cyrilw.cyriltools.Constant
import com.cyrilw.cyriltools.R
import com.cyrilw.cyriltools.base.BaseAdapter
import com.cyrilw.cyriltools.ui.MainActivity

class MainListAdapter(context: Context, data: ArrayList<String>, itemLayoutId: Int) :
    BaseAdapter<String>(context, data, itemLayoutId) {

    companion object {
        private val FEATURE_MAP = mapOf(
            Constant.FEATURE_PICKER to Constant.APP_LIST_FRAGMENT_TAG,
            Constant.FEATURE_QR_CODE to Constant.QR_CODE_FRAGMENT_TAG,
            Constant.FEATURE_RULER to Constant.RULER_FRAGMENT_TAG,
            Constant.FEATURE_TEXT_CONVERT to Constant.TEXT_CONVERT_TAG,
            Constant.FEATURE_MISCELLANEOUS to Constant.MISCELLANEOUS_FRAGMENT_TAG
        )
    }

    override fun bindingViewHolder(holder: ViewHolder, position: Int) {
        holder.getView<TextView>(R.id.tool_name)?.text = mData[position]
    }

    override fun onItemViewClick(itemView: View, position: Int) {
        if (mContext is MainActivity) {
            FEATURE_MAP[mData[position]]?.let {
                mContext.switchTo(it, 0)
            }
        }
    }

}