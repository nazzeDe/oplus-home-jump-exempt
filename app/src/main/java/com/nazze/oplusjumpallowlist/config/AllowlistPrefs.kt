package com.nazze.oplusjumpallowlist.config

/**
 * SharedPreferences identity for settings writes and hook-side hot reads.
 */
object AllowlistPrefs {
    const val MODULE_PACKAGE = "com.nazze.oplusjumpallowlist"
    const val PREFS_NAME = "caller_allowlist"
    const val KEY_PACKAGES = "packages"
    const val KEY_SHOW_SYSTEM_APPS = "show_system_apps"
}
