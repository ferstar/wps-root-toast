package io.github.ferstar.wpsroottoast;

import org.junit.Test;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public final class RootWarningMatcherTest {
    @Test public void matchesKnownWarningWithLayoutWhitespace() {
        assertTrue(RootWarningMatcher.matches("设备已Root，使用WPS Office可能存在安全风险。"));
        assertTrue(RootWarningMatcher.matches("设备已Root，\n使用WPS\u00a0Office可能存在安全风险。\u3000"));
    }

    @Test public void preservesOrdinaryAndDifferentSecurityMessages() {
        for (String text : new String[]{null, "", "保存成功", "复制成功", "无法打开文件", "Root.docx",
                "设备存在安全风险", "设备已Root，另一项风险提示。",
                "设备已Root，使用WPS Office可能存在安全风险。请立即处理"}) {
            assertFalse(RootWarningMatcher.matches(text));
        }
    }
}
