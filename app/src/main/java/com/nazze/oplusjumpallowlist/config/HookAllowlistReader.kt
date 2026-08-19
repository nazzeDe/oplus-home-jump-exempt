package com.nazze.oplusjumpallowlist.config

import de.robv.android.xposed.XSharedPreferences

/**
 * Hook-facing allowlist reader. Call [isCallerAllowed] on each decision; it reloads from disk.
 *
 * Read failures (exceptions / unreadable prefs file) fail closed — the caller is not exempt.
 * A missing key is treated as the default empty allowlist, not a read failure.
 */
class HookAllowlistReader(
    preferencesFactory: () -> XSharedPreferences = {
        XSharedPreferences(AllowlistPrefs.MODULE_PACKAGE, AllowlistPrefs.PREFS_NAME)
    },
) {
    private val prefs: XSharedPreferences by lazy(preferencesFactory)

    fun isCallerAllowed(callerPackage: String?): Boolean {
        return try {
            val encoded = readEncodedOrNullOnFailure() ?: return false
            CallerAllowlist.isCallerAllowed(callerPackage, encoded)
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Latest package set, or empty when unreadable / unset.
     * Prefer [isCallerAllowed] at the decision point so fail-closed stays centralized.
     */
    fun readPackages(): Set<String> {
        return try {
            val encoded = readEncodedOrNullOnFailure() ?: return emptySet()
            CallerAllowlist.decode(encoded)
        } catch (_: Throwable) {
            emptySet()
        }
    }

    private fun readEncodedOrNullOnFailure(): String? {
        prefs.reload()
        val file = prefs.file
        if (file.exists() && !file.canRead()) {
            return null
        }
        // Missing key => default empty allowlist (encoded "").
        return prefs.getString(AllowlistPrefs.KEY_PACKAGES, "") ?: ""
    }
}
