package jp.igapyon.mikumd2docx.core;

public class MikuMd2docxCore {
    public static final String VERSION = "1.0.0";

    public Md2DocxResult convertMarkdownToDocx(String markdown) {
        return convertMarkdownToDocx(markdown, new Md2DocxOptions());
    }

    public Md2DocxResult convertMarkdownToDocx(String markdown, Md2DocxOptions options) {
        RenderState state = new RenderState(options == null ? new Md2DocxOptions() : options);
        MarkdownRenderer.collectReferenceDefinitions(markdown == null ? "" : markdown, state);
        MarkdownRenderer.collectHeadingBookmarks(markdown == null ? "" : markdown, state);
        String documentXml = DocxPackageBuilder.buildDocumentXml(MarkdownRenderer.renderMarkdown(markdown == null ? "" : markdown, state), state);
        return new Md2DocxResult(DocxPackageBuilder.createDocx(documentXml, state), state.summary);
    }

    public String formatSummary(Md2DocxSummary summary) {
        return SummaryFormatter.format(summary);
    }
}
