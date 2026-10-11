package io.github.ferstar.wpsroottoast;

import android.util.Log;
import android.widget.Toast;
import java.lang.reflect.Field;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedInterface.ExceptionMode;

/** Modern API 102 module: filters one warning without changing root detection. */
public final class Hook extends XposedModule {
    private static final String TARGET_PACKAGE = "cn.wps.moffice_eng";
    private static final String TAG = "WpsRootToast";
    private boolean installed;

    @Override
    public void onPackageReady(PackageReadyParam app) {
        if (TARGET_PACKAGE.equals(app.getPackageName())) installFilter();
    }

    @Override
    public void onHotReloaded(HotReloadedParam param) {
        String process = param.getProcessName();
        if (TARGET_PACKAGE.equals(process) || process.startsWith(TARGET_PACKAGE + ":")) {
            // API 102 replaces this module's previous hook atomically by its stable id.
            installFilter();
        }
    }

    private synchronized void installFilter() {
        if (installed) return;
        try {
            Field textField = Toast.class.getDeclaredField("mText");
            textField.setAccessible(true);
            hook(Toast.class.getDeclaredMethod("show"))
                    .setId("wps-root-warning-text")
                    .setExceptionMode(ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        Object text;
                        try {
                            text = textField.get(chain.getThisObject());
                        } catch (ReflectiveOperationException | SecurityException ignored) {
                            return chain.proceed();
                        }
                        if (text instanceof CharSequence && RootWarningMatcher.matches((CharSequence) text)) {
                            log(Log.INFO, TAG, "Blocked matching root warning (API 102)");
                            return null;
                        }
                        return chain.proceed();
                    });
            installed = true;
            log(Log.INFO, TAG, "Exact warning filter installed (API 102)");
        } catch (ReflectiveOperationException | RuntimeException | LinkageError ignored) {
            // Fail open if a future Android/framework release changes the reflection or hook API.
            log(Log.WARN, TAG, "Filter unavailable; WPS behavior unchanged");
        }
    }
}
