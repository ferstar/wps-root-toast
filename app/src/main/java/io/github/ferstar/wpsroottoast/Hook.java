package io.github.ferstar.wpsroottoast;

import android.widget.Toast;
import de.robv.android.xposed.IXposedHookLoadPackage;
import de.robv.android.xposed.XC_MethodHook;
import de.robv.android.xposed.XposedBridge;
import de.robv.android.xposed.XposedHelpers;
import de.robv.android.xposed.callbacks.XC_LoadPackage;

/** Filters a single warning in WPS, without changing its root detection. */
public final class Hook implements IXposedHookLoadPackage {
    private static final String TARGET_PACKAGE = "cn.wps.moffice_eng";
    private static boolean installed;

    @Override
    public synchronized void handleLoadPackage(XC_LoadPackage.LoadPackageParam app) {
        if (!TARGET_PACKAGE.equals(app.packageName) || installed) return;
        try {
            XposedHelpers.findAndHookMethod(Toast.class, "show", new XC_MethodHook() {
                @Override
                protected void beforeHookedMethod(MethodHookParam param) {
                    Object text;
                    try {
                        text = XposedHelpers.getObjectField(param.thisObject, "mText");
                    } catch (Throwable ignored) {
                        // A changed Android implementation or a custom Toast stays untouched.
                        return;
                    }
                    if (text instanceof CharSequence && RootWarningMatcher.matches((CharSequence) text)) {
                        param.setResult(null);
                        XposedBridge.log("[WpsRootToast] Blocked matching root warning");
                    }
                }
            });
            installed = true;
            XposedBridge.log("[WpsRootToast] Exact warning filter installed");
        } catch (Throwable ignored) {
            // Fail open: preserve WPS behavior if a future framework changes the API.
            XposedBridge.log("[WpsRootToast] Filter unavailable; WPS behavior unchanged");
        }
    }
}
