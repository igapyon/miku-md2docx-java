package jp.igapyon.mikumd2docx;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import jp.igapyon.mikumd2docx.core.Md2DocxResult;
import jp.igapyon.mikumd2docx.core.Md2DocxOptions;
import jp.igapyon.mikumd2docx.core.ImageAsset;
import jp.igapyon.mikumd2docx.core.MikuMd2docxCore;
import jp.igapyon.mikumsofficecore.ZipEntryInput;
import jp.igapyon.mikumsofficecore.ZipPackage;
import org.junit.jupiter.api.Test;

class MikuMd2docxCoreTest {
    @Test
    void createsDocxWithCoreEntriesAndSummary() throws IOException {
        MikuMd2docxCore core = new MikuMd2docxCore();
        Md2DocxResult result = core.convertMarkdownToDocx("# Title\n\nHello [site](https://example.com).");
        Map<String, byte[]> entries = unzip(result.getDocx());

        assertTrue(entries.containsKey("[Content_Types].xml"));
        assertTrue(entries.containsKey("word/document.xml"));
        assertTrue(entries.containsKey("word/styles.xml"));
        assertTrue(entries.containsKey("word/settings.xml"));
        assertTrue(new String(entries.get("word/document.xml"), StandardCharsets.UTF_8).contains("Heading1"));
        assertTrue(new String(entries.get("word/settings.xml"), StandardCharsets.UTF_8).contains("w:val=\"15\""));
        assertTrue(new String(entries.get("word/_rels/document.xml.rels"), StandardCharsets.UTF_8).contains("relationships/settings"));
        assertTrue(new String(entries.get("[Content_Types].xml"), StandardCharsets.UTF_8).contains("/word/settings.xml"));
        assertEquals(1, result.getSummary().headings);
        assertEquals(1, result.getSummary().paragraphs);
        assertEquals(1, result.getSummary().externalLinks);
        assertTrue(new String(entries.get("word/_rels/document.xml.rels"), StandardCharsets.UTF_8).contains("Target=\"https://example.com\" TargetMode=\"External\""));
    }

    @Test
    void preservesSupplementaryUnicodeAndRemovesInvalidXmlCharacters() throws IOException {
        String markdown = "Valid 😀 🐇 𠮷野家; invalid "
                + Character.toString((char) 0xd800)
                + Character.toString((char) 0xfffe)
                + ".";

        Map<String, byte[]> entries = unzip(new MikuMd2docxCore().convertMarkdownToDocx(markdown).getDocx());
        String documentXml = new String(entries.get("word/document.xml"), StandardCharsets.UTF_8);

        assertTrue(documentXml.contains("Valid 😀 🐇 𠮷野家; invalid ."));
        assertFalse(documentXml.contains(Character.toString((char) 0xd800)));
        assertFalse(documentXml.contains(Character.toString((char) 0xfffe)));
    }

    @Test
    void summaryFormatMatchesUpstreamVocabulary() {
        MikuMd2docxCore core = new MikuMd2docxCore();
        Md2DocxResult result = core.convertMarkdownToDocx("![Missing](missing.png)");
        String summary = core.formatSummary(result.getSummary());

        assertTrue(summary.contains("images: 1"));
        assertTrue(summary.contains("missingImages: 1"));
        assertTrue(summary.contains("missingImageDetails:"));
    }

    @Test
    void usesDocxTemplateWhileReplacingDocumentBody() throws IOException {
        MikuMd2docxCore core = new MikuMd2docxCore();
        Map<String, byte[]> baseEntries = unzip(core.convertMarkdownToDocx("Template body").getDocx());
        List<ZipEntryInput> templateEntries = new ArrayList<ZipEntryInput>();
        for (Map.Entry<String, byte[]> entry : baseEntries.entrySet()) {
            templateEntries.add(new ZipEntryInput(entry.getKey(), entry.getValue()));
        }
        String templateDocument = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<w:document xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\"><w:body>"
                + "<w:p><w:r><w:t>Template body</w:t></w:r></w:p>"
                + "<w:sectPr><w:headerReference r:id=\"rIdHeader\" xmlns:r=\"http://schemas.openxmlformats.org/officeDocument/2006/relationships\"/>"
                + "<w:pgSz w:w=\"15840\" w:h=\"12240\" w:orient=\"landscape\"/></w:sectPr></w:body></w:document>";
        String templateStyles = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:style w:type=\"paragraph\" w:styleId=\"TemplateOnly\"><w:name w:val=\"Template Only\"/></w:style></w:styles>";
        String templateSettings = "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<w:settings xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:zoom w:percent=\"90\"/><w:compat><w:compatSetting w:name=\"compatibilityMode\" "
                + "w:uri=\"http://schemas.microsoft.com/office/word\" w:val=\"12\"/></w:compat></w:settings>";
        templateEntries = ZipPackage.upsertZipEntry(templateEntries, new ZipEntryInput("word/document.xml", templateDocument));
        templateEntries = ZipPackage.upsertZipEntry(templateEntries, new ZipEntryInput("word/styles.xml", templateStyles));
        templateEntries = ZipPackage.upsertZipEntry(templateEntries, new ZipEntryInput("word/settings.xml", templateSettings));
        templateEntries.add(new ZipEntryInput("customXml/item1.xml", "<template-marker/>"));

        Md2DocxOptions options = new Md2DocxOptions();
        options.setTemplateDocx(ZipPackage.writeZipPackage(templateEntries));
        Map<String, byte[]> entries = unzip(core.convertMarkdownToDocx("# Generated\n\nText.", options).getDocx());
        String documentXml = new String(entries.get("word/document.xml"), StandardCharsets.UTF_8);
        String stylesXml = new String(entries.get("word/styles.xml"), StandardCharsets.UTF_8);
        String settingsXml = new String(entries.get("word/settings.xml"), StandardCharsets.UTF_8);

        assertTrue(documentXml.contains("Generated"));
        assertFalse(documentXml.contains("Template body"));
        assertTrue(documentXml.contains("w:orient=\"landscape\""));
        assertFalse(documentXml.contains("headerReference"));
        assertTrue(stylesXml.contains("w:styleId=\"TemplateOnly\""));
        assertTrue(stylesXml.contains("w:styleId=\"Heading1\""));
        assertTrue(stylesXml.contains("w:styleId=\"CodeChar\""));
        assertTrue(settingsXml.contains("<w:zoom w:percent=\"90\"/>"));
        assertTrue(settingsXml.contains("w:val=\"15\""));
        assertFalse(settingsXml.contains("w:val=\"12\""));
        assertEquals("<template-marker/>", new String(entries.get("customXml/item1.xml"), StandardCharsets.UTF_8));
    }

    @Test
    void outputIsDeterministicForSameInput() {
        MikuMd2docxCore core = new MikuMd2docxCore();
        byte[] first = core.convertMarkdownToDocx("# Same").getDocx();
        byte[] second = core.convertMarkdownToDocx("# Same").getDocx();

        assertArrayEquals(first, second);
    }

    @Test
    void rendersRepresentativeOoxmlForMarkdownStructures() throws IOException {
        String markdown = String.join("\n",
                "# Target",
                "",
                "Plain **bold** and *italic* with `code`.",
                "",
                "See [target](#target) and [site](https://example.com).",
                "",
                "- first",
                "  1. nested",
                "",
                "| A | B |",
                "| --- | --- |",
                "| **x** | y |",
                "",
                "> quoted",
                "",
                "```js",
                "const value = 1;",
                "```",
                "",
                "---");
        Md2DocxResult result = new MikuMd2docxCore().convertMarkdownToDocx(markdown);
        Map<String, byte[]> entries = unzip(result.getDocx());
        String documentXml = new String(entries.get("word/document.xml"), StandardCharsets.UTF_8);

        assertTrue(documentXml.contains("<w:bookmarkStart"));
        assertTrue(documentXml.contains("<w:hyperlink w:anchor=\"target\""));
        assertTrue(documentXml.contains("<w:b/>"));
        assertTrue(documentXml.contains("<w:i/>"));
        assertTrue(documentXml.contains("<w:rStyle w:val=\"CodeChar\"/>"));
        assertTrue(documentXml.contains("<w:tbl>"));
        assertTrue(documentXml.contains("<w:pStyle w:val=\"Quote\"/>"));
        assertTrue(documentXml.contains("<w:pStyle w:val=\"Code\"/>"));
        assertTrue(documentXml.contains("<w:pStyle w:val=\"Separator\"/>"));
        assertEquals(1, result.getSummary().tables);
        assertEquals(1, result.getSummary().blockquotes);
        assertEquals(1, result.getSummary().codeBlocks);
        assertEquals(1, result.getSummary().horizontalRules);
    }

    @Test
    void embedsProvidedLocalImageBytes() throws IOException {
        Md2DocxOptions options = new Md2DocxOptions();
        final byte[] png = new byte[] {
                (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a,
                0x00, 0x00, 0x00, 0x0d, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x00, 0x10, 0x00, 0x00, 0x00, 0x08
        };
        options.setImageLoader(path -> new ImageAsset(path, png));

        Md2DocxResult result = new MikuMd2docxCore().convertMarkdownToDocx("![Small](small.png)", options);
        Map<String, byte[]> entries = unzip(result.getDocx());
        String documentXml = new String(entries.get("word/document.xml"), StandardCharsets.UTF_8);
        String relsXml = new String(entries.get("word/_rels/document.xml.rels"), StandardCharsets.UTF_8);

        assertTrue(entries.containsKey("word/media/image-1.png"));
        assertTrue(documentXml.contains("descr=\"Small\""));
        assertTrue(documentXml.contains("cx=\"152400\" cy=\"76200\""));
        assertTrue(relsXml.contains("relationships/image"));
        assertEquals(1, result.getSummary().embeddedImages);
        assertEquals(0, result.getSummary().missingImages);
    }

    @Test
    void handlesImageFormatsRemoteImagesAndResizing() throws IOException {
        Md2DocxOptions options = new Md2DocxOptions();
        final byte[] gif = new byte[] {0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x20, 0x00, 0x10, 0x00};
        final byte[] jpeg = new byte[] {(byte) 0xff, (byte) 0xd8, (byte) 0xff, (byte) 0xc0, 0x00, 0x11, 0x08, 0x00, 0x20, 0x00, 0x40, 0x00};
        final byte[] webp = new byte[] {0x52, 0x49, 0x46, 0x46, 0x00, 0x00, 0x00, 0x00, 0x57, 0x45, 0x42, 0x50};
        options.setImageLoader(path -> {
            if (path.endsWith(".gif")) {
                return new ImageAsset(path, gif);
            }
            if (path.endsWith(".jpeg")) {
                return new ImageAsset(path, jpeg);
            }
            if (path.endsWith(".webp")) {
                return new ImageAsset(path, webp);
            }
            if (path.endsWith(".bin")) {
                return new ImageAsset(path, new byte[] {1, 2, 3, 4});
            }
            return null;
        });

        Md2DocxResult result = new MikuMd2docxCore().convertMarkdownToDocx(String.join("\n",
                "![Gif alt](media/sample.gif)",
                "",
                "![J](photo.jpeg)",
                "",
                "![W](asset.webp)",
                "",
                "![Binary](diagram.bin)",
                "",
                "![Remote](https://example.com/image.png)"), options);
        Map<String, byte[]> entries = unzip(result.getDocx());
        String contentTypesXml = new String(entries.get("[Content_Types].xml"), StandardCharsets.UTF_8);
        String documentXml = new String(entries.get("word/document.xml"), StandardCharsets.UTF_8);

        assertTrue(entries.containsKey("word/media/image-1.gif"));
        assertTrue(entries.containsKey("word/media/image-2.jpeg"));
        assertTrue(entries.containsKey("word/media/image-3.webp"));
        assertTrue(entries.containsKey("word/media/image-4.bin"));
        assertTrue(contentTypesXml.contains("Extension=\"gif\" ContentType=\"image/gif\""));
        assertTrue(contentTypesXml.contains("Extension=\"jpeg\" ContentType=\"image/jpeg\""));
        assertTrue(contentTypesXml.contains("Extension=\"webp\" ContentType=\"image/webp\""));
        assertTrue(contentTypesXml.contains("Extension=\"bin\" ContentType=\"application/octet-stream\""));
        assertTrue(documentXml.contains("cx=\"304800\" cy=\"152400\""));
        assertTrue(documentXml.contains("cx=\"609600\" cy=\"304800\""));
        assertTrue(documentXml.contains("[Missing image: Remote]"));
        assertEquals(5, result.getSummary().images);
        assertEquals(4, result.getSummary().embeddedImages);
        assertEquals(1, result.getSummary().missingImages);
        assertEquals(1, result.getSummary().remoteImages);
        assertTrue(result.getSummary().missingImageDetails.isEmpty());
        assertEquals(1, result.getSummary().remoteImageDetails.size());
        assertEquals("https://example.com/image.png", result.getSummary().remoteImageDetails.get(0).url);
        assertTrue(new MikuMd2docxCore().formatSummary(result.getSummary()).contains("remoteImageDetails:"));
    }

    @Test
    void shrinksLargeEmbeddedImagesToDocumentBodyWidth() throws IOException {
        Md2DocxOptions options = new Md2DocxOptions();
        final byte[] png = new byte[] {
                (byte) 0x89, 0x50, 0x4e, 0x47, 0x0d, 0x0a, 0x1a, 0x0a,
                0x00, 0x00, 0x00, 0x0d, 0x49, 0x48, 0x44, 0x52,
                0x00, 0x00, 0x03, (byte) 0xe8, 0x00, 0x00, 0x01, (byte) 0xf4
        };
        options.setImageLoader(path -> new ImageAsset(path, png));

        Md2DocxResult result = new MikuMd2docxCore().convertMarkdownToDocx("![Large](large.png)", options);
        String documentXml = new String(unzip(result.getDocx()).get("word/document.xml"), StandardCharsets.UTF_8);

        assertTrue(documentXml.contains("cx=\"5943600\" cy=\"2971800\""));
        assertEquals(1, result.getSummary().resizedImages);
    }

    @Test
    void handlesSupportedAndUnsupportedRawHtml() throws IOException {
        String markdown = "A <BR/> B <INS>u</INS> C <A class='x' href='https://example.com/u'>upper</A> D <IMG alt='Pic' src='missing-upper.png'>"
                + "\n\nBefore <span class=\"x\">inside</span> after\n\n<div><b>block</b></div>";
        Md2DocxResult result = new MikuMd2docxCore().convertMarkdownToDocx(markdown);
        Map<String, byte[]> entries = unzip(result.getDocx());
        String documentXml = new String(entries.get("word/document.xml"), StandardCharsets.UTF_8);
        String relsXml = new String(entries.get("word/_rels/document.xml.rels"), StandardCharsets.UTF_8);

        assertTrue(documentXml.contains("<w:br/>"));
        assertTrue(documentXml.contains("<w:u w:val=\"single\"/>"));
        assertTrue(documentXml.contains("<w:hyperlink r:id="));
        assertTrue(documentXml.contains("[Missing image: Pic]"));
        assertTrue(documentXml.contains("inside"));
        assertTrue(documentXml.contains("block"));
        assertTrue(relsXml.contains("Target=\"https://example.com/u\" TargetMode=\"External\""));
        assertEquals(1, result.getSummary().links);
        assertEquals(1, result.getSummary().missingImages);
        assertTrue(result.getSummary().unsupportedHtml >= 2);
    }

    @Test
    void rendersGfmAutolinks() throws IOException {
        Md2DocxResult result = new MikuMd2docxCore().convertMarkdownToDocx(String.join("\n",
                "# Autolink",
                "",
                "Visit https://example.com/a?b=1 and www.example.org/path.",
                "",
                "Mail dev@example.com."));
        Map<String, byte[]> entries = unzip(result.getDocx());
        String documentXml = new String(entries.get("word/document.xml"), StandardCharsets.UTF_8);
        String relsXml = new String(entries.get("word/_rels/document.xml.rels"), StandardCharsets.UTF_8);

        assertTrue(documentXml.contains("<w:hyperlink r:id=\"rId1\""));
        assertTrue(documentXml.contains("<w:t>www.example.org/path</w:t>"));
        assertTrue(relsXml.contains("Target=\"https://example.com/a?b=1\" TargetMode=\"External\""));
        assertTrue(relsXml.contains("Target=\"http://www.example.org/path\" TargetMode=\"External\""));
        assertTrue(relsXml.contains("Target=\"mailto:dev@example.com\" TargetMode=\"External\""));
        assertEquals(3, result.getSummary().links);
        assertEquals(3, result.getSummary().externalLinks);
    }

    @Test
    void rendersSetextHeadingsAndMarkdownBreaks() throws IOException {
        Md2DocxResult result = new MikuMd2docxCore().convertMarkdownToDocx(String.join("\n",
                "Setext One",
                "==========",
                "",
                "Setext Two",
                "----------",
                "",
                "Soft line",
                "continues here.",
                "",
                "Hard break with spaces  ",
                "continues after break.",
                "",
                "Hard break with slash\\",
                "continues after slash."));
        String documentXml = new String(unzip(result.getDocx()).get("word/document.xml"), StandardCharsets.UTF_8);

        assertTrue(documentXml.contains("<w:pStyle w:val=\"Heading1\"/>"));
        assertTrue(documentXml.contains("<w:pStyle w:val=\"Heading2\"/>"));
        assertTrue(documentXml.contains("Soft line\ncontinues here."));
        assertTrue(documentXml.contains("<w:br/>"));
        assertEquals(2, result.getSummary().headings);
        assertEquals(3, result.getSummary().paragraphs);
        assertEquals(0, result.getSummary().horizontalRules);
    }

    @Test
    void rendersTildeFencedCodeBlocks() throws IOException {
        Md2DocxResult result = new MikuMd2docxCore().convertMarkdownToDocx(String.join("\n",
                "# Tilde Code",
                "",
                "~~~txt",
                "tilde one",
                "tilde two",
                "~~~",
                "",
                "after"));
        String documentXml = new String(unzip(result.getDocx()).get("word/document.xml"), StandardCharsets.UTF_8);

        assertTrue(documentXml.contains("<w:pStyle w:val=\"Code\"/>"));
        assertTrue(documentXml.contains("<w:t>tilde one</w:t>"));
        assertTrue(documentXml.contains("<w:t>tilde two</w:t>"));
        assertEquals(1, result.getSummary().codeBlocks);
        assertEquals(1, result.getSummary().paragraphs);
    }

    @Test
    void rendersIndentedCodeBlocks() throws IOException {
        Md2DocxResult result = new MikuMd2docxCore().convertMarkdownToDocx(String.join("\n",
                "# Indented Code",
                "",
                "    alpha",
                "    beta",
                "",
                "after"));
        String documentXml = new String(unzip(result.getDocx()).get("word/document.xml"), StandardCharsets.UTF_8);

        assertTrue(documentXml.contains("<w:pStyle w:val=\"Code\"/>"));
        assertTrue(documentXml.contains("<w:t>alpha</w:t>"));
        assertTrue(documentXml.contains("<w:t>beta</w:t>"));
        assertEquals(1, result.getSummary().codeBlocks);
        assertEquals(1, result.getSummary().paragraphs);
    }

    @Test
    void rendersReferenceDefinitionsLikeUpstream() throws IOException {
        Md2DocxResult result = new MikuMd2docxCore().convertMarkdownToDocx(String.join("\n",
                "# Reference Case",
                "",
                "See [Example][site] and [shortcut].",
                "",
                "![Logo][logo]",
                "",
                "[site]: https://example.com/ref \"Example title\"",
                "[shortcut]: https://example.com/shortcut",
                "[logo]: images/logo.png \"Logo title\""));
        String documentXml = new String(unzip(result.getDocx()).get("word/document.xml"), StandardCharsets.UTF_8);
        String relsXml = new String(unzip(result.getDocx()).get("word/_rels/document.xml.rels"), StandardCharsets.UTF_8);

        assertTrue(documentXml.contains("<w:t>Example</w:t>"));
        assertTrue(documentXml.contains("<w:t>shortcut</w:t>"));
        assertFalse(documentXml.contains("[site]:"));
        assertFalse(documentXml.contains("https://example.com/ref"));
        assertFalse(documentXml.contains("![Logo][logo]"));
        assertFalse(relsXml.contains("https://example.com/ref"));
        assertEquals(0, result.getSummary().links);
        assertEquals(0, result.getSummary().images);
        assertEquals(2, result.getSummary().paragraphs);
    }

    @Test
    void rendersEscapesAndEntitiesLikeUpstream() throws IOException {
        Md2DocxResult result = new MikuMd2docxCore().convertMarkdownToDocx(String.join("\n",
                "# Escapes &amp; Entities",
                "",
                "Literal \\*not emphasis\\* and \\[not link\\].",
                "",
                "Entity &amp; &copy; &#x41; &#65; &lt;tag&gt;."));
        String documentXml = new String(unzip(result.getDocx()).get("word/document.xml"), StandardCharsets.UTF_8);

        assertTrue(documentXml.contains("<w:t>Escapes &amp; Entities</w:t>"));
        assertTrue(documentXml.contains("<w:t>Literal *not emphasis* and [not link].</w:t>"));
        assertTrue(documentXml.contains("<w:t>Entity &amp; \u00a9 A A &lt;tag&gt;.</w:t>"));
        assertFalse(documentXml.contains("<w:i/>"));
        assertEquals(1, result.getSummary().headings);
        assertEquals(2, result.getSummary().paragraphs);
    }

    @Test
    void rendersNestedBlockquotesLikeUpstream() throws IOException {
        Md2DocxResult result = new MikuMd2docxCore().convertMarkdownToDocx(String.join("\n",
                "# Nested Quote",
                "",
                "> Outer line",
                "> continues",
                ">",
                "> > Inner line",
                "> > Inner **bold**",
                ">",
                "> Back outer"));
        String documentXml = new String(unzip(result.getDocx()).get("word/document.xml"), StandardCharsets.UTF_8);

        assertTrue(documentXml.contains("<w:pStyle w:val=\"Quote\"/>"));
        assertTrue(documentXml.contains("Outer line\ncontinues"));
        assertTrue(documentXml.contains("Inner line\nInner "));
        assertTrue(documentXml.contains("<w:b/>"));
        assertTrue(documentXml.contains("<w:t>Back outer</w:t>"));
        assertEquals(1, result.getSummary().blockquotes);
        assertEquals(3, result.getSummary().paragraphs);
    }

    @Test
    void ignoresBlockquoteListAndCodeChildrenLikeUpstream() throws IOException {
        Md2DocxResult result = new MikuMd2docxCore().convertMarkdownToDocx(String.join("\n",
                "# Quote Children",
                "",
                "> Intro",
                ">",
                "> - ignored list",
                "> - ignored second",
                ">",
                "> ```txt",
                "> ignored code",
                "> ```",
                ">",
                "> Outro"));
        String documentXml = new String(unzip(result.getDocx()).get("word/document.xml"), StandardCharsets.UTF_8);

        assertTrue(documentXml.contains("<w:t>Intro</w:t>"));
        assertTrue(documentXml.contains("<w:t>Outro</w:t>"));
        assertFalse(documentXml.contains("ignored list"));
        assertFalse(documentXml.contains("ignored code"));
        assertEquals(1, result.getSummary().blockquotes);
        assertEquals(2, result.getSummary().paragraphs);
        assertEquals(0, result.getSummary().lists);
        assertEquals(0, result.getSummary().listItems);
        assertEquals(0, result.getSummary().codeBlocks);
    }

    @Test
    void rendersListChildrenLikeUpstream() throws IOException {
        Md2DocxResult result = new MikuMd2docxCore().convertMarkdownToDocx(String.join("\n",
                "# List Children",
                "",
                "- first paragraph",
                "",
                "  second paragraph ignored",
                "",
                "  - nested child",
                "",
                "      code ignored",
                "",
                "- second item"));
        String documentXml = new String(unzip(result.getDocx()).get("word/document.xml"), StandardCharsets.UTF_8);

        assertTrue(documentXml.contains("<w:t>first paragraph</w:t>"));
        assertTrue(documentXml.contains("<w:t>nested child</w:t>"));
        assertTrue(documentXml.contains("<w:t>second item</w:t>"));
        assertFalse(documentXml.contains("second paragraph ignored"));
        assertFalse(documentXml.contains("code ignored"));
        assertEquals(2, result.getSummary().lists);
        assertEquals(3, result.getSummary().listItems);
        assertEquals(0, result.getSummary().paragraphs);
        assertEquals(0, result.getSummary().codeBlocks);
    }

    @Test
    void rendersHtmlBlockEdgesLikeUpstream() throws IOException {
        Md2DocxResult result = new MikuMd2docxCore().convertMarkdownToDocx(String.join("\n",
                "# HTML Edge",
                "",
                "<ins>Block underline</ins>",
                "",
                "<a href=\"https://example.com/html\">Block **link**</a>",
                "",
                "<br>",
                "",
                "Inline <ins>under **bold**</ins> and <a href=\"https://example.com/split\">split **link**</a>."));
        String documentXml = new String(unzip(result.getDocx()).get("word/document.xml"), StandardCharsets.UTF_8);
        String relsXml = new String(unzip(result.getDocx()).get("word/_rels/document.xml.rels"), StandardCharsets.UTF_8);

        assertTrue(documentXml.contains("<w:u w:val=\"single\"/>"));
        assertTrue(documentXml.contains("<w:b/><w:u w:val=\"single\"/>"));
        assertTrue(documentXml.contains("<w:br/>"));
        assertTrue(documentXml.contains("<w:hyperlink r:id=\"rId1\""));
        assertTrue(documentXml.contains("<w:hyperlink r:id=\"rId2\""));
        assertTrue(relsXml.contains("Target=\"https://example.com/html\" TargetMode=\"External\""));
        assertTrue(relsXml.contains("Target=\"https://example.com/split\" TargetMode=\"External\""));
        assertEquals(4, result.getSummary().paragraphs);
        assertEquals(2, result.getSummary().links);
        assertEquals(0, result.getSummary().unsupportedHtml);
    }

    @Test
    void rendersTableEscapedPipeLikeUpstream() throws IOException {
        Md2DocxResult result = new MikuMd2docxCore().convertMarkdownToDocx(String.join("\n",
                "# Table Edge",
                "",
                "| Left | Center | Right | Pipe |",
                "| :--- | :---: | ---: | --- |",
                "| l | c | r | a \\| b |",
                "| **bold** | plain | `code` | x |"));
        String documentXml = new String(unzip(result.getDocx()).get("word/document.xml"), StandardCharsets.UTF_8);

        assertTrue(documentXml.contains("<w:t>a | b</w:t>"));
        assertFalse(documentXml.contains("<w:t>a \\</w:t>"));
        assertTrue(documentXml.contains("<w:b/>"));
        assertTrue(documentXml.contains("<w:rStyle w:val=\"CodeChar\"/>"));
        assertEquals(1, result.getSummary().tables);
        assertEquals(0, result.getSummary().paragraphs);
    }

    @Test
    void ignoresLinkAndImageTitlesLikeUpstream() throws IOException {
        Md2DocxResult result = new MikuMd2docxCore().convertMarkdownToDocx(String.join("\n",
                "# Title Attr",
                "",
                "[Titled](https://example.com/title \"Link title\") and [Plain](https://example.com/plain).",
                "",
                "![Missing titled](missing-title.png \"Image title\")"));
        String relsXml = new String(unzip(result.getDocx()).get("word/_rels/document.xml.rels"), StandardCharsets.UTF_8);

        assertTrue(relsXml.contains("Target=\"https://example.com/title\" TargetMode=\"External\""));
        assertFalse(relsXml.contains("Link title"));
        assertEquals(2, result.getSummary().links);
        assertEquals(1, result.getSummary().images);
        assertEquals(1, result.getSummary().missingImages);
        assertEquals("missing-title.png", result.getSummary().missingImageDetails.get(0).path);
        assertEquals("Missing titled", result.getSummary().missingImageDetails.get(0).alt);
    }

    @Test
    void keepsComplexMarkdownSummaryStable() {
        String markdown = String.join("\n",
                "---",
                "title: Complex",
                "---",
                "",
                "# Title",
                "",
                "> Quote",
                "",
                "1. one",
                "2. two",
                "",
                "---",
                "",
                "```",
                "line 1",
                "line 2",
                "```",
                "",
                "![No Alt](missing-a.png)",
                "![](missing-b.png)");
        Md2DocxResult result = new MikuMd2docxCore().convertMarkdownToDocx(markdown);

        assertTrue(result.getSummary().frontMatter);
        assertEquals(1, result.getSummary().headings);
        assertEquals(1, result.getSummary().blockquotes);
        assertEquals(1, result.getSummary().lists);
        assertEquals(2, result.getSummary().listItems);
        assertEquals(1, result.getSummary().horizontalRules);
        assertEquals(1, result.getSummary().codeBlocks);
        assertEquals(2, result.getSummary().images);
        assertEquals(2, result.getSummary().missingImages);
        assertFalse(result.getSummary().missingImageDetails.isEmpty());
        assertEquals("missing-a.png", result.getSummary().missingImageDetails.get(0).path);
        assertEquals("No Alt", result.getSummary().missingImageDetails.get(0).alt);
        assertEquals("missing-b.png", result.getSummary().missingImageDetails.get(1).path);
        assertEquals("", result.getSummary().missingImageDetails.get(1).alt);
    }

    private Map<String, byte[]> unzip(byte[] docx) throws IOException {
        Map<String, byte[]> entries = new LinkedHashMap<String, byte[]>();
        ZipInputStream zip = new ZipInputStream(new ByteArrayInputStream(docx));
        ZipEntry entry;
        byte[] buffer = new byte[4096];
        while ((entry = zip.getNextEntry()) != null) {
            java.io.ByteArrayOutputStream out = new java.io.ByteArrayOutputStream();
            int read;
            while ((read = zip.read(buffer)) >= 0) {
                out.write(buffer, 0, read);
            }
            entries.put(entry.getName(), out.toByteArray());
        }
        return entries;
    }
}
