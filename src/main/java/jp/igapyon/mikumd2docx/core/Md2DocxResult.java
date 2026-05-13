package jp.igapyon.mikumd2docx.core;

public class Md2DocxResult {
    private final byte[] docx;
    private final Md2DocxSummary summary;

    public Md2DocxResult(byte[] docx, Md2DocxSummary summary) {
        this.docx = docx;
        this.summary = summary;
    }

    public byte[] getDocx() {
        return docx;
    }

    public Md2DocxSummary getSummary() {
        return summary;
    }
}
