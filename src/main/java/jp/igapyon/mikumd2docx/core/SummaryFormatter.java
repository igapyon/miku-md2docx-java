package jp.igapyon.mikumd2docx.core;

final class SummaryFormatter {
    private SummaryFormatter() {
    }

    static String format(Md2DocxSummary summary) {
        StringBuilder builder = new StringBuilder();
        builder.append("paragraphs: ").append(summary.paragraphs).append('\n');
        builder.append("headings: ").append(summary.headings).append('\n');
        builder.append("links: ").append(summary.links).append('\n');
        builder.append("internalLinks: ").append(summary.internalLinks).append('\n');
        builder.append("externalLinks: ").append(summary.externalLinks).append('\n');
        builder.append("unresolvedInternalLinks: ").append(summary.unresolvedInternalLinks).append('\n');
        builder.append("lists: ").append(summary.lists).append('\n');
        builder.append("listItems: ").append(summary.listItems).append('\n');
        builder.append("tables: ").append(summary.tables).append('\n');
        builder.append("codeBlocks: ").append(summary.codeBlocks).append('\n');
        builder.append("blockquotes: ").append(summary.blockquotes).append('\n');
        builder.append("horizontalRules: ").append(summary.horizontalRules).append('\n');
        builder.append("images: ").append(summary.images).append('\n');
        builder.append("embeddedImages: ").append(summary.embeddedImages).append('\n');
        builder.append("missingImages: ").append(summary.missingImages).append('\n');
        builder.append("remoteImages: ").append(summary.remoteImages).append('\n');
        builder.append("resizedImages: ").append(summary.resizedImages).append('\n');
        builder.append("frontMatter: ").append(summary.frontMatter).append('\n');
        builder.append("unsupportedHtml: ").append(summary.unsupportedHtml).append('\n');
        if (!summary.missingImageDetails.isEmpty()) {
            builder.append("missingImageDetails:\n");
            for (Md2DocxSummary.MissingImageDetail detail : summary.missingImageDetails) {
                builder.append("- path: ").append(detail.path).append('\n');
                builder.append("  alt: ").append(detail.alt).append('\n');
            }
        }
        if (!summary.remoteImageDetails.isEmpty()) {
            builder.append("remoteImageDetails:\n");
            for (Md2DocxSummary.RemoteImageDetail detail : summary.remoteImageDetails) {
                builder.append("- url: ").append(detail.url).append('\n');
                builder.append("  alt: ").append(detail.alt).append('\n');
            }
        }
        return builder.toString();
    }
}
