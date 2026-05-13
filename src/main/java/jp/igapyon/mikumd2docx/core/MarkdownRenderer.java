package jp.igapyon.mikumd2docx.core;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class MarkdownRenderer {
    private static final Pattern DEFINITION = Pattern.compile("^\\s{0,3}\\[([^]]+)]\\s*:\\s*\\S+.*$");

    private MarkdownRenderer() {
    }

    static String renderMarkdown(String markdown, RenderState state) {
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
            if (isCodeFence(trimmed)) {
                if (inCode) {
                    MarkdownBlockRenderer.renderCodeBlock(body, codeLines);
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
            if (isDefinitionLine(line)) {
                continue;
            }
            Matcher heading = MarkdownBlockRenderer.HEADING.matcher(trimmed);
            if (heading.matches()) {
                MarkdownBlockRenderer.renderHeading(body, heading.group(1).length(), heading.group(2), state);
            } else if (isSetextHeading(lines, index)) {
                MarkdownBlockRenderer.renderHeading(body, MarkdownBlockRenderer.setextHeadingLevel(lines[index + 1]), trimmed, state);
                index++;
            } else if (isIndentedCodeLine(line)) {
                CodeScan code = collectIndentedCode(lines, index);
                state.summary.codeBlocks++;
                MarkdownBlockRenderer.renderCodeBlock(body, code.lines);
                index = code.endIndex;
            } else if (MarkdownBlockRenderer.isTableStart(lines, index)) {
                index = MarkdownBlockRenderer.renderTable(body, lines, index, state);
            } else if (MarkdownBlockRenderer.isListLine(line)) {
                index = MarkdownBlockRenderer.renderList(body, lines, index, state);
            } else if (trimmed.startsWith(">")) {
                index = MarkdownBlockRenderer.renderBlockquote(body, lines, index, state);
            } else if (trimmed.matches("^(-{3,}|\\*{3,}|_{3,})$")) {
                MarkdownBlockRenderer.renderHorizontalRule(body, state);
            } else if (MarkdownBlockRenderer.isUnsupportedHtmlBlock(trimmed)) {
                MarkdownBlockRenderer.renderUnsupportedHtmlBlock(body, trimmed, state);
            } else {
                ParagraphScan paragraph = collectParagraph(lines, index);
                MarkdownBlockRenderer.renderParagraph(body, paragraph.text, state);
                index = paragraph.endIndex;
            }
        }
        if (inCode) {
            MarkdownBlockRenderer.renderCodeBlock(body, codeLines);
        }
        return body.toString();
    }

    static void collectHeadingBookmarks(String markdown, RenderState state) {
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
            Matcher heading = MarkdownBlockRenderer.HEADING.matcher(trimmed);
            String text;
            if (heading.matches()) {
                text = MarkdownText.plainInlineText(heading.group(2));
            } else if (isSetextHeading(lines, i)) {
                text = MarkdownText.plainInlineText(trimmed);
                i++;
            } else {
                continue;
            }
            String base = MarkdownText.normalizeAnchor(text);
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

    static void collectReferenceDefinitions(String markdown, RenderState state) {
        String[] lines = markdown.replace("\r\n", "\n").replace('\r', '\n').split("\n", -1);
        for (String line : lines) {
            Matcher definition = DEFINITION.matcher(line);
            if (definition.matches()) {
                state.referenceDefinitions.add(MarkdownText.normalizeReferenceLabel(definition.group(1)));
            }
        }
    }

    private static boolean isSetextHeading(String[] lines, int index) {
        return index + 1 < lines.length && !lines[index].trim().isEmpty()
                && MarkdownBlockRenderer.isSetextHeadingUnderline(lines[index + 1]);
    }

    private static ParagraphScan collectParagraph(String[] lines, int start) {
        StringBuilder text = new StringBuilder();
        int index = start;
        while (index < lines.length) {
            String line = lines[index];
            String trimmed = line.trim();
            if (trimmed.isEmpty()) {
                break;
            }
            if (isDefinitionLine(line)) {
                break;
            }
            if (index > start && isBlockStart(lines, index)) {
                break;
            }
            if (text.length() > 0 && !endsWithHardBreakToken(text)) {
                text.append('\n');
            }
            if (line.endsWith("\\")) {
                text.append(line.substring(0, line.length() - 1));
                text.append("<br>");
            } else if (line.matches(".* {2,}$")) {
                text.append(line.replaceFirst(" {2,}$", ""));
                text.append("<br>");
            } else {
                text.append(trimmed);
            }
            index++;
        }
        return new ParagraphScan(text.toString(), index - 1);
    }

    private static boolean endsWithHardBreakToken(StringBuilder text) {
        int length = text.length();
        return length >= 4 && "<br>".contentEquals(text.subSequence(length - 4, length));
    }

    private static boolean isBlockStart(String[] lines, int index) {
        String line = lines[index];
        String trimmed = line.trim();
        return MarkdownBlockRenderer.HEADING.matcher(trimmed).matches()
                || isSetextHeading(lines, index)
                || isDefinitionLine(line)
                || isIndentedCodeLine(line)
                || MarkdownBlockRenderer.isTableStart(lines, index)
                || MarkdownBlockRenderer.isListLine(line)
                || trimmed.startsWith(">")
                || isCodeFence(trimmed)
                || trimmed.matches("^(-{3,}|\\*{3,}|_{3,})$")
                || MarkdownBlockRenderer.isUnsupportedHtmlBlock(trimmed);
    }

    private static boolean isCodeFence(String trimmed) {
        return trimmed.startsWith("```") || trimmed.startsWith("~~~");
    }

    private static boolean isDefinitionLine(String line) {
        return DEFINITION.matcher(line).matches();
    }

    private static boolean isIndentedCodeLine(String line) {
        return line.startsWith("    ") || line.startsWith("\t");
    }

    private static CodeScan collectIndentedCode(String[] lines, int start) {
        List<String> codeLines = new ArrayList<String>();
        int index = start;
        while (index < lines.length) {
            String line = lines[index];
            if (line.trim().isEmpty()) {
                break;
            }
            if (!isIndentedCodeLine(line)) {
                break;
            }
            codeLines.add(stripCodeIndent(line));
            index++;
        }
        return new CodeScan(codeLines, index - 1);
    }

    private static String stripCodeIndent(String line) {
        if (line.startsWith("\t")) {
            return line.substring(1);
        }
        return line.length() >= 4 ? line.substring(4) : "";
    }

    private static final class ParagraphScan {
        final String text;
        final int endIndex;

        ParagraphScan(String text, int endIndex) {
            this.text = text;
            this.endIndex = endIndex;
        }
    }

    private static final class CodeScan {
        final List<String> lines;
        final int endIndex;

        CodeScan(List<String> lines, int endIndex) {
            this.lines = lines;
            this.endIndex = endIndex;
        }
    }
}
