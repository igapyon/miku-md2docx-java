package jp.igapyon.mikumd2docx.core;

final class XmlUtils {
    private XmlUtils() {
    }

    static String escapeXml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    static String escapeAttr(String text) {
        return escapeXml(text).replace("\"", "&quot;");
    }

    static String stripHtml(String text) {
        return text.replaceAll("<[^>]*>", "");
    }
}
