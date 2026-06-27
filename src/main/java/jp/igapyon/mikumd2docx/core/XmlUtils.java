package jp.igapyon.mikumd2docx.core;

import jp.igapyon.mikumsofficecore.XmlHelper;

final class XmlUtils {
    private XmlUtils() {
    }

    static String escapeXml(String text) {
        return XmlHelper.escapeXmlText(text == null ? "" : text);
    }

    static String escapeAttr(String text) {
        return XmlHelper.escapeXmlAttribute(text == null ? "" : text);
    }

    static String stripHtml(String text) {
        return text == null ? "" : text.replaceAll("<[^>]*>", "");
    }
}
