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
            if (options.templatePath != null) {
                convertOptions.setTemplateDocx(Files.readAllBytes(Paths.get(options.templatePath)));
            }
            convertOptions.setImageLoader(createImageLoader(Paths.get(options.inputPath)));
            Md2DocxResult result = core.convertMarkdownToDocx(markdown, convertOptions);
            Path outputPath = Paths.get(options.outPath);
            createParentDirectories(outputPath);
            Files.write(outputPath, result.getDocx());
            if (options.verbose) {
                err.println("verbose: wrote " + options.outPath);
            }
            String summary = core.formatSummary(result.getSummary());
            if (options.summary) {
                out.print(summary);
            }
            if (options.summaryOutPath != null) {
                Path summaryOutputPath = Paths.get(options.summaryOutPath);
                createParentDirectories(summaryOutputPath);
                Files.write(summaryOutputPath, summary.getBytes(StandardCharsets.UTF_8));
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
                + "  java -jar miku-md2docx-java-" + MikuMd2docxCore.VERSION + ".jar <input.md> --out <output.docx>\n"
                + "  java -jar miku-md2docx-java-" + MikuMd2docxCore.VERSION + ".jar --help\n"
                + "  java -jar miku-md2docx-java-" + MikuMd2docxCore.VERSION + ".jar --version\n"
                + "\n"
                + "Description:\n"
                + "  Convert one UTF-8 Markdown file to one editable Word .docx file locally.\n"
                + "\n"
                + "Primary contract:\n"
                + "  stdout  Human-readable summary only with --summary; help and version text\n"
                + "  stderr  CLI usage errors, file or conversion failures, and --verbose progress\n"
                + "  file    The generated .docx; optional human-readable summary text file\n"
                + "\n"
                + "Arguments:\n"
                + "  <input.md>            Markdown input file. Required for conversion.\n"
                + "\n"
                + "Required options:\n"
                + "  --out <file>          DOCX output file. Required for conversion.\n"
                + "\n"
                + "Options:\n"
                + "  --summary             Print conversion summary to stdout\n"
                + "  --summary-out <file>  Write conversion summary to file\n"
                + "  --template <docx>     Reuse compatible DOCX template package parts\n"
                + "  --verbose             Print progress diagnostics to stderr\n"
                + "  --help                Show this help\n"
                + "  --version             Show version\n"
                + "\n"
                + "Inputs:\n"
                + "  <input.md> is read as UTF-8 Markdown. Local images are resolved relative to\n"
                + "  the input Markdown file.\n"
                + "\n"
                + "Outputs:\n"
                + "  --out <file> is the generated editable Word .docx file. Summary output is\n"
                + "  written only when --summary or --summary-out is specified. Missing parent\n"
                + "  directories for --out and --summary-out are created automatically.\n"
                + "\n"
                + "Generated artifacts:\n"
                + "  Conversion creates only the .docx given by --out and, when requested, the\n"
                + "  summary text file given by --summary-out. Repository target/ contents are\n"
                + "  development build artifacts, not conversion outputs.\n"
                + "\n"
                + "Machine-readable output contract:\n"
                + "  The .docx file is the primary generated artifact. Summary output is\n"
                + "  human-readable text and is not a stable machine-readable API.\n"
                + "\n"
                + "Overwrite behavior:\n"
                + "  Existing --out and --summary-out files are overwritten.\n"
                + "\n"
                + "Diagnostics:\n"
                + "  CLI usage errors, file-system failures, conversion failures, and verbose\n"
                + "  progress are written to stderr.\n"
                + "  Missing images, remote image URLs, unresolved internal links, and unsupported\n"
                + "  HTML are reported in the summary without aborting conversion.\n"
                + "\n"
                + "Exit codes:\n"
                + "  0  success, --help, or --version\n"
                + "  1  conversion or file-system failure\n"
                + "  2  invalid CLI usage, such as missing <input.md> or --out\n"
                + "\n"
                + "Examples:\n"
                + "  java -jar miku-md2docx-java-" + MikuMd2docxCore.VERSION + ".jar README.md --out README.docx\n"
                + "  java -jar miku-md2docx-java-" + MikuMd2docxCore.VERSION + ".jar README.md --out README.docx --template template.docx\n"
                + "  java -jar miku-md2docx-java-" + MikuMd2docxCore.VERSION + ".jar README.md --out README.docx --summary\n"
                + "  java -jar miku-md2docx-java-" + MikuMd2docxCore.VERSION + ".jar README.md --out generated/README.docx --summary-out generated/README.summary.txt\n"
                + "\n"
                + "Template notes:\n"
                + "  Template mode replaces the template document body with generated Markdown\n"
                + "  content while preserving compatible package parts where practical.\n"
                + "  Template styles and section settings may carry over; numbering is regenerated.\n"
                + "  Existing template body paragraphs are not copied.\n"
                + "  Header and footer references are not carried over in the first cut.\n"
                + "\n"
                + "Markdown handling notes:\n"
                + "  Remote image URLs are not downloaded.\n"
                + "  SVG images are not converted.\n"
                + "  Table alignment and merged cells are ignored.\n";
    }

    private void createParentDirectories(Path path) throws IOException {
        Path parent = path.toAbsolutePath().normalize().getParent();
        if (parent != null) {
            Files.createDirectories(parent);
        }
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
