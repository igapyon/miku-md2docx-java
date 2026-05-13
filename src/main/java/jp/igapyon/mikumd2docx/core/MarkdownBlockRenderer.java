package jp.igapyon.mikumd2docx.core;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class MarkdownBlockRenderer {
    static final Pattern HEADING = Pattern.compile("^(#{1,6})\\s+(.+)$");
    private static final Pattern LIST = Pattern.compile("^(\\s*)([-*+]|\\d+\\.)\\s+(.+)$");

    private MarkdownBlockRenderer() {
    }

    static boolean isListLine(String line) {
        return LIST.matcher(line).matches();
    }

    static boolean isSetextHeadingUnderline(String line) {
        return line.trim().matches("^(=+|-+)\\s*$");
    }

    static int setextHeadingLevel(String line) {
        return line.trim().startsWith("=") ? 1 : 2;
    }

    static void renderHeading(StringBuilder body, int level, String text, RenderState state) {
        state.summary.headings++;
        String plain = MarkdownText.plainInlineText(text);
        String bookmark = state.nextHeadingBookmark(plain);
        String bookmarkXml = "";
        if (bookmark != null) {
            bookmarkXml = "<w:bookmarkStart w:id=\"" + state.summary.headings + "\" w:name=\"" + XmlUtils.escapeAttr(bookmark) + "\"/>";
        }
        String bookmarkEnd = bookmark == null ? "" : "<w:bookmarkEnd w:id=\"" + state.summary.headings + "\"/>";
        body.append(OoxmlPrimitives.paragraphXml(bookmarkXml + InlineRenderer.renderInline(text, state) + bookmarkEnd, "Heading" + Math.min(Math.max(level, 1), 6)));
    }

    static int renderList(StringBuilder body, String[] lines, int start, RenderState state) {
        int index = start;
        Set<String> seenListKeys = new LinkedHashSet<String>();
        while (index < lines.length) {
            Matcher matcher = LIST.matcher(lines[index]);
            if (!matcher.matches()) {
                if (lines[index].trim().isEmpty()) {
                    int next = nextNonBlankLine(lines, index + 1);
                    if (next < lines.length && (LIST.matcher(lines[next]).matches() || isIndentedListChild(lines[next]))) {
                        index++;
                        continue;
                    }
                } else if (isIndentedListChild(lines[index])) {
                    index++;
                    continue;
                }
                break;
            }
            String marker = matcher.group(2);
            String value = matcher.group(3);
            int level = Math.min(matcher.group(1).length() / 2, 2);
            boolean ordered = marker.endsWith(".");
            String listKey = level + ":" + ordered;
            if (!seenListKeys.contains(listKey)) {
                state.summary.lists++;
                seenListKeys.add(listKey);
            }
            state.summary.listItems++;
            Matcher task = Pattern.compile("^\\[( |x|X)]\\s+(.+)$").matcher(value);
            String itemXml;
            if (task.matches()) {
                itemXml = OoxmlPrimitives.runXml("[" + task.group(1).toLowerCase(Locale.ROOT) + "] ", new RunStyle())
                        + InlineRenderer.renderInline(task.group(2), state);
            } else {
                itemXml = InlineRenderer.renderInline(value, state);
            }
            body.append(OoxmlPrimitives.paragraphXml(itemXml, null, ordered ? "2" : "1", level));
            index++;
        }
        return index - 1;
    }

    private static int nextNonBlankLine(String[] lines, int start) {
        int index = start;
        while (index < lines.length && lines[index].trim().isEmpty()) {
            index++;
        }
        return index;
    }

    private static boolean isIndentedListChild(String line) {
        return line.startsWith("  ") || line.startsWith("\t");
    }

    static boolean isTableStart(String[] lines, int index) {
        if (index + 1 >= lines.length) {
            return false;
        }
        return lines[index].trim().startsWith("|") && lines[index].trim().endsWith("|")
                && lines[index + 1].trim().matches("^\\|?\\s*:?-{3,}:?\\s*(\\|\\s*:?-{3,}:?\\s*)+\\|?$");
    }

    static int renderTable(StringBuilder body, String[] lines, int start, RenderState state) {
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
            List<String> cells = splitTableCells(trimmed.substring(1, trimmed.length() - 1));
            StringBuilder cellXml = new StringBuilder();
            for (String cell : cells) {
                RunStyle style = new RunStyle();
                style.bold = rowIndex == 0;
                cellXml.append(OoxmlPrimitives.tableCellXml(InlineRenderer.renderInline(cell.trim(), state, style)));
            }
            rows.append("<w:tr>").append(cellXml).append("</w:tr>");
            rowIndex++;
            index++;
        }
        body.append(OoxmlPrimitives.tableXml(rows.toString()));
        return index - 1;
    }

    private static List<String> splitTableCells(String text) {
        java.util.ArrayList<String> cells = new java.util.ArrayList<String>();
        StringBuilder cell = new StringBuilder();
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == '|' && !isEscaped(text, i)) {
                cells.add(cell.toString());
                cell.setLength(0);
            } else {
                cell.append(current);
            }
        }
        cells.add(cell.toString());
        return cells;
    }

    private static boolean isEscaped(String text, int index) {
        int backslashes = 0;
        for (int i = index - 1; i >= 0 && text.charAt(i) == '\\'; i--) {
            backslashes++;
        }
        return backslashes % 2 == 1;
    }

    static int renderBlockquote(StringBuilder body, String[] lines, int start, RenderState state) {
        state.summary.blockquotes++;
        StringBuilder paragraph = new StringBuilder();
        int index = start;
        boolean inQuotedCodeFence = false;
        while (index < lines.length) {
            String trimmed = lines[index].trim();
            if (!trimmed.startsWith(">")) {
                break;
            }
            String quoteText = stripQuotePrefixes(trimmed);
            if (quoteText.trim().isEmpty()) {
                flushQuoteParagraph(body, paragraph, state);
                index++;
                continue;
            }
            String quoteTrimmed = quoteText.trim();
            if (quoteTrimmed.startsWith("```") || quoteTrimmed.startsWith("~~~")) {
                flushQuoteParagraph(body, paragraph, state);
                inQuotedCodeFence = !inQuotedCodeFence;
                index++;
                continue;
            }
            if (inQuotedCodeFence || isListLine(quoteText) || quoteText.startsWith("    ") || quoteText.startsWith("\t")) {
                flushQuoteParagraph(body, paragraph, state);
                index++;
                continue;
            }
            if (paragraph.length() > 0) {
                paragraph.append('\n');
            }
            paragraph.append(quoteTrimmed);
            index++;
        }
        flushQuoteParagraph(body, paragraph, state);
        return index - 1;
    }

    static void renderHorizontalRule(StringBuilder body, RenderState state) {
        state.summary.horizontalRules++;
        body.append(OoxmlPrimitives.paragraphXml(OoxmlPrimitives.runXml("----------", new RunStyle()), "Separator"));
    }

    static void renderUnsupportedHtmlBlock(StringBuilder body, String trimmed, RenderState state) {
        state.summary.paragraphs++;
        countUnsupportedHtml(trimmed, state);
        body.append(OoxmlPrimitives.paragraphXml(OoxmlPrimitives.runXml(XmlUtils.stripHtml(trimmed), new RunStyle()), null));
    }

    static void renderParagraph(StringBuilder body, String trimmed, RenderState state) {
        state.summary.paragraphs++;
        String inline = InlineRenderer.renderInline(trimmed, state);
        String style = trimmed.matches("(?i)<br\\s*/?>") ? null : "Normal";
        body.append(inline.isEmpty()
                ? OoxmlPrimitives.paragraphXml(OoxmlPrimitives.runXml("", new RunStyle()), null)
                : OoxmlPrimitives.paragraphXml(inline, style));
    }

    static void renderCodeBlock(StringBuilder body, List<String> codeLines) {
        for (String codeLine : codeLines) {
            RunStyle style = new RunStyle();
            style.code = true;
            body.append(OoxmlPrimitives.paragraphXml(OoxmlPrimitives.runXml(codeLine, style), "Code"));
        }
    }

    static boolean isUnsupportedHtmlBlock(String text) {
        if (!text.startsWith("<") || !text.endsWith(">")) {
            return false;
        }
        Matcher tag = Pattern.compile("^<([a-zA-Z][a-zA-Z0-9-]*)(\\s+[^>]*)?>").matcher(text);
        if (!tag.find()) {
            return false;
        }
        String name = tag.group(1).toLowerCase(Locale.ROOT);
        return !"br".equals(name) && !"ins".equals(name) && !"a".equals(name) && !"img".equals(name);
    }

    private static void countUnsupportedHtml(String text, RenderState state) {
        Matcher tag = Pattern.compile("<\\/?([a-zA-Z][a-zA-Z0-9-]*)(\\s+[^>]*)?>").matcher(text);
        while (tag.find()) {
            if (tag.group().startsWith("</")) {
                continue;
            }
            String name = tag.group(1).toLowerCase(Locale.ROOT);
            if (!"br".equals(name) && !"ins".equals(name) && !"a".equals(name) && !"img".equals(name)) {
                state.summary.unsupportedHtml++;
            }
        }
    }

    private static String stripQuotePrefixes(String text) {
        String current = text;
        while (current.startsWith(">")) {
            current = current.substring(1);
            if (current.startsWith(" ")) {
                current = current.substring(1);
            }
        }
        return current;
    }

    private static void flushQuoteParagraph(StringBuilder body, StringBuilder paragraph, RenderState state) {
        if (paragraph.length() == 0) {
            return;
        }
        state.summary.paragraphs++;
        body.append(OoxmlPrimitives.paragraphXml(InlineRenderer.renderInline(paragraph.toString(), state), "Quote"));
        paragraph.setLength(0);
    }
}
