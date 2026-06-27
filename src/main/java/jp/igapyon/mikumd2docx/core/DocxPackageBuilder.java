package jp.igapyon.mikumd2docx.core;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import jp.igapyon.mikumsofficecore.OpcContentTypeDefault;
import jp.igapyon.mikumsofficecore.OpcContentTypeOverride;
import jp.igapyon.mikumsofficecore.OpcContentTypes;
import jp.igapyon.mikumsofficecore.OpcRelationship;
import jp.igapyon.mikumsofficecore.OpcRelationships;
import jp.igapyon.mikumsofficecore.ZipEntryInput;
import jp.igapyon.mikumsofficecore.ZipPackage;

final class DocxPackageBuilder {
    private DocxPackageBuilder() {
    }

    static String buildDocumentXml(String body) {
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

    static byte[] createDocx(String documentXml, List<Relationship> relationships, Map<String, byte[]> imageMedia) {
        List<ZipEntryInput> entries = new ArrayList<ZipEntryInput>();
        entries.add(new ZipEntryInput("[Content_Types].xml", contentTypesXml(imageMedia.keySet())));
        entries.add(new ZipEntryInput("_rels/.rels", rootRelsXml()));
        entries.add(new ZipEntryInput("docProps/app.xml", appXml()));
        entries.add(new ZipEntryInput("docProps/core.xml", coreXml()));
        entries.add(new ZipEntryInput("word/document.xml", documentXml));
        entries.add(new ZipEntryInput("word/_rels/document.xml.rels", Relationships.documentRelsXml(relationships)));
        entries.add(new ZipEntryInput("word/styles.xml", stylesXml()));
        entries.add(new ZipEntryInput("word/numbering.xml", numberingXml()));
        for (Map.Entry<String, byte[]> image : imageMedia.entrySet()) {
            entries.add(new ZipEntryInput(image.getKey(), image.getValue()));
        }
        return ZipPackage.writeZipPackage(entries);
    }

    private static String contentTypesXml(Set<String> imagePaths) {
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

        List<OpcContentTypeDefault> contentTypeDefaults = new ArrayList<OpcContentTypeDefault>();
        contentTypeDefaults.add(new OpcContentTypeDefault("rels", "application/vnd.openxmlformats-package.relationships+xml"));
        contentTypeDefaults.add(new OpcContentTypeDefault("xml", "application/xml"));
        for (String ext : defaults) {
            contentTypeDefaults.add(new OpcContentTypeDefault(ext, ImageAssets.contentTypeForExt(ext)));
        }

        List<OpcContentTypeOverride> overrides = Arrays.asList(
                new OpcContentTypeOverride("word/document.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.document.main+xml"),
                new OpcContentTypeOverride("word/styles.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.styles+xml"),
                new OpcContentTypeOverride("word/numbering.xml", "application/vnd.openxmlformats-officedocument.wordprocessingml.numbering+xml"),
                new OpcContentTypeOverride("docProps/core.xml", "application/vnd.openxmlformats-package.core-properties+xml"),
                new OpcContentTypeOverride("docProps/app.xml", "application/vnd.openxmlformats-officedocument.extended-properties+xml"));
        return OpcContentTypes.buildOpcContentTypesXml(new OpcContentTypes(contentTypeDefaults, overrides));
    }

    private static String rootRelsXml() {
        return OpcRelationships.buildOpcRelationshipsXml(Arrays.asList(
                new OpcRelationship("rId1",
                        "http://schemas.openxmlformats.org/officeDocument/2006/relationships/officeDocument",
                        "word/document.xml")));
    }

    private static String appXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<Properties xmlns=\"http://schemas.openxmlformats.org/officeDocument/2006/extended-properties\" xmlns:vt=\"http://schemas.openxmlformats.org/officeDocument/2006/docPropsVTypes\"><Application>miku-md2docx</Application></Properties>";
    }

    private static String coreXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?>"
                + "<cp:coreProperties xmlns:cp=\"http://schemas.openxmlformats.org/package/2006/metadata/core-properties\" xmlns:dc=\"http://purl.org/dc/elements/1.1/\" xmlns:dcterms=\"http://purl.org/dc/terms/\" xmlns:dcmitype=\"http://purl.org/dc/dcmitype/\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\"><dc:creator>miku-md2docx</dc:creator><cp:lastModifiedBy>miku-md2docx</cp:lastModifiedBy></cp:coreProperties>";
    }

    private static String stylesXml() {
        StringBuilder styles = new StringBuilder();
        styles.append("<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><w:styles xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">");
        styles.append("<w:style w:type=\"paragraph\" w:default=\"1\" w:styleId=\"Normal\"><w:name w:val=\"Normal\"/></w:style>");
        for (int i = 1; i <= 6; i++) {
            String sizeXml = i == 1 ? "<w:sz w:val=\"32\"/>" : i == 2 ? "<w:sz w:val=\"28\"/>" : i == 3 ? "<w:sz w:val=\"24\"/>" : "";
            styles.append("<w:style w:type=\"paragraph\" w:styleId=\"Heading").append(i).append("\"><w:name w:val=\"heading ").append(i)
                    .append("\"/><w:basedOn w:val=\"Normal\"/><w:pPr><w:outlineLvl w:val=\"").append(i - 1)
                    .append("\"/></w:pPr><w:rPr><w:b/>").append(sizeXml).append("</w:rPr></w:style>");
        }
        styles.append("<w:style w:type=\"paragraph\" w:styleId=\"Quote\"><w:name w:val=\"Quote\"/><w:basedOn w:val=\"Normal\"/><w:pPr><w:ind w:left=\"720\"/></w:pPr><w:rPr><w:i/></w:rPr></w:style>");
        styles.append("<w:style w:type=\"paragraph\" w:styleId=\"Code\"><w:name w:val=\"Code\"/><w:basedOn w:val=\"Normal\"/><w:rPr><w:rFonts w:ascii=\"Courier New\" w:hAnsi=\"Courier New\"/></w:rPr></w:style>");
        styles.append("<w:style w:type=\"paragraph\" w:styleId=\"Separator\"><w:name w:val=\"Separator\"/><w:basedOn w:val=\"Normal\"/><w:pPr><w:pBdr><w:bottom w:val=\"single\" w:sz=\"6\" w:space=\"1\" w:color=\"auto\"/></w:pBdr></w:pPr></w:style>");
        styles.append("<w:style w:type=\"character\" w:styleId=\"CodeChar\"><w:name w:val=\"Code Char\"/><w:rPr><w:rFonts w:ascii=\"Courier New\" w:hAnsi=\"Courier New\"/></w:rPr></w:style>");
        styles.append("</w:styles>");
        return styles.toString();
    }

    private static String numberingXml() {
        return "<?xml version=\"1.0\" encoding=\"UTF-8\" standalone=\"yes\"?><w:numbering xmlns:w=\"http://schemas.openxmlformats.org/wordprocessingml/2006/main\">"
                + "<w:abstractNum w:abstractNumId=\"0\"><w:nsid w:val=\"5A6B7C01\"/><w:multiLevelType w:val=\"hybridMultilevel\"/><w:tmpl w:val=\"11111111\"/><w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"bullet\"/><w:lvlText w:val=\"•\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:hint=\"default\"/></w:rPr></w:lvl><w:lvl w:ilvl=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"bullet\"/><w:lvlText w:val=\"•\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:ind w:left=\"1440\" w:hanging=\"360\"/></w:pPr><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:hint=\"default\"/></w:rPr></w:lvl><w:lvl w:ilvl=\"2\"><w:start w:val=\"1\"/><w:numFmt w:val=\"bullet\"/><w:lvlText w:val=\"•\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:ind w:left=\"2160\" w:hanging=\"360\"/></w:pPr><w:rPr><w:rFonts w:ascii=\"Arial\" w:hAnsi=\"Arial\" w:hint=\"default\"/></w:rPr></w:lvl></w:abstractNum>"
                + "<w:abstractNum w:abstractNumId=\"1\"><w:nsid w:val=\"5A6B7C02\"/><w:multiLevelType w:val=\"hybridMultilevel\"/><w:tmpl w:val=\"22222222\"/><w:lvl w:ilvl=\"0\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%1.\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:ind w:left=\"720\" w:hanging=\"360\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"1\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%2.\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:ind w:left=\"1440\" w:hanging=\"360\"/></w:pPr></w:lvl><w:lvl w:ilvl=\"2\"><w:start w:val=\"1\"/><w:numFmt w:val=\"decimal\"/><w:lvlText w:val=\"%3.\"/><w:lvlJc w:val=\"left\"/><w:pPr><w:ind w:left=\"2160\" w:hanging=\"360\"/></w:pPr></w:lvl></w:abstractNum>"
                + "<w:num w:numId=\"1\"><w:abstractNumId w:val=\"0\"/></w:num><w:num w:numId=\"2\"><w:abstractNumId w:val=\"1\"/></w:num></w:numbering>";
    }
}
