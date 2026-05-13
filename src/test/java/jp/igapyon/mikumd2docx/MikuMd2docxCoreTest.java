package jp.igapyon.mikumd2docx;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;
import jp.igapyon.mikumd2docx.core.Md2DocxResult;
import jp.igapyon.mikumd2docx.core.Md2DocxOptions;
import jp.igapyon.mikumd2docx.core.ImageAsset;
import jp.igapyon.mikumd2docx.core.MikuMd2docxCore;
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
        assertTrue(new String(entries.get("word/document.xml"), StandardCharsets.UTF_8).contains("Heading1"));
        assertEquals(1, result.getSummary().headings);
        assertEquals(1, result.getSummary().paragraphs);
        assertEquals(1, result.getSummary().externalLinks);
        assertTrue(new String(entries.get("word/_rels/document.xml.rels"), StandardCharsets.UTF_8).contains("Target=\"https://example.com\" TargetMode=\"External\""));
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
