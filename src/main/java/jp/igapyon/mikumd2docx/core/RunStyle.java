package jp.igapyon.mikumd2docx.core;

final class RunStyle {
    boolean bold;
    boolean italic;
    boolean strike;
    boolean underline;
    boolean code;

    RunStyle copy() {
        RunStyle copy = new RunStyle();
        copy.bold = bold;
        copy.italic = italic;
        copy.strike = strike;
        copy.underline = underline;
        copy.code = code;
        return copy;
    }
}
