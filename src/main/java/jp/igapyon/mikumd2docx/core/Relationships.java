package jp.igapyon.mikumd2docx.core;

import java.util.List;

final class Relationships {
    static final String REL_HYPERLINK = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink";
    static final String REL_IMAGE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image";

    private Relationships() {
    }

    static String documentRelsXml(List<Relationship> relationships) {
        StringBuilder rels = new StringBuilder();
        rels.append("<Relationship Id=\"rIdStyles\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>");
        rels.append("<Relationship Id=\"rIdNumbering\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering\" Target=\"numbering.xml\"/>");
        for (Relationship rel : relationships) {
            String mode = rel.targetMode == null ? "" : " TargetMode=\"" + XmlUtils.escapeAttr(rel.targetMode) + "\"";
            rels.append("<Relationship Id=\"").append(rel.id).append("\" Type=\"").append(XmlUtils.escapeAttr(rel.type)).append("\" Target=\"")
                    .append(XmlUtils.escapeAttr(rel.target)).append("\"").append(mode).append("/>");
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" + rels + "</Relationships>";
    }
}
