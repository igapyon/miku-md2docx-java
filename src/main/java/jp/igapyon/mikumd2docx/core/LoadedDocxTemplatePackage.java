package jp.igapyon.mikumd2docx.core;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import jp.igapyon.mikumsofficecore.ZipEntry;
import jp.igapyon.mikumsofficecore.ZipPackage;

final class LoadedDocxTemplatePackage {
    final List<ZipEntry> entries;
    final Map<String, byte[]> files;
    final byte[] documentXmlBytes;
    final byte[] stylesBytes;

    private LoadedDocxTemplatePackage(List<ZipEntry> entries, Map<String, byte[]> files,
            byte[] documentXmlBytes, byte[] stylesBytes) {
        this.entries = entries;
        this.files = files;
        this.documentXmlBytes = documentXmlBytes;
        this.stylesBytes = stylesBytes;
    }

    static LoadedDocxTemplatePackage load(byte[] data) {
        List<ZipEntry> entries = ZipPackage.readZipPackage(data).getEntries();
        Map<String, byte[]> files = new LinkedHashMap<String, byte[]>();
        for (ZipEntry entry : entries) {
            files.put(entry.getPath(), entry.getData());
        }
        byte[] documentXml = files.get("word/document.xml");
        if (documentXml == null) {
            throw new IllegalArgumentException("word/document.xml was not found in template DOCX.");
        }
        return new LoadedDocxTemplatePackage(entries, files, documentXml, files.get("word/styles.xml"));
    }
}
