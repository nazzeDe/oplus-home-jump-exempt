package com.nazze.oplusjumpallowlist.config

import android.content.Context
import android.content.SharedPreferences
import java.io.File

/**
 * Settings-process persistence for the caller allowlist.
 *
 * Uses [SharedPreferences.Editor.commit] so hook-side [de.robv.android.xposed.XSharedPreferences]
 * reloads observe the latest snapshot without restarting. After each write, makes the real
 * prefs file world-readable (including LSPosed misc-bridge paths) so `system_server` can read it.
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
        // LSPosed often redirects SharedPreferences to /data/misc/.../prefs/<package>/.
        // Chmod that real file (via mFile); also chmod the conventional dataDir path as fallback.
        val targets = linkedSetOf<File>()
        PrefsAccess.sharedPreferencesFile(prefs)?.let { targets.add(it) }
        targets.add(
            PrefsAccess.conventionalPrefsFile(
                File(appContext.applicationInfo.dataDir),
                AllowlistPrefs.PREFS_NAME,
            ),
        )
        for (file in targets) {
            PrefsAccess.ensureWorldReadable(file)
        }
    }
}
