package io.github.ferstar.wpsroottoast;

import android.content.Context;
import android.util.Log;
import android.view.View;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.List;
import io.github.libxposed.api.XposedModule;
import io.github.libxposed.api.XposedInterface.ExceptionMode;
import io.github.libxposed.api.error.HookFailedError;

/** Modern API 102 module: filters one warning without changing root detection. */
public final class Hook extends XposedModule {
    private static final String TARGET_PACKAGE = "cn.wps.moffice_eng";
    private static final String TAG = "WpsRootToast";
    private boolean installed;
    private final WarningToastRegistry<Toast> warnings = new WarningToastRegistry<>();

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
        List<HookHandle> handles = new ArrayList<>();
        try {
            handles.add(hook(Toast.class.getDeclaredMethod("makeText", Context.class, CharSequence.class, int.class))
                    .setId("wps-warning-create-text")
                    .setExceptionMode(ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        Toast result = (Toast) chain.proceed();
                        warnings.record(result, (CharSequence) chain.getArgs().get(1));
                        return result;
                    }));
            // Hook the resource overload too, so inlining of makeText(CharSequence) cannot bypass tracking.
            handles.add(hook(Toast.class.getDeclaredMethod("makeText", Context.class, int.class, int.class))
                    .setId("wps-warning-create-resource")
                    .setExceptionMode(ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        Toast result = (Toast) chain.proceed();
                        Context context = (Context) chain.getArgs().get(0);
                        int resourceId = (Integer) chain.getArgs().get(1);
                        warnings.record(result, context.getText(resourceId));
                        return result;
                    }));
            handles.add(hook(Toast.class.getDeclaredMethod("setText", CharSequence.class))
                    .setId("wps-warning-change-text")
                    .setExceptionMode(ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        Object result = chain.proceed();
                        warnings.record((Toast) chain.getThisObject(), (CharSequence) chain.getArgs().get(0));
                        return result;
                    }));
            // Android's public setText(int) delegates to setText(CharSequence); prevent that call being inlined.
            deoptimize(Toast.class.getDeclaredMethod("setText", int.class));
            handles.add(hook(Toast.class.getDeclaredMethod("setView", View.class))
                    .setId("wps-warning-custom-view")
                    .setExceptionMode(ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        Object result = chain.proceed();
                        warnings.clear((Toast) chain.getThisObject());
                        return result;
                    }));
            handles.add(hook(Toast.class.getDeclaredMethod("show"))
                    .setId("wps-warning-show")
                    .setExceptionMode(ExceptionMode.PROTECTIVE)
                    .intercept(chain -> {
                        if (warnings.shouldBlock((Toast) chain.getThisObject())) {
                            log(Log.INFO, TAG, "Blocked matching root warning (API 102)");
                            return null;
                        }
                        return chain.proceed();
                    }));
            installed = true;
            log(Log.INFO, TAG, "Exact warning filter installed (API 102)");
        } catch (ReflectiveOperationException | RuntimeException | LinkageError | HookFailedError ignored) {
            for (HookHandle handle : handles) handle.unhook();
            // Fail open if a future Android/framework release changes the public methods or hook API.
            log(Log.WARN, TAG, "Filter unavailable; WPS behavior unchanged");
        }
    }
}
