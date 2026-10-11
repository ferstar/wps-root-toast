package io.github.ferstar.wpsroottoast;

import java.util.Collections;
import java.util.Set;
import java.util.WeakHashMap;

/** Tracks only matching Toast identities, never retaining message text or strong keys. */
final class WarningToastRegistry<T> {
    private final Set<T> warnings = Collections.newSetFromMap(new WeakHashMap<>());

    synchronized void record(T toast, CharSequence text) {
        if (toast == null) return;
        if (RootWarningMatcher.matches(text)) warnings.add(toast);
        else warnings.remove(toast);
    }

    synchronized void clear(T toast) {
        warnings.remove(toast);
    }

    synchronized boolean shouldBlock(T toast) {
        return warnings.contains(toast);
    }
}
