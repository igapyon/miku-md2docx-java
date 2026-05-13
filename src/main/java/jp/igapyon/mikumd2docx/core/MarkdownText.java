package jp.igapyon.mikumd2docx.core;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class MarkdownText {
    private static final Pattern ENTITY = Pattern.compile("&(#x[0-9A-Fa-f]+|#[0-9]+|[A-Za-z][A-Za-z0-9]+);");
    private static final String ESCAPABLE_PUNCTUATION = "\\`*{}[]<>()#+-.!_|~";

    private MarkdownText() {
    }

    static String plainInlineText(String text) {
        String plain = XmlUtils.stripHtml(text).replaceAll("!\\[([^]]*)]\\(([^)]+)\\)", "$1")
                .replaceAll("\\[([^]]+)]\\(([^)]+)\\)", "$1")
                .replace("**", "").replace("__", "").replace("*", "").replace("_", "")
                .replace("~~", "").replace("`", "").trim();
        return normalizeInlineText(plain);
    }

    static String normalizeAnchor(String value) {
        return value.trim().toLowerCase(Locale.ROOT).replaceAll("\\s+", "-")
                .replaceAll("[^a-z0-9\\-_]+", "-").replaceAll("-+", "-").replaceAll("^-|-$", "");
    }

    static String normalizeReferenceLabel(String value) {
        return value.trim().replaceAll("\\s+", " ").toLowerCase(Locale.ROOT);
    }

    static String normalizeInlineText(String text) {
        return decodeHtmlEntities(unescapeMarkdownPunctuation(text));
    }

    private static String unescapeMarkdownPunctuation(String text) {
        StringBuilder out = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char current = text.charAt(i);
            if (current == '\\' && i + 1 < text.length()
                    && ESCAPABLE_PUNCTUATION.indexOf(text.charAt(i + 1)) >= 0) {
                out.append(text.charAt(i + 1));
                i++;
            } else {
                out.append(current);
            }
        }
        return out.toString();
    }

    private static String decodeHtmlEntities(String text) {
        Matcher matcher = ENTITY.matcher(text);
        StringBuffer out = new StringBuffer();
        while (matcher.find()) {
            String decoded = decodeEntity(matcher.group(1));
            if (decoded == null) {
                matcher.appendReplacement(out, Matcher.quoteReplacement(matcher.group()));
            } else {
                matcher.appendReplacement(out, Matcher.quoteReplacement(decoded));
            }
        }
        matcher.appendTail(out);
        return out.toString();
    }

    private static String decodeEntity(String entity) {
        if (entity.startsWith("#x") || entity.startsWith("#X")) {
            return codePointToString(entity.substring(2), 16);
        }
        if (entity.startsWith("#")) {
            return codePointToString(entity.substring(1), 10);
        }
        String name = entity.toLowerCase(Locale.ROOT);
        if ("amp".equals(name)) {
            return "&";
        }
        if ("lt".equals(name)) {
            return "<";
        }
        if ("gt".equals(name)) {
            return ">";
        }
        if ("quot".equals(name)) {
            return "\"";
        }
        if ("apos".equals(name)) {
            return "'";
        }
        if ("copy".equals(name)) {
            return "\u00a9";
        }
        return null;
    }

    private static String codePointToString(String value, int radix) {
        try {
            int codePoint = Integer.parseInt(value, radix);
            if (!Character.isValidCodePoint(codePoint)) {
                return null;
            }
            return new String(Character.toChars(codePoint));
        } catch (NumberFormatException ex) {
            return null;
        }
    }
}
