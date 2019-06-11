package com.cyrilw.cyriltools.ui

import android.content.ComponentName
import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.view.MenuItem
import androidx.preference.Preference
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.SwitchPreference
import com.cyrilw.cyriltools.Constant
import com.cyrilw.cyriltools.R
import com.cyrilw.cyriltools.service.IndicatorService


class MiscellaneousFragment : PreferenceFragmentCompat() {

    private var mActivity: MainActivity? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setHasOptionsMenu(true)
        mActivity = activity as? MainActivity
    }

    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.miscellaneous, rootKey)
    }

    override fun onPreferenceTreeClick(preference: Preference?): Boolean {
        when (preference?.key) {
            Constant.INDICATOR_SWITCH_KEY -> {
                if ((preference as SwitchPreference).isChecked) {
                    mActivity?.startService(Intent(mActivity, IndicatorService::class.java))
                } else {
                    mActivity?.stopService(Intent(mActivity, IndicatorService::class.java))
                }
            }
            Constant.NOTIFICATION_EXCLUSIONS_KEY -> mActivity?.switchTo(
                Constant.APP_LIST_FRAGMENT_TAG,
                1
            )
        }
        return super.onPreferenceTreeClick(preference)
    }

    override fun onResume() {
        super.onResume()
        if (isNotificationListenerEnabled()) {
            setActiveDisplayPreference("Enable", true)
        } else {
            setActiveDisplayPreference("Disable", false)
        }
        setToolbar(Constant.FEATURE_MISCELLANEOUS, true)
    }

    override fun onDetach() {
        super.onDetach()
        mActivity = null
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setToolbar(Constant.FEATURE_MISCELLANEOUS, true)
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            android.R.id.home -> mActivity?.switchBack()
        }
        return true
    }

    private fun setToolbar(title: String, isShowBack: Boolean) {
        mActivity?.supportActionBar?.run {
            this.title = title
            setDisplayHomeAsUpEnabled(isShowBack)
        }
    }

    private fun setActiveDisplayPreference(summary: String, isEnabled: Boolean) {
        findPreference<Preference>(Constant.NLS_SWITCH_KEY)?.summary = summary
        findPreference<Preference>(Constant.NOTIFICATION_EXCLUSIONS_KEY)?.isEnabled = isEnabled
    }

    private fun isNotificationListenerEnabled(): Boolean {
        val name = mActivity?.packageName
        val listeners =
            Settings.Secure.getString(mActivity?.contentResolver, "enabled_notification_listeners")
        val components = listeners?.split(":")
        components?.let {
            for (component: String in it) {
                val componentName = ComponentName.unflattenFromString(component)
                if (componentName?.packageName.equals(name)) {
                    return true
                }
            }
        }
        return false
    }
}
