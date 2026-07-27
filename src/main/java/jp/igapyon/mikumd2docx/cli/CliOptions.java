package jp.igapyon.mikumd2docx.cli;

class CliOptions {
    String inputPath;
    String outPath;
    String templatePath;
    String summaryOutPath;
    boolean summary;
    boolean verbose;
    boolean help;
    boolean version;

    static CliOptions parse(String[] args) {
        CliOptions options = new CliOptions();
        for (int i = 0; i < args.length; i++) {
            String arg = args[i];
            if ("--help".equals(arg)) {
                options.help = true;
                return options;
            } else if ("--version".equals(arg)) {
                options.version = true;
                return options;
            } else if ("--out".equals(arg)) {
                options.outPath = requireValue(args, ++i, "--out");
            } else if ("--summary".equals(arg)) {
                options.summary = true;
            } else if ("--template".equals(arg)) {
                options.templatePath = requireValue(args, ++i, "--template");
            } else if ("--summary-out".equals(arg)) {
                options.summaryOutPath = requireValue(args, ++i, "--summary-out");
            } else if ("--verbose".equals(arg)) {
                options.verbose = true;
            } else if (arg.startsWith("-")) {
                throw new IllegalArgumentException("Unknown option: " + arg);
            } else if (options.inputPath == null) {
                options.inputPath = arg;
            } else {
                throw new IllegalArgumentException("Unknown argument: " + arg);
            }
        }
        return options;
    }

    private static String requireValue(String[] args, int index, String option) {
        if (index >= args.length || args[index].startsWith("--")) {
            throw new IllegalArgumentException(option + " requires a value.");
        }
        return args[index];
    }
}
