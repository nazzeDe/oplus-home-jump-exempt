package com.nazze.oplusjumpallowlist.config

import android.content.Context
import android.content.SharedPreferences
import java.io.File

/**
 * Settings-process persistence for the caller allowlist.
 *
 * Uses [SharedPreferences.Editor.commit] so hook-side [de.robv.android.xposed.XSharedPreferences]
 * reloads observe the latest snapshot without restarting.
 */
class AllowlistStore(context: Context) {
    private val appContext = context.applicationContext
    private val prefs: SharedPreferences =
        appContext.getSharedPreferences(AllowlistPrefs.PREFS_NAME, Context.MODE_PRIVATE)

    fun getPackages(): Set<String> =
        CallerAllowlist.decode(prefs.getString(AllowlistPrefs.KEY_PACKAGES, "") ?: "")

    fun setPackages(packages: Set<String>) {
        prefs.edit()
            .putString(AllowlistPrefs.KEY_PACKAGES, CallerAllowlist.encode(packages))
            .commit()
        makeWorldReadableBestEffort()
    }

    fun getShowSystemApps(): Boolean =
        prefs.getBoolean(AllowlistPrefs.KEY_SHOW_SYSTEM_APPS, false)

    fun setShowSystemApps(show: Boolean) {
        prefs.edit()
            .putBoolean(AllowlistPrefs.KEY_SHOW_SYSTEM_APPS, show)
            .commit()
        makeWorldReadableBestEffort()
    }

    private fun makeWorldReadableBestEffort() {
        try {
            val sharedPrefsDir = File(appContext.applicationInfo.dataDir, "shared_prefs")
            sharedPrefsDir.setExecutable(true, false)
            sharedPrefsDir.setReadable(true, false)
            val prefsFile = File(sharedPrefsDir, "${AllowlistPrefs.PREFS_NAME}.xml")
            if (prefsFile.exists()) {
                prefsFile.setReadable(true, false)
            }
        } catch (_: Throwable) {
            // LSPosed can still bridge module private prefs into XSharedPreferences.
        }
    }
}
