package jp.igapyon.mikumd2docx.core;

final class OoxmlPrimitives {
    private OoxmlPrimitives() {
    }

    static String paragraphXml(String content, String style) {
        return paragraphXml(content, style, null, 0);
    }

    static String paragraphXml(String content, String style, String numId, int level) {
        String styleXml = style == null ? "" : "<w:pStyle w:val=\"" + XmlUtils.escapeAttr(style) + "\"/>";
        String numXml = numId == null ? "" : "<w:numPr><w:ilvl w:val=\"" + level + "\"/><w:numId w:val=\"" + numId + "\"/></w:numPr>";
        String pPr = styleXml.isEmpty() && numXml.isEmpty() ? "" : "<w:pPr>" + styleXml + numXml + "</w:pPr>";
        return "<w:p>" + pPr + content + "</w:p>";
    }

    static String runXml(String text, RunStyle style) {
        String preserve = needsSpacePreserve(text) ? " xml:space=\"preserve\"" : "";
        String props = (style.bold ? "<w:b/>" : "") + (style.italic ? "<w:i/>" : "")
                + (style.strike ? "<w:strike/>" : "") + (style.underline ? "<w:u w:val=\"single\"/>" : "")
                + (style.code ? "<w:rStyle w:val=\"CodeChar\"/>" : "");
        String rPr = props.isEmpty() ? "" : "<w:rPr>" + props + "</w:rPr>";
        return "<w:r>" + rPr + "<w:t" + preserve + ">" + XmlUtils.escapeXml(text) + "</w:t></w:r>";
    }

    static String tableXml(String rows) {
        return "<w:tbl><w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/><w:tblBorders>"
                + "<w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
                + "<w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
                + "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
                + "<w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
                + "<w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
                + "<w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
                + "</w:tblBorders></w:tblPr>" + rows + "</w:tbl>";
    }

    static String tableCellXml(String content) {
        return "<w:tc><w:tcPr><w:tcW w:w=\"2400\" w:type=\"dxa\"/></w:tcPr>" + paragraphXml(content, null) + "</w:tc>";
    }

    static String drawingXml(String relId, String alt, int cx, int cy, int docPrId) {
        String escapedAlt = XmlUtils.escapeAttr(alt);
        return "<w:r><w:drawing><wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\"><wp:extent cx=\"" + cx + "\" cy=\"" + cy
                + "\"/><wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/><wp:docPr id=\"" + docPrId + "\" name=\"Image " + docPrId
                + "\" descr=\"" + escapedAlt + "\"/><wp:cNvGraphicFramePr><a:graphicFrameLocks noChangeAspect=\"1\"/></wp:cNvGraphicFramePr><a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:pic><pic:nvPicPr><pic:cNvPr id=\""
                + docPrId + "\" name=\"Image " + docPrId + "\" descr=\"" + escapedAlt
                + "\"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed=\"" + relId
                + "\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\""
                + cx + "\" cy=\"" + cy + "\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r>";
    }

    private static boolean needsSpacePreserve(String text) {
        if (text.isEmpty()) {
            return false;
        }
        if (Character.isWhitespace(text.charAt(0)) || Character.isWhitespace(text.charAt(text.length() - 1))) {
            return true;
        }
        for (int i = 1; i < text.length(); i++) {
            if (Character.isWhitespace(text.charAt(i - 1)) && Character.isWhitespace(text.charAt(i))) {
                return true;
            }
        }
        return false;
    }
}
