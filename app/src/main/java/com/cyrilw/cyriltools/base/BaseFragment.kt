package com.cyrilw.cyriltools.base

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import com.cyrilw.cyriltools.ui.MainActivity

abstract class BaseFragment : Fragment() {

    protected abstract val layoutRes: Int
    protected abstract val menuRes: Int?

    protected var mActivity: MainActivity? = null

    private var mTitle: String? = null
    private var isShowBack: Boolean = false
    protected var mMode: Int = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mActivity = activity as? MainActivity
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(layoutRes, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView(view)
    }

    override fun onResume() {
        super.onResume()
        setToolbar(mTitle, isShowBack)
    }

    override fun onDetach() {
        super.onDetach()
        mActivity = null
    }

    override fun onCreateOptionsMenu(menu: Menu, inflater: MenuInflater) {
        menuRes?.let { inflater.inflate(it, menu) }
        super.onCreateOptionsMenu(menu, inflater)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> mActivity?.switchBack()
        }
        setMenu(item.itemId)
        return true
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            whenShow()
            setToolbar(mTitle, isShowBack)
        } else {
            mMode = 0
        }
    }

    protected open fun initView(view: View) {
    }

    protected open fun setMenu(itemId: Int) {
    }

    protected open fun whenShow() {
    }

    protected fun <T : View> bindView(id: Int): Lazy<T> = lazy {
        view!!.findViewById<T>(id)
    }

    protected fun setToolbarTitle(title: String) {
        mTitle = title
    }

    protected fun setShowBack() {
        isShowBack = true
    }

    fun setMode(mode: Int) {
        mMode = mode
    }

    private fun setToolbar(title: String?, isShowBack: Boolean) {
        title?.let { mActivity?.supportActionBar?.title = it }
        mActivity?.supportActionBar?.setDisplayHomeAsUpEnabled(isShowBack)
    }

}