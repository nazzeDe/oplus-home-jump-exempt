package com.nazze.oplusjumpallowlist.config

import android.content.SharedPreferences
import java.io.File

/**
 * Helpers so hook-side [de.robv.android.xposed.XSharedPreferences] can read module prefs.
 *
 * Under LSPosed, [Context.getSharedPreferences] may write into the misc prefs bridge
 * (`/data/misc/.../prefs/<package>/`) instead of the app dataDir copy. Chmod must target
 * that real file; the dataDir path alone is not enough.
 */
object PrefsAccess {
    /**
     * Best-effort world-readable (and parent traverse) bits for [file].
     * Returns false when the file is missing or chmod fails.
     */
    fun ensureWorldReadable(file: File): Boolean {
        return try {
            val parent = file.parentFile
            if (parent != null) {
                parent.mkdirs()
                parent.setExecutable(true, false)
                parent.setReadable(true, false)
            }
            if (!file.exists()) return false
            file.setReadable(true, false)
        } catch (_: Throwable) {
            false
        }
    }

    /**
     * Actual on-disk file backing [prefs], when it is Android's SharedPreferencesImpl
     * (including LSPosed-redirected paths). Null when reflection cannot resolve it.
     */
    fun sharedPreferencesFile(prefs: SharedPreferences): File? {
        var clazz: Class<*>? = prefs.javaClass
        while (clazz != null) {
            try {
                val field = clazz.getDeclaredField("mFile")
                field.isAccessible = true
                return field.get(prefs) as? File
            } catch (_: NoSuchFieldException) {
                clazz = clazz.superclass
            } catch (_: Throwable) {
                return null
            }
        }
        return null
    }

    /** dataDir conventional path — may be stale under LSPosed redirect; still chmod'd as fallback. */
    fun conventionalPrefsFile(dataDir: File, prefsName: String): File =
        File(File(dataDir, "shared_prefs"), "$prefsName.xml")
}
