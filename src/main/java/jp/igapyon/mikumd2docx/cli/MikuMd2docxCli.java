package jp.igapyon.mikumd2docx.cli;

import java.io.IOException;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import jp.igapyon.mikumd2docx.core.ImageAsset;
import jp.igapyon.mikumd2docx.core.Md2DocxResult;
import jp.igapyon.mikumd2docx.core.Md2DocxOptions;
import jp.igapyon.mikumd2docx.core.MikuMd2docxCore;

public class MikuMd2docxCli {
    public static void main(String[] args) {
        int exitCode = new MikuMd2docxCli().run(args, System.out, System.err);
        if (exitCode != 0) {
            System.exit(exitCode);
        }
    }

    public int run(String[] args, PrintStream out, PrintStream err) {
        CliOptions options;
        try {
            options = CliOptions.parse(args);
        } catch (IllegalArgumentException ex) {
            err.println(ex.getMessage());
            return 2;
        }
        if (options.help) {
            out.print(helpText());
            return 0;
        }
        if (options.version) {
            out.println(MikuMd2docxCore.VERSION);
            return 0;
        }
        if (options.inputPath == null || options.outPath == null) {
            err.print(helpText());
            return 2;
        }
        try {
            if (options.verbose) {
                err.println("verbose: reading " + options.inputPath);
            }
            String markdown = new String(Files.readAllBytes(Paths.get(options.inputPath)), StandardCharsets.UTF_8);
            MikuMd2docxCore core = new MikuMd2docxCore();
            Md2DocxOptions convertOptions = new Md2DocxOptions();
            convertOptions.setImageLoader(createImageLoader(Paths.get(options.inputPath)));
            Md2DocxResult result = core.convertMarkdownToDocx(markdown, convertOptions);
            Files.write(Paths.get(options.outPath), result.getDocx());
            if (options.verbose) {
                err.println("verbose: wrote " + options.outPath);
            }
            String summary = core.formatSummary(result.getSummary());
            if (options.summary) {
                out.print(summary);
            }
            if (options.summaryOutPath != null) {
                Files.write(Paths.get(options.summaryOutPath), summary.getBytes(StandardCharsets.UTF_8));
            }
            return 0;
        } catch (IOException ex) {
            err.println(ex.getMessage());
            return 1;
        } catch (RuntimeException ex) {
            err.println(ex.getMessage());
            return 1;
        }
    }

    public static String helpText() {
        return "miku-md2docx " + MikuMd2docxCore.VERSION + "\n"
                + "\n"
                + "Usage:\n"
                + "  java -jar target/miku-md2docx-java-" + MikuMd2docxCore.VERSION + ".jar <input.md> --out <output.docx>\n"
                + "  java -jar target/miku-md2docx-java-" + MikuMd2docxCore.VERSION + ".jar --help\n"
                + "  java -jar target/miku-md2docx-java-" + MikuMd2docxCore.VERSION + ".jar --version\n"
                + "\n"
                + "Options:\n"
                + "  --out <file>          Write DOCX output to file\n"
                + "  --summary             Print conversion summary to stdout\n"
                + "  --summary-out <file>  Write conversion summary to file\n"
                + "  --verbose             Print progress diagnostics to stderr\n"
                + "  --help                Show this help\n"
                + "  --version             Show version\n";
    }

    private Md2DocxOptions.ImageLoader createImageLoader(final Path inputPath) {
        return new Md2DocxOptions.ImageLoader() {
            @Override
            public ImageAsset load(String path) {
                if (path == null || path.matches("^[a-zA-Z][a-zA-Z0-9+.-]*:.*")) {
                    return null;
                }
                try {
                    Path base = inputPath.toAbsolutePath().getParent();
                    Path resolved = base == null ? Paths.get(path) : base.resolve(path).normalize();
                    return new ImageAsset(path, Files.readAllBytes(resolved));
                } catch (IOException ex) {
                    return null;
                }
            }
        };
    }
}
