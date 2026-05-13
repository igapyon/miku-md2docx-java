package jp.igapyon.mikumd2docx.core;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.HashMap;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

public class MikuMd2docxCore {
    public static final String VERSION = "0.5.0.1";

    private static final String REL_HYPERLINK = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/hyperlink";
    private static final String REL_IMAGE = "http://schemas.openxmlformats.org/officeDocument/2006/relationships/image";
    private static final int DOC_BODY_WIDTH_EMU = 5943600;
    private static final int EMU_PER_PIXEL_AT_96_DPI = 9525;
    private static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.+)$");
    private static final Pattern LIST = Pattern.compile("^(\\s*)([-*+]|\\d+\\.)\\s+(.+)$");
    private static final Pattern IMAGE = Pattern.compile("!\\[([^]]*)]\\(([^)]+)\\)");
    private static final Pattern LINK = Pattern.compile("(?<!!)\\[([^]]+)]\\(([^)]+)\\)");

    public Md2DocxResult convertMarkdownToDocx(String markdown) {
        return convertMarkdownToDocx(markdown, new Md2DocxOptions());
    }

    public Md2DocxResult convertMarkdownToDocx(String markdown, Md2DocxOptions options) {
        RenderState state = new RenderState(options == null ? new Md2DocxOptions() : options);
        collectHeadingBookmarks(markdown == null ? "" : markdown, state);
        String documentXml = buildDocumentXml(renderMarkdown(markdown == null ? "" : markdown, state));
        return new Md2DocxResult(createDocx(documentXml, state), state.summary);
    }

    public String formatSummary(Md2DocxSummary summary) {
        StringBuilder builder = new StringBuilder();
        builder.append("paragraphs: ").append(summary.paragraphs).append('\n');
        builder.append("headings: ").append(summary.headings).append('\n');
        builder.append("links: ").append(summary.links).append('\n');
        builder.append("internalLinks: ").append(summary.internalLinks).append('\n');
        builder.append("externalLinks: ").append(summary.externalLinks).append('\n');
        builder.append("unresolvedInternalLinks: ").append(summary.unresolvedInternalLinks).append('\n');
        builder.append("lists: ").append(summary.lists).append('\n');
        builder.append("listItems: ").append(summary.listItems).append('\n');
        builder.append("tables: ").append(summary.tables).append('\n');
        builder.append("codeBlocks: ").append(summary.codeBlocks).append('\n');
        builder.append("blockquotes: ").append(summary.blockquotes).append('\n');
        builder.append("horizontalRules: ").append(summary.horizontalRules).append('\n');
        builder.append("images: ").append(summary.images).append('\n');
        builder.append("embeddedImages: ").append(summary.embeddedImages).append('\n');
        builder.append("missingImages: ").append(summary.missingImages).append('\n');
        builder.append("resizedImages: ").append(summary.resizedImages).append('\n');
        builder.append("frontMatter: ").append(summary.frontMatter).append('\n');
        builder.append("unsupportedHtml: ").append(summary.unsupportedHtml).append('\n');
        if (!summary.missingImageDetails.isEmpty()) {
            builder.append("missingImageDetails:\n");
            for (Md2DocxSummary.MissingImageDetail detail : summary.missingImageDetails) {
                builder.append("- path: ").append(detail.path).append('\n');
                builder.append("  alt: ").append(detail.alt).append('\n');
            }
        }
        return builder.toString();
    }

    private String renderMarkdown(String markdown, RenderState state) {
        StringBuilder body = new StringBuilder();
        String[] lines = markdown.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        boolean inFrontMatter = lines.length > 0 && "---".equals(lines[0].trim());
        boolean inCode = false;
        List<String> codeLines = new ArrayList<String>();
        if (inFrontMatter) {
            state.summary.frontMatter = true;
        }

        for (int index = inFrontMatter ? 1 : 0; index < lines.length; index++) {
            String line = lines[index];
            String trimmed = line.trim();
            if (inFrontMatter) {
                if ("---".equals(trimmed)) {
                    inFrontMatter = false;
                }
                continue;
            }
            if (trimmed.startsWith("```")) {
                if (inCode) {
                    renderCodeBlock(body, codeLines, state);
                    codeLines.clear();
                } else {
                    state.summary.codeBlocks++;
                }
                inCode = !inCode;
                continue;
            }
            if (inCode) {
                codeLines.add(line);
                continue;
            }
            if (trimmed.isEmpty()) {
                continue;
            }
            Matcher heading = HEADING.matcher(trimmed);
            if (heading.matches()) {
                renderHeading(body, heading.group(1).length(), heading.group(2), state);
            } else if (isTableStart(lines, index)) {
                index = renderTable(body, lines, index, state);
            } else if (LIST.matcher(line).matches()) {
                index = renderList(body, lines, index, state);
            } else if (trimmed.startsWith(">")) {
                state.summary.blockquotes++;
                state.summary.paragraphs++;
                body.append(paragraphXml(renderInline(trimmed.replaceFirst("^>\\s*", ""), state), "Quote"));
            } else if (trimmed.matches("^(-{3,}|\\*{3,}|_{3,})$")) {
                state.summary.horizontalRules++;
                body.append(paragraphXml(runXml("----------", new RunStyle()), "Separator"));
            } else {
                state.summary.paragraphs++;
                body.append(paragraphXml(renderInline(trimmed, state), "Normal"));
            }
        }
        if (inCode) {
            renderCodeBlock(body, codeLines, state);
        }
        return body.toString();
    }

    private void renderHeading(StringBuilder body, int level, String text, RenderState state) {
        state.summary.headings++;
        String plain = plainInlineText(text);
        String bookmark = state.nextHeadingBookmark(plain);
        String bookmarkXml = "";
        if (bookmark != null) {
            bookmarkXml = "<w:bookmarkStart w:id=\"" + state.summary.headings + "\" w:name=\"" + escapeAttr(bookmark) + "\"/>";
        }
        String bookmarkEnd = bookmark == null ? "" : "<w:bookmarkEnd w:id=\"" + state.summary.headings + "\"/>";
        body.append(paragraphXml(bookmarkXml + renderInline(text, state) + bookmarkEnd, "Heading" + Math.min(Math.max(level, 1), 6)));
    }

    private int renderList(StringBuilder body, String[] lines, int start, RenderState state) {
        int index = start;
        String previousListKey = null;
        while (index < lines.length) {
            Matcher matcher = LIST.matcher(lines[index]);
            if (!matcher.matches()) {
                break;
            }
            String marker = matcher.group(2);
            String value = matcher.group(3);
            int level = Math.min(matcher.group(1).length() / 2, 2);
            boolean ordered = marker.endsWith(".");
            String listKey = level + ":" + ordered;
            if (!listKey.equals(previousListKey)) {
                state.summary.lists++;
                previousListKey = listKey;
            }
            state.summary.listItems++;
            String text = value.replaceFirst("^\\[( |x|X)]\\s+", "[$1] ");
            body.append(paragraphXml(renderInline(text, state), null, ordered ? "2" : "1", level));
            index++;
        }
        return index - 1;
    }

    private boolean isTableStart(String[] lines, int index) {
        if (index + 1 >= lines.length) {
            return false;
        }
        return lines[index].trim().startsWith("|") && lines[index].trim().endsWith("|")
                && lines[index + 1].trim().matches("^\\|?\\s*:?-{3,}:?\\s*(\\|\\s*:?-{3,}:?\\s*)+\\|?$");
    }

    private int renderTable(StringBuilder body, String[] lines, int start, RenderState state) {
        state.summary.tables++;
        StringBuilder rows = new StringBuilder();
        int index = start;
        int rowIndex = 0;
        while (index < lines.length) {
            String trimmed = lines[index].trim();
            if (!trimmed.startsWith("|") || !trimmed.endsWith("|")) {
                break;
            }
            if (index == start + 1) {
                index++;
                continue;
            }
            String[] cells = trimmed.substring(1, trimmed.length() - 1).split("\\|", -1);
            StringBuilder cellXml = new StringBuilder();
            for (String cell : cells) {
                RunStyle style = new RunStyle();
                style.bold = rowIndex == 0;
                cellXml.append(tableCellXml(renderInline(cell.trim(), state, style)));
            }
            rows.append("<w:tr>").append(cellXml).append("</w:tr>");
            rowIndex++;
            index++;
        }
        body.append(tableXml(rows.toString()));
        return index - 1;
    }

    private void renderCodeBlock(StringBuilder body, List<String> codeLines, RenderState state) {
        for (String codeLine : codeLines) {
            RunStyle style = new RunStyle();
            style.code = true;
            body.append(paragraphXml(runXml(codeLine, style), "Code"));
        }
    }

    private String renderInline(String text, RenderState state) {
        return renderInline(text, state, new RunStyle());
    }

    private String renderInline(String text, RenderState state, RunStyle inherited) {
        StringBuilder xml = new StringBuilder();
        int index = 0;
        Matcher token = Pattern.compile("!\\[[^]]*]\\([^)]+\\)|\\[[^]]+]\\([^)]+\\)|<br\\s*/?>|<ins>.*?</ins>|<a\\s+[^>]*href=[\"'][^\"']+[\"'][^>]*>.*?</a>|<img\\s+[^>]*>", Pattern.CASE_INSENSITIVE).matcher(text);
        while (token.find()) {
            appendStyledText(xml, text.substring(index, token.start()), inherited, state);
            xml.append(renderToken(token.group(), state, inherited));
            index = token.end();
        }
        appendStyledText(xml, text.substring(index), inherited, state);
        return xml.toString();
    }

    private String renderToken(String token, RenderState state, RunStyle inherited) {
        Matcher image = IMAGE.matcher(token);
        if (image.matches()) {
            return renderImage(image.group(2), image.group(1), state);
        }
        Matcher link = LINK.matcher(token);
        if (link.matches()) {
            return renderLink(link.group(2), renderInline(link.group(1), state, inherited), plainInlineText(link.group(1)), state);
        }
        if (token.matches("(?i)<br\\s*/?>")) {
            return "<w:r><w:br/></w:r>";
        }
        Matcher ins = Pattern.compile("(?i)<ins>(.*?)</ins>").matcher(token);
        if (ins.matches()) {
            RunStyle style = inherited.copy();
            style.underline = true;
            return renderInline(ins.group(1), state, style);
        }
        Matcher htmlLink = Pattern.compile("(?i)<a\\s+[^>]*href=[\"']([^\"']+)[\"'][^>]*>(.*?)</a>").matcher(token);
        if (htmlLink.matches()) {
            return renderLink(htmlLink.group(1), renderInline(stripHtml(htmlLink.group(2)), state, inherited), stripHtml(htmlLink.group(2)), state);
        }
        Matcher htmlImage = Pattern.compile("(?i)<img\\s+[^>]*src=[\"']([^\"']+)[\"'][^>]*>").matcher(token);
        if (htmlImage.matches()) {
            Matcher alt = Pattern.compile("(?i)\\salt=[\"']([^\"']*)[\"']").matcher(token);
            return renderImage(htmlImage.group(1), alt.find() ? alt.group(1) : "", state);
        }
        state.summary.unsupportedHtml++;
        return runXml(stripHtml(token), inherited);
    }

    private void appendStyledText(StringBuilder xml, String text, RunStyle inherited, RenderState state) {
        if (text.isEmpty()) {
            return;
        }
        countUnsupportedHtml(text, state);
        int index = 0;
        Matcher marker = Pattern.compile("(\\*\\*[^*]+\\*\\*|__[^_]+__|\\*[^*]+\\*|_[^_]+_|~~[^~]+~~|`[^`]+`)").matcher(text);
        while (marker.find()) {
            appendPlainText(xml, text.substring(index, marker.start()), inherited);
            String value = marker.group();
            RunStyle style = inherited.copy();
            if (value.startsWith("**") || value.startsWith("__")) {
                style.bold = true;
                value = value.substring(2, value.length() - 2);
            } else if (value.startsWith("*") || value.startsWith("_")) {
                style.italic = true;
                value = value.substring(1, value.length() - 1);
            } else if (value.startsWith("~~")) {
                style.strike = true;
                value = value.substring(2, value.length() - 2);
            } else if (value.startsWith("`")) {
                style.code = true;
                value = value.substring(1, value.length() - 1);
            }
            appendPlainText(xml, value, style);
            index = marker.end();
        }
        appendPlainText(xml, text.substring(index), inherited);
    }

    private void countUnsupportedHtml(String text, RenderState state) {
        Matcher tag = Pattern.compile("<\\/?([a-zA-Z][a-zA-Z0-9-]*)(\\s+[^>]*)?>").matcher(text);
        while (tag.find()) {
            String name = tag.group(1).toLowerCase(Locale.ROOT);
            if (!"br".equals(name) && !"ins".equals(name) && !"a".equals(name) && !"img".equals(name)) {
                state.summary.unsupportedHtml++;
            }
        }
    }

    private void appendPlainText(StringBuilder xml, String text, RunStyle style) {
        String cleaned = text.replaceAll("<[^>]*>", "");
        if (!cleaned.isEmpty()) {
            xml.append(runXml(cleaned, style));
        }
    }

    private String renderLink(String url, String inlineXml, String text, RenderState state) {
        state.summary.links++;
        if (url.startsWith("#")) {
            state.summary.internalLinks++;
            String anchor = normalizeAnchor(url.substring(1));
            if (state.knownBookmarks.contains(anchor)) {
                return "<w:hyperlink w:anchor=\"" + escapeAttr(anchor) + "\" w:history=\"1\">" + inlineXml + "</w:hyperlink>";
            }
            state.summary.unresolvedInternalLinks++;
            return inlineXml;
        }
        state.summary.externalLinks++;
        String relId = state.addRelationship(REL_HYPERLINK, url, "External");
        return "<w:hyperlink r:id=\"" + relId + "\" w:history=\"1\">" + inlineXml + "</w:hyperlink>";
    }

    private String renderImage(String path, String alt, RenderState state) {
        state.summary.images++;
        ImageAsset asset = state.options.getImageLoader() == null ? null : state.options.getImageLoader().load(path);
        if (asset == null) {
            state.summary.missingImages++;
            state.summary.missingImageDetails.add(new Md2DocxSummary.MissingImageDetail(path, alt));
            return runXml("[Missing image: " + (alt == null || alt.isEmpty() ? path : alt) + "]", new RunStyle());
        }
        String mediaName = safeMediaName(state.summary.embeddedImages + 1, asset.getPath());
        String mediaPath = "word/media/" + mediaName;
        String relId = state.addRelationship(REL_IMAGE, mediaPath.replaceFirst("^word/", ""), null);
        ImageSize display = displaySizeForImage(asset.getData());
        if (display.resized) {
            state.summary.resizedImages++;
        }
        state.summary.embeddedImages++;
        state.imageMedia.put(mediaPath, asset.getData());
        return drawingXml(relId, alt == null ? "" : alt, display.width, display.height, state.nextDocPrId++);
    }

    private void collectHeadingBookmarks(String markdown, RenderState state) {
        Set<String> used = new LinkedHashSet<String>();
        String[] lines = markdown.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        boolean inFrontMatter = lines.length > 0 && "---".equals(lines[0].trim());
        for (int i = inFrontMatter ? 1 : 0; i < lines.length; i++) {
            String trimmed = lines[i].trim();
            if (inFrontMatter) {
                if ("---".equals(trimmed)) {
                    inFrontMatter = false;
                }
                continue;
            }
            Matcher heading = HEADING.matcher(trimmed);
            if (!heading.matches()) {
                continue;
            }
            String text = plainInlineText(heading.group(2));
            String base = normalizeAnchor(text);
            if (base.isEmpty()) {
                base = "heading-" + (used.size() + 1);
            }
            String candidate = base;
            int counter = 2;
            while (used.contains(candidate)) {
                candidate = base + "-" + counter++;
            }
            used.add(candidate);
            List<String> candidates = state.headingBookmarks.get(text);
            if (candidates == null) {
                candidates = new ArrayList<String>();
                state.headingBookmarks.put(text, candidates);
            }
            candidates.add(candidate);
            state.knownBookmarks.add(candidate);
        }
    }

    private String plainInlineText(String text) {
        return stripHtml(text).replaceAll("!\\[([^]]*)]\\(([^)]+)\\)", "$1")
                .replaceAll("\\[([^]]+)]\\(([^)]+)\\)", "$1")
                .replace("**", "").replace("__", "").replace("*", "").replace("_", "")
                .replace("~~", "").replace("`", "").trim();
    }

    private String normalizeAnchor(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9\\-_]+", "-").replaceAll("-+", "-").replaceAll("^-|-$", "");
    }

    private String buildDocumentXml(String body) {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\" "
                + "xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\" "
                + "xmlns:wp=\"http://schemas.openxmlformats.org/drawingml/2006/wordprocessingDrawing\" "
                + "xmlns:a=\"http://schemas.openxmlformats.org/drawingml/2006/main\" "
                + "xmlns:pic=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><w:body>"
                + body
                + "<w:sectPr><w:pgSz w:w=\"12240\" w:h=\"15840\"/><w:pgMar w:top=\"1440\" w:right=\"1440\" w:bottom=\"1440\" w:left=\"1440\" w:header=\"720\" w:footer=\"720\" w:gutter=\"0\"/></w:sectPr>"
                + "</w:body></w:document>";
    }

    private byte[] createDocx(String documentXml, RenderState state) {
        try {
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            ZipOutputStream zip = new ZipOutputStream(bytes);
            add(zip, "[Content_Types].xml", contentTypesXml(state.imageMedia.keySet()));
            add(zip, "_rels/.rels", rootRelsXml());
            add(zip, "docProps/app.xml", appXml());
            add(zip, "docProps/core.xml", coreXml());
            add(zip, "word/document.xml", documentXml);
            add(zip, "word/_rels/document.xml.rels", documentRelsXml(state.relationships));
            add(zip, "word/styles.xml", stylesXml());
            add(zip, "word/numbering.xml", numberingXml());
            for (Map.Entry<String, byte[]> image : state.imageMedia.entrySet()) {
                add(zip, image.getKey(), image.getValue());
            }
            zip.close();
            return bytes.toByteArray();
        } catch (IOException ex) {
            throw new IllegalStateException("DOCX package creation failed", ex);
        }
    }

    private void add(ZipOutputStream zip, String name, String text) throws IOException {
        add(zip, name, text.getBytes(StandardCharsets.UTF_8));
    }

    private void add(ZipOutputStream zip, String name, byte[] data) throws IOException {
        ZipEntry entry = new ZipEntry(name);
        entry.setTime(0L);
        zip.putNextEntry(entry);
        zip.write(data);
        zip.closeEntry();
    }

    private String paragraphXml(String content, String style) {
        return paragraphXml(content, style, null, 0);
    }

    private String paragraphXml(String content, String style, String numId, int level) {
        String styleXml = style == null ? "" : "<w:pStyle w:val=\"" + escapeAttr(style) + "\"/>";
        String numXml = numId == null ? "" : "<w:numPr><w:ilvl w:val=\"" + level + "\"/><w:numId w:val=\"" + numId + "\"/></w:numPr>";
        String pPr = styleXml.isEmpty() && numXml.isEmpty() ? "" : "<w:pPr>" + styleXml + numXml + "</w:pPr>";
        return "<w:p>" + pPr + content + "</w:p>";
    }

    private String runXml(String text, RunStyle style) {
        String preserve = text.matches("^\\s.*|.*\\s$|.*\\s{2,}.*") ? " xml:space=\"preserve\"" : "";
        String props = (style.bold ? "<w:b/>" : "") + (style.italic ? "<w:i/>" : "")
                + (style.strike ? "<w:strike/>" : "") + (style.underline ? "<w:u w:val=\"single\"/>" : "")
                + (style.code ? "<w:rStyle w:val=\"CodeChar\"/>" : "");
        String rPr = props.isEmpty() ? "" : "<w:rPr>" + props + "</w:rPr>";
        return "<w:r>" + rPr + "<w:t" + preserve + ">" + escapeXml(text) + "</w:t></w:r>";
    }

    private String tableXml(String rows) {
        return "<w:tbl><w:tblPr><w:tblW w:w=\"0\" w:type=\"auto\"/><w:tblBorders>"
                + "<w:top w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
                + "<w:left w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
                + "<w:bottom w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
                + "<w:right w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
                + "<w:insideH w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
                + "<w:insideV w:val=\"single\" w:sz=\"4\" w:space=\"0\" w:color=\"auto\"/>"
                + "</w:tblBorders></w:tblPr>" + rows + "</w:tbl>";
    }

    private String tableCellXml(String content) {
        return "<w:tc><w:tcPr><w:tcW w:w=\"2400\" w:type=\"dxa\"/></w:tcPr>" + paragraphXml(content, null) + "</w:tc>";
    }

    private String drawingXml(String relId, String alt, int cx, int cy, int docPrId) {
        String escapedAlt = escapeAttr(alt);
        return "<w:r><w:drawing><wp:inline distT=\"0\" distB=\"0\" distL=\"0\" distR=\"0\"><wp:extent cx=\"" + cx + "\" cy=\"" + cy
                + "\"/><wp:effectExtent l=\"0\" t=\"0\" r=\"0\" b=\"0\"/><wp:docPr id=\"" + docPrId + "\" name=\"Image " + docPrId
                + "\" descr=\"" + escapedAlt + "\"/><wp:cNvGraphicFramePr><a:graphicFrameLocks noChangeAspect=\"1\"/></wp:cNvGraphicFramePr><a:graphic><a:graphicData uri=\"http://schemas.openxmlformats.org/drawingml/2006/picture\"><pic:pic><pic:nvPicPr><pic:cNvPr id=\""
                + docPrId + "\" name=\"Image " + docPrId + "\" descr=\"" + escapedAlt
                + "\"/><pic:cNvPicPr/></pic:nvPicPr><pic:blipFill><a:blip r:embed=\"" + relId
                + "\"/><a:stretch><a:fillRect/></a:stretch></pic:blipFill><pic:spPr><a:xfrm><a:off x=\"0\" y=\"0\"/><a:ext cx=\""
                + cx + "\" cy=\"" + cy + "\"/></a:xfrm><a:prstGeom prst=\"rect\"><a:avLst/></a:prstGeom></pic:spPr></pic:pic></a:graphicData></a:graphic></wp:inline></w:drawing></w:r>";
    }

    private String contentTypesXml(Set<String> imagePaths) {
        Set<String> defaults = new LinkedHashSet<String>();
        defaults.add("png");
        defaults.add("jpg");
        defaults.add("jpeg");
        defaults.add("gif");
        defaults.add("webp");
        for (String path : imagePaths) {
            int dot = path.lastIndexOf('.');
            if (dot >= 0) {
                defaults.add(path.substring(dot + 1).toLowerCase(Locale.ROOT));
            }
        }
        StringBuilder imageDefaults = new StringBuilder();
        for (String ext : defaults) {
            imageDefaults.append("<Default Extension=\"").append(escapeAttr(ext)).append("\" ContentType=\"")
                    .append(escapeAttr(contentTypeForExt(ext))).append("\"/>");
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Types xmlns=\"http://schemas.openxmlformats.org/package/2006/content-types\">"
                + "<Default Extension=\"rels\" ContentType=\"application/vnd.openxmlformats-package.relationships+xml\"/>"
                + "<Default Extension=\"xml\" ContentType=\"application/xml\"/>" + imageDefaults
                + "<Override PartName=\"/word/document.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml\"/>"
                + "<Override PartName=\"/word/styles.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml\"/>"
                + "<Override PartName=\"/word/numbering.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml\"/>"
                + "<Override PartName=\"/docProps/core.xml\" ContentType=\"application/vnd.openxmlformats-package.core-properties+xml\"/>"
                + "<Override PartName=\"/docProps/app.xml\" ContentType=\"application/vnd.openxmlformats-officedocument.extended-properties+xml\"/>"
                + "</Types>";
    }

    private String rootRelsXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">"
                + "<Relationship Id=\"rId1\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument\" Target=\"word/document.xml\"/>"
                + "</Relationships>";
    }

    private String documentRelsXml(List<Relationship> relationships) {
        StringBuilder rels = new StringBuilder();
        rels.append("<Relationship Id=\"rIdStyles\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/styles\" Target=\"styles.xml\"/>");
        rels.append("<Relationship Id=\"rIdNumbering\" Type=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships/numbering\" Target=\"numbering.xml\"/>");
        for (Relationship rel : relationships) {
            String mode = rel.targetMode == null ? "" : " TargetMode=\"" + escapeAttr(rel.targetMode) + "\"";
            rels.append("<Relationship Id=\"").append(rel.id).append("\" Type=\"").append(escapeAttr(rel.type)).append("\" Target=\"")
                    .append(escapeAttr(rel.target)).append("\"").append(mode).append("/>");
        }
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Relationships xmlns=\"http://schemas.openxmlformats.org/package/2006/relationships\">" + rels + "</Relationships>";
    }

    private String appXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\" xmlns:vt=\"http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes\"><Application>miku-md2docx</Application></Properties>";
    }

    private String coreXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><dc:creator>miku-md2docx</dc:creator><cp:lastModifiedBy>miku-md2docx</cp:lastModifiedBy></cp:coreProperties>";
    }

    private String stylesXml() {
        StringBuilder styles = new StringBuilder();
        styles.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">");
        styles.append("<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/></w:style>");
        for (int i = 1; i <= 6; i++) {
            styles.append("<w:style w:type=\"paragraph\" w:styleId=\"Heading").append(i).append("\"><w:name w:val=\"heading ").append(i)
                    .append("\"/><w:basedOn w:val=\"Normal\"/><w:pPr><w:outlineLvl w:val=\"").append(i - 1)
                    .append("\"/></w:pPr><w:rPr><w:b/></w:rPr></w:style>");
        }
        styles.append("<w:style w:type=\"paragraph\" w:styleId=\"Quote\"><w:name w:val=\"Quote\"/><w:basedOn w:val=\"Normal\"/><w:pPr><w:ind w:left=\"720\"/></w:pPr><w:rPr><w:i/></w:rPr></w:style>");
        styles.append("<w:style w:type=\"paragraph\" w:styleId=\"Code\"><w:name w:val=\"Code\"/><w:basedOn w:val=\"Normal\"/><w:rPr><w:rFonts w:ascii=\"Courier New\" w:hAnsi=\"Courier New\"/></w:rPr></w:style>");
        styles.append("<w:style w:type=\"paragraph\" w:styleId=\"Separator\"><w:name w:val=\"Separator\"/><w:basedOn w:val=\"Normal\"/><w:pPr><w:pBdr><w:bottom w:val=\"single\" w:sz=\"6\" w:space=\"1\" w:color=\"auto\"/></w:pBdr></w:pPr></w:style>");
        styles.append("<w:style w:type=\"character\" w:styleId=\"CodeChar\"><w:name w:val=\"Code Char\"/><w:rPr><w:rFonts w:ascii=\"Courier New\" w:hAnsi=\"Courier New\"/></w:rPr></w:style>");
        styles.append("</w:styles>");
        return styles.toString();
    }

    private String numberingXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><w:numbering xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:abstractNum w:abstractNumId=\"0\"><w:multiLevelType w:val=\"hybridMultilevel\"/><w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"bullet\"/><w:lvlText w:val=\"•\"/><w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:hint=\"default\"/></w:rPr></w:lvl><w:lvl w:ilvl=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"bullet\"/><w:lvlText w:val=\"•\"/><w:pPr><w:ind w:left=\"1440\" w:hanging=\"360\"/></w:pPr><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:hint=\"default\"/></w:rPr></w:lvl><w:lvl w:ilvl=\"2\"><w:start w:val=\"1\"/><w:numFmt w:val=\"bullet\"/><w:lvlText w:val=\"•\"/><w:pPr><w:ind w:left=\"2160\" w:hanging=\"360\"/></w:pPr><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:hint=\"default\"/></w:rPr></w:lvl></w:abstractNum>"
                + "<w:abstractNum w:abstractNumId=\"1\"><w:multiLevelType w:val=\"hybridMultilevel\"/><w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%1.\"/><w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%2.\"/><w:pPr><w:ind w:left=\"1440\" w:hanging=\"360\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"2\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%3.\"/><w:pPr><w:ind w:left=\"2160\" w:hanging=\"360\"/></w:pPr></w:lvl></w:abstractNum>"
                + "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num><w:num w:numId=\"2\"><w:abstractNumId w:val=\"1\"/></w:num></w:numbering>";
    }

    private String safeMediaName(int index, String path) {
        String file = path.replace('\\', '/');
        int slash = file.lastIndexOf('/');
        if (slash >= 0) {
            file = file.substring(slash + 1);
        }
        String ext = "bin";
        int dot = file.lastIndexOf('.');
        if (dot >= 0) {
            ext = file.substring(dot + 1).toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9]", "");
        }
        if (ext.isEmpty()) {
            ext = "bin";
        }
        return "image-" + index + "." + ext;
    }

    private String contentTypeForExt(String ext) {
        if ("jpg".equals(ext) || "jpeg".equals(ext)) {
            return "image/jpeg";
        }
        if ("gif".equals(ext)) {
            return "image/gif";
        }
        if ("webp".equals(ext)) {
            return "image/webp";
        }
        if ("png".equals(ext)) {
            return "image/png";
        }
        return "application/octet-stream";
    }

    private ImageSize displaySizeForImage(byte[] data) {
        ImageSize natural = imageSize(data);
        int naturalWidth = (natural == null ? 320 : natural.width) * EMU_PER_PIXEL_AT_96_DPI;
        int naturalHeight = (natural == null ? 240 : natural.height) * EMU_PER_PIXEL_AT_96_DPI;
        int width = Math.min(naturalWidth, DOC_BODY_WIDTH_EMU);
        int height = (int) Math.round((double) width * naturalHeight / naturalWidth);
        return new ImageSize(width, height, width < naturalWidth);
    }

    private ImageSize imageSize(byte[] data) {
        if (data.length >= 24 && data[0] == (byte) 0x89 && data[1] == 0x50 && data[2] == 0x4e && data[3] == 0x47) {
            return new ImageSize(readBe32(data, 16), readBe32(data, 20), false);
        }
        if (data.length >= 10 && data[0] == 0x47 && data[1] == 0x49 && data[2] == 0x46) {
            return new ImageSize(readLe16(data, 6), readLe16(data, 8), false);
        }
        if (data.length >= 4 && data[0] == (byte) 0xff && data[1] == (byte) 0xd8) {
            int offset = 2;
            while (offset + 9 <= data.length) {
                if (data[offset] != (byte) 0xff) {
                    return null;
                }
                int marker = data[offset + 1] & 0xff;
                int length = readBe16(data, offset + 2);
                if (marker >= 0xc0 && marker <= 0xc3) {
                    return new ImageSize(readBe16(data, offset + 7), readBe16(data, offset + 5), false);
                }
                offset += 2 + length;
            }
        }
        return null;
    }

    private int readBe16(byte[] data, int offset) {
        return ((data[offset] & 0xff) << 8) | (data[offset + 1] & 0xff);
    }

    private int readLe16(byte[] data, int offset) {
        return (data[offset] & 0xff) | ((data[offset + 1] & 0xff) << 8);
    }

    private int readBe32(byte[] data, int offset) {
        return ((data[offset] & 0xff) << 24) | ((data[offset + 1] & 0xff) << 16) | ((data[offset + 2] & 0xff) << 8) | (data[offset + 3] & 0xff);
    }

    private String escapeXml(String text) {
        return text.replace("&", "&amp;").replace("<", "&lt;").replace(">", "&gt;");
    }

    private String escapeAttr(String text) {
        return escapeXml(text).replace("\"", "&quot;");
    }

    private String stripHtml(String text) {
        return text.replaceAll("<[^>]*>", "");
    }

    private static class RunStyle {
        boolean bold;
        boolean italic;
        boolean strike;
        boolean underline;
        boolean code;

        RunStyle copy() {
            RunStyle copy = new RunStyle();
            copy.bold = bold;
            copy.italic = italic;
            copy.strike = strike;
            copy.underline = underline;
            copy.code = code;
            return copy;
        }
    }

    private static class Relationship {
        final String id;
        final String type;
        final String target;
        final String targetMode;

        Relationship(String id, String type, String target, String targetMode) {
            this.id = id;
            this.type = type;
            this.target = target;
            this.targetMode = targetMode;
        }
    }

    private static class ImageSize {
        final int width;
        final int height;
        final boolean resized;

        ImageSize(int width, int height, boolean resized) {
            this.width = width;
            this.height = height;
            this.resized = resized;
        }
    }

    private static class RenderState {
        final Md2DocxOptions options;
        final Md2DocxSummary summary = new Md2DocxSummary();
        final List<Relationship> relationships = new ArrayList<Relationship>();
        final Map<String, byte[]> imageMedia = new LinkedHashMap<String, byte[]>();
        final Map<String, List<String>> headingBookmarks = new LinkedHashMap<String, List<String>>();
        final Map<String, Integer> headingRenderCounts = new HashMap<String, Integer>();
        final Set<String> knownBookmarks = new LinkedHashSet<String>();
        int nextRelId = 1;
        int nextDocPrId = 1;

        RenderState(Md2DocxOptions options) {
            this.options = options;
        }

        String nextHeadingBookmark(String headingText) {
            List<String> candidates = headingBookmarks.get(headingText);
            if (candidates == null || candidates.isEmpty()) {
                return null;
            }
            Integer index = headingRenderCounts.get(headingText);
            int current = index == null ? 0 : index.intValue();
            headingRenderCounts.put(headingText, current + 1);
            return current < candidates.size() ? candidates.get(current) : candidates.get(candidates.size() - 1);
        }

        String addRelationship(String type, String target, String targetMode) {
            String id = "rId" + nextRelId++;
            relationships.add(new Relationship(id, type, target, targetMode));
            return id;
        }
    }
}
