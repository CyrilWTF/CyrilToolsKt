package com.cyrilw.cyriltools.ui

import android.content.pm.PackageInfo
import android.preference.PreferenceManager
import android.view.View
import android.widget.ProgressBar
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.cyrilw.cyriltools.Constant
import com.cyrilw.cyriltools.R
import com.cyrilw.cyriltools.adapter.AppListAdapter
import com.cyrilw.cyriltools.base.BaseFragment
import kotlinx.coroutines.*
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.text.Collator
import java.util.*
import kotlin.Comparator
import kotlin.collections.ArrayList
import kotlin.collections.HashSet
import kotlin.coroutines.CoroutineContext

class AppListFragment : BaseFragment(), CoroutineScope {

    override val layoutRes: Int
        get() = R.layout.fragment_app_list
    override val menuRes: Int?
        get() = R.menu.menu_app_list

    private val mJob = SupervisorJob()
    override val coroutineContext: CoroutineContext
        get() = Dispatchers.Default + mJob

    private val mAdapter: AppListAdapter by lazy {
        AppListAdapter(
            mActivity ?: view!!.context,
            mAppList,
            R.layout.app_list_item
        )
    }
    private val mAppList: ArrayList<PackageInfo> = ArrayList()

    override fun onDestroy() {
        super.onDestroy()
        coroutineContext.cancelChildren()
    }

    override fun initView(view: View) {
        setToolbarTitle(Constant.FEATURE_PICKER)
        setShowBack()

        val context = mActivity ?: view.context
        val packageManager = context.packageManager
        val recyclerView = view.findViewById<RecyclerView>(R.id.app_list)
        launch {
            mAppList.addAll(packageManager.getInstalledPackages(0))
            mAppList.sortWith(Comparator { o1, o2 ->
                val collator = Collator.getInstance(Locale.CHINA)
                val str1 = o1.applicationInfo.loadLabel(packageManager).toString()
                val str2 = o2.applicationInfo.loadLabel(packageManager).toString()
                collator.compare(str1, str2)
            })
            launch(Dispatchers.Main) {
                val progressBar = view.findViewById<ProgressBar>(R.id.progress_bar)
                progressBar.visibility = View.GONE
                recyclerView.adapter = mAdapter
            }
        }
        with(recyclerView) {
            layoutManager = LinearLayoutManager(context)
            addItemDecoration(DividerItemDecoration(context, RecyclerView.VERTICAL))
        }
    }

    override fun setMenu(itemId: Int) {
        when (itemId) {
            R.id.menu_select_all -> mAdapter.selectAll()
            R.id.menu_commit -> {
                when (mMode) {
                    1 -> {
                        mActivity?.let {
                            val editor = PreferenceManager.getDefaultSharedPreferences(it).edit()
                            val name = HashSet<String>()
                            for (info in mAdapter.commit()) {
                                name.add(info.packageName)
                            }
                            editor.putStringSet(Constant.NOTIFICATION_EXCLUSIONS_KEY, name).apply()
                            it.switchBack()
                        }
                    }
                    0 -> {
                        val dir = mActivity?.getExternalFilesDir("APKBackup")
                        try {
                            launch {
                                for (info in mAdapter.commit()) {
                                    val path = info.applicationInfo.sourceDir
                                    val name = info.packageName
                                    val source = File(path)

                                    if (source.exists()) {
                                        val inputStream = FileInputStream(source)
                                        val outputStream = FileOutputStream(File("$dir/$name.apk"))
                                        val buffer = ByteArray(1024)
                                        var byteRead: Int
                                        do {
                                            byteRead = inputStream.read(buffer)
                                            if (byteRead == -1) {
                                                break
                                            }
                                            outputStream.write(buffer, 0, byteRead)
                                        } while (true)

                                        with(outputStream) {
                                            flush()
                                            close()
                                        }
                                        inputStream.close()
                                    }
                                }
                                launch(Dispatchers.Main) {
                                    mActivity?.showSnackBar("Task complete.")
                                }
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                        }
                    }
                }
            }
        }
    }

    override fun whenShow() {
        mAdapter.reset()
    }

}