package com.nazze.oplusjumpallowlist.hook

import android.content.Intent
import com.nazze.oplusjumpallowlist.config.HookAllowlistReader
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers

/**
 * Decision-point hook for ColorOS inter-app start confirmation.
 *
 * Target: [com.android.server.am.OplusSecurityPermissionManager.checkAllowStartActivity]
 * Returning [RESULT_ALLOW_WITHOUT_CONFIRM] skips [AppStartConfirmDialogActivity].
 * Non-allowlisted callers and config/read failures leave the original method alone (fail-closed).
 */
object CheckAllowStartActivityHook {
    const val TARGET_CLASS = "com.android.server.am.OplusSecurityPermissionManager"
    const val TARGET_METHOD = "checkAllowStartActivity"

    /** Stock ColorOS return that means "no confirm dialog; start proceeds". */
    const val RESULT_ALLOW_WITHOUT_CONFIRM = -1

    private const val LOG_TAG = "OplusJumpAllowlist"

    fun install(classLoader: ClassLoader, reader: HookAllowlistReader = HookAllowlistReader()) {
        try {
            XposedHelpers.findAndHookMethod(
                TARGET_CLASS,
                classLoader,
                TARGET_METHOD,
                String::class.java,
                String::class.java,
                Intent::class.java,
                Int::class.javaPrimitiveType,
                Int::class.javaPrimitiveType,
                object : XC_MethodHook() {
                    override fun beforeHookedMethod(param: MethodHookParam) {
                        val callerPackage = param.args.getOrNull(0) as? String
                        if (!reader.isCallerAllowed(callerPackage)) {
                            return
                        }
                        param.result = RESULT_ALLOW_WITHOUT_CONFIRM
                        XposedBridge.log(
                            "[$LOG_TAG] allow without confirm: caller=$callerPackage",
                        )
                    }
                },
            )
            XposedBridge.log(
                "[$LOG_TAG] hooked $TARGET_CLASS.$TARGET_METHOD",
            )
        } catch (t: Throwable) {
            XposedBridge.log("[$LOG_TAG] failed to hook $TARGET_CLASS.$TARGET_METHOD: $t")
        }
    }
}
