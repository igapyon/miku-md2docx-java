package jp.igapyon.mikumd2docx.core;

public class Md2DocxSummary {
    public int paragraphs;
    public int headings;
    public int links;
    public int internalLinks;
    public int externalLinks;
    public int unresolvedInternalLinks;
    public int lists;
    public int listItems;
    public int tables;
    public int codeBlocks;
    public int blockquotes;
    public int horizontalRules;
    public int images;
    public int embeddedImages;
    public int missingImages;
    public int resizedImages;
    public boolean frontMatter;
    public int unsupportedHtml;
    public final java.util.List<MissingImageDetail> missingImageDetails = new java.util.ArrayList<MissingImageDetail>();

    public static class MissingImageDetail {
        public final String path;
        public final String alt;

        public MissingImageDetail(String path, String alt) {
            this.path = path;
            this.alt = alt;
        }
    }
}
