package com.cyrilw.cyriltools.ui

import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyrilw.cyriltools.Constant
import com.cyrilw.cyriltools.R
import com.cyrilw.cyriltools.adapter.MainListAdapter
import com.cyrilw.cyriltools.base.BaseFragment

class MainFragment : BaseFragment() {

    override val layoutRes: Int
        get() = R.layout.fragment_main
    override val menuRes: Int?
        get() = null

    companion object {
        private val FEATURE = arrayListOf(
            Constant.FEATURE_PICKER,
            Constant.FEATURE_QR_CODE,
            Constant.FEATURE_RULER,
            Constant.FEATURE_TEXT_CONVERT,
            Constant.FEATURE_MISCELLANEOUS
        )
    }

    override fun initView(view: View) {
        val context = mActivity ?: view.context
        val recyclerView = view.findViewById<RecyclerView>(R.id.main_list)
        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            adapter = MainListAdapter(context, FEATURE, R.layout.main_list_item)
        }
    }

}
