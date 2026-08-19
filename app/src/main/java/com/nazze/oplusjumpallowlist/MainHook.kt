package com.nazze.oplusjumpallowlist

import com.nazze.oplusjumpallowlist.hook.CheckAllowStartActivityHook
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.callbacks.XC_LoadPackage

/**
 * LSPosed entry point. Jump-intercept decision hooks run in `system_server`
 * (scope / package name `system` or `android` depending on LSPosed build).
 */
class MainHook : IXposedHookLoadPackage {
    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam) {
        if (lpparam.packageName !in SYSTEM_SERVER_PACKAGES) return
        CheckAllowStartActivityHook.install(lpparam.classLoader)
    }

    companion object {
        /**
         * Package names used for `system_server` across LSPosed builds.
         * Stock LSPosed uses `android`; this device's LSPosed scope/log uses `system`.
         */
        val SYSTEM_SERVER_PACKAGES = setOf("android", "system")
    }
}
