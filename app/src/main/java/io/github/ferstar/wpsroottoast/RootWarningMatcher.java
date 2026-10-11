package io.github.ferstar.wpsroottoast;

import java.util.regex.Pattern;

/** Exact warning matching: unrelated messages must remain visible. */
final class RootWarningMatcher {
    private static final Pattern SPACES = Pattern.compile("[\\s\\u00a0\\u3000]");
    private static final String WARNING = "设备已Root，使用WPSOffice可能存在安全风险。";

    private RootWarningMatcher() {}

    static boolean matches(CharSequence text) {
        return text != null && WARNING.equals(SPACES.matcher(text).replaceAll(""));
    }
}
