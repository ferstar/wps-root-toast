package io.github.ferstar.wpsroottoast;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class WarningToastRegistryTest {
    private static final String WARNING = "设备已Root，使用WPS Office可能存在安全风险。";

    @Test public void onlyBlocksTheMatchingToastInstance() {
        WarningToastRegistry<Object> registry = new WarningToastRegistry<>();
        Object warning = new Object();
        Object ordinary = new Object();
        registry.record(warning, WARNING);
        registry.record(ordinary, "保存成功");
        assertTrue(registry.shouldBlock(warning));
        assertFalse(registry.shouldBlock(ordinary));
        assertFalse(registry.shouldBlock(new Object()));
    }

    @Test public void allowsAWarningToastReusedForAnOrdinaryMessage() {
        WarningToastRegistry<Object> registry = new WarningToastRegistry<>();
        Object toast = new Object();
        registry.record(toast, WARNING);
        registry.record(toast, "复制成功");
        assertFalse(registry.shouldBlock(toast));
        registry.record(toast, WARNING);
        registry.clear(toast);
        assertFalse(registry.shouldBlock(toast));
    }

    @Test public void nullOrUnknownMessagesStayUntouched() {
        WarningToastRegistry<Object> registry = new WarningToastRegistry<>();
        Object toast = new Object();
        registry.record(null, WARNING);
        registry.record(toast, null);
        assertFalse(registry.shouldBlock(toast));
    }
}
