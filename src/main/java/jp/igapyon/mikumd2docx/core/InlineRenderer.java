package jp.igapyon.mikumd2docx.core;

import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

final class InlineRenderer {
    private static final Pattern IMAGE = Pattern.compile("(?<!\\\\)!\\[([^]]*)]\\(([^)]+)\\)");
    private static final Pattern REFERENCE_IMAGE = Pattern.compile("(?<!\\\\)!\\[([^]]*)]\\[([^]]*)]");
    private static final Pattern LINK = Pattern.compile("(?<!!)(?<!\\\\)\\[([^]]+)]\\(([^)]+)\\)");
    private static final Pattern REFERENCE_LINK = Pattern.compile("(?<!!)(?<!\\\\)\\[([^]]+)]\\[([^]]*)]");
    private static final Pattern SHORTCUT_REFERENCE = Pattern.compile("(?<!!)(?<!\\\\)\\[([^]]+)]");
    private static final Pattern AUTOLINK = Pattern.compile("(?i)(https?://[A-Za-z0-9/?#@!$&'()*+,;=%._~:-]*[A-Za-z0-9/#@&=_~-]|www\\.[A-Za-z0-9/?#@!$&'()*+,;=%._~:-]*[A-Za-z0-9/#@&=_~-]|[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,})");
    private static final Pattern TOKEN = Pattern.compile("(?<!\\\\)!\\[[^]]*]\\([^)]+\\)|(?<!\\\\)!\\[[^]]*]\\[[^]]*]|(?<!!)(?<!\\\\)\\[[^]]+]\\([^)]+\\)|(?<!!)(?<!\\\\)\\[[^]]+]\\[[^]]*]|(?<!!)(?<!\\\\)\\[[^]]+]|<br\\s*/?>|<ins>.*?</ins>|<a\\s+[^>]*href=[\"'][^\"']+[\"'][^>]*>.*?</a>|<img\\s+[^>]*>|https?://[A-Za-z0-9/?#@!$&'()*+,;=%._~:-]*[A-Za-z0-9/#@&=_~-]|www\\.[A-Za-z0-9/?#@!$&'()*+,;=%._~:-]*[A-Za-z0-9/#@&=_~-]|[A-Za-z0-9._%+-]+@[A-Za-z0-9.-]+\\.[A-Za-z]{2,}", Pattern.CASE_INSENSITIVE);

    private InlineRenderer() {
    }

    static String renderInline(String text, RenderState state) {
        return renderInline(text, state, new RunStyle());
    }

    static String renderInline(String text, RenderState state, RunStyle inherited) {
        StringBuilder xml = new StringBuilder();
        int index = 0;
        Matcher token = TOKEN.matcher(text);
        while (token.find()) {
            appendStyledText(xml, text.substring(index, token.start()), inherited, state);
            xml.append(renderToken(token.group(), state, inherited));
            index = token.end();
        }
        appendStyledText(xml, text.substring(index), inherited, state);
        return xml.toString();
    }

    private static String renderToken(String token, RenderState state, RunStyle inherited) {
        Matcher image = IMAGE.matcher(token);
        if (image.matches()) {
            return renderImage(markdownDestination(image.group(2)), image.group(1), state);
        }
        Matcher referenceImage = REFERENCE_IMAGE.matcher(token);
        if (referenceImage.matches()) {
            String label = referenceImage.group(2).isEmpty() ? referenceImage.group(1) : referenceImage.group(2);
            if (state.referenceDefinitions.contains(MarkdownText.normalizeReferenceLabel(label))) {
                return "";
            }
            return OoxmlPrimitives.runXml(token, inherited);
        }
        Matcher link = LINK.matcher(token);
        if (link.matches()) {
            return renderLink(markdownDestination(link.group(2)), renderInline(link.group(1), state, inherited), MarkdownText.plainInlineText(link.group(1)), state);
        }
        Matcher referenceLink = REFERENCE_LINK.matcher(token);
        if (referenceLink.matches()) {
            String label = referenceLink.group(2).isEmpty() ? referenceLink.group(1) : referenceLink.group(2);
            if (state.referenceDefinitions.contains(MarkdownText.normalizeReferenceLabel(label))) {
                return renderInline(referenceLink.group(1), state, inherited);
            }
            return OoxmlPrimitives.runXml(token, inherited);
        }
        Matcher shortcutReference = SHORTCUT_REFERENCE.matcher(token);
        if (shortcutReference.matches()) {
            String label = shortcutReference.group(1);
            if (state.referenceDefinitions.contains(MarkdownText.normalizeReferenceLabel(label))) {
                return renderInline(label, state, inherited);
            }
            return OoxmlPrimitives.runXml(token, inherited);
        }
        if (AUTOLINK.matcher(token).matches()) {
            String url = token;
            if (token.regionMatches(true, 0, "www.", 0, 4)) {
                url = "http://" + token;
            } else if (token.indexOf('@') >= 0 && !token.regionMatches(true, 0, "http", 0, 4)) {
                url = "mailto:" + token;
            }
            return renderLink(url, OoxmlPrimitives.runXml(token, inherited), token, state);
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
            return renderLink(htmlLink.group(1), renderInline(XmlUtils.stripHtml(htmlLink.group(2)), state, inherited), XmlUtils.stripHtml(htmlLink.group(2)), state);
        }
        Matcher htmlImage = Pattern.compile("(?i)<img\\s+[^>]*src=[\"']([^\"']+)[\"'][^>]*>").matcher(token);
        if (htmlImage.matches()) {
            Matcher alt = Pattern.compile("(?i)\\salt=[\"']([^\"']*)[\"']").matcher(token);
            return renderImage(htmlImage.group(1), alt.find() ? alt.group(1) : "", state);
        }
        state.summary.unsupportedHtml++;
        return OoxmlPrimitives.runXml(XmlUtils.stripHtml(token), inherited);
    }

    private static void appendStyledText(StringBuilder xml, String text, RunStyle inherited, RenderState state) {
        if (text.isEmpty()) {
            return;
        }
        Matcher htmlTag = Pattern.compile("<\\/?([a-zA-Z][a-zA-Z0-9-]*)(\\s+[^>]*)?>").matcher(text);
        if (htmlTag.find()) {
            int index = 0;
            do {
                appendStyledText(xml, text.substring(index, htmlTag.start()), inherited, state);
                String name = htmlTag.group(1).toLowerCase(Locale.ROOT);
                if (!"br".equals(name) && !"ins".equals(name) && !"a".equals(name) && !"img".equals(name)) {
                    if (!htmlTag.group().startsWith("</")) {
                        state.summary.unsupportedHtml++;
                    }
                    xml.append(OoxmlPrimitives.runXml("", inherited));
                }
                index = htmlTag.end();
            } while (htmlTag.find());
            appendStyledText(xml, text.substring(index), inherited, state);
            return;
        }
        int index = 0;
        Matcher marker = Pattern.compile("((?<!\\\\)\\*\\*[^*]+(?<!\\\\)\\*\\*|(?<!\\\\)__[^_]+(?<!\\\\)__|(?<!\\\\)\\*[^*]+(?<!\\\\)\\*|(?<!\\\\)_[^_]+(?<!\\\\)_|(?<!\\\\)~~[^~]+(?<!\\\\)~~|(?<!\\\\)`[^`]+(?<!\\\\)`)").matcher(text);
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

    private static String markdownDestination(String raw) {
        String trimmed = raw.trim();
        if (trimmed.startsWith("<")) {
            int close = trimmed.indexOf('>');
            if (close > 0) {
                return trimmed.substring(1, close);
            }
        }
        for (int i = 0; i < trimmed.length(); i++) {
            char current = trimmed.charAt(i);
            if (Character.isWhitespace(current)) {
                return trimmed.substring(0, i);
            }
        }
        return trimmed;
    }

    private static void appendPlainText(StringBuilder xml, String text, RunStyle style) {
        String cleaned = MarkdownText.normalizeInlineText(text.replaceAll("<[^>]*>", ""));
        if (!cleaned.isEmpty()) {
            xml.append(OoxmlPrimitives.runXml(cleaned, style));
        }
    }

    private static String renderLink(String url, String inlineXml, String text, RenderState state) {
        state.summary.links++;
        if (url.startsWith("#")) {
            state.summary.internalLinks++;
            String anchor = MarkdownText.normalizeAnchor(url.substring(1));
            if (state.knownBookmarks.contains(anchor)) {
                return "<w:hyperlink w:anchor=\"" + XmlUtils.escapeAttr(anchor) + "\" w:history=\"1\">" + inlineXml + "</w:hyperlink>";
            }
            state.summary.unresolvedInternalLinks++;
            return inlineXml;
        }
        state.summary.externalLinks++;
        String relId = state.addRelationship(Relationships.REL_HYPERLINK, url, "External");
        return "<w:hyperlink r:id=\"" + relId + "\" w:history=\"1\">" + inlineXml + "</w:hyperlink>";
    }

    private static String renderImage(String path, String alt, RenderState state) {
        state.summary.images++;
        if (path != null && path.matches("(?i)^[a-z][a-z0-9+.-]*://.*")) {
            state.summary.missingImages++;
            state.summary.remoteImages++;
            state.summary.remoteImageDetails.add(new Md2DocxSummary.RemoteImageDetail(path, alt));
            return OoxmlPrimitives.runXml("[Missing image: " + (alt == null || alt.isEmpty() ? path : alt) + "]", new RunStyle());
        }
        ImageAsset asset = state.options.getImageLoader() == null ? null : state.options.getImageLoader().load(path);
        if (asset == null) {
            state.summary.missingImages++;
            state.summary.missingImageDetails.add(new Md2DocxSummary.MissingImageDetail(path, alt));
            return OoxmlPrimitives.runXml("[Missing image: " + (alt == null || alt.isEmpty() ? path : alt) + "]", new RunStyle());
        }
        String mediaName = ImageAssets.safeMediaName(state.summary.embeddedImages + 1, asset.getPath());
        String mediaPath = "word/media/" + mediaName;
        String relId = state.addRelationship(Relationships.REL_IMAGE, mediaPath.replaceFirst("^word/", ""), null);
        ImageAssets.ImageSize display = ImageAssets.displaySizeForImage(asset.getData());
        if (display.resized) {
            state.summary.resizedImages++;
        }
        state.summary.embeddedImages++;
        state.imageMedia.put(mediaPath, asset.getData());
        return OoxmlPrimitives.drawingXml(relId, alt == null ? "" : alt, display.width, display.height, state.nextDocPrId++);
    }
}
