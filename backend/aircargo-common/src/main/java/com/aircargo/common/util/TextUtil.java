package com.aircargo.common.util;

public final class TextUtil {

    private TextUtil() {}

    public static String xmlEscape(String s) {
        if (s == null) return "";
        return s.replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&apos;");
    }

    public static String safe(String s) {
        return s != null ? s.replace("\"", "\\\"") : "";
    }
}
