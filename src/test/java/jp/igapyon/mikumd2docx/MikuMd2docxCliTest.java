package jp.igapyon.mikumd2docx;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import jp.igapyon.mikumd2docx.cli.MikuMd2docxCli;
import org.junit.jupiter.api.Test;

class MikuMd2docxCliTest {
    @Test
    void printsVersionAndHelp() {
        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        MikuMd2docxCli cli = new MikuMd2docxCli();

        assertEquals(0, cli.run(new String[] {"--version"}, stream(out), stream(err)));
        assertTrue(out.toString().contains("1.0.0"));

        out.reset();
        assertEquals(0, cli.run(new String[] {"--help"}, stream(out), stream(err)));
        String help = out.toString();
        assertTrue(help.contains("Usage:"));
        assertTrue(help.contains("Arguments:"));
        assertTrue(help.contains("Required options:"));
        assertTrue(help.contains("Outputs:"));
        assertTrue(help.contains("Overwrite behavior:"));
        assertTrue(help.contains("Diagnostics:"));
        assertTrue(help.contains("Exit codes:"));
        assertTrue(help.contains("--template <docx>"));
        assertTrue(help.contains("Template styles and section settings may carry over"));
        assertTrue(help.contains("Header and footer references are not carried over"));
        assertTrue(help.contains("2  invalid CLI usage"));
    }

    @Test
    void convertsMarkdownFile() throws Exception {
        Path dir = Files.createTempDirectory("miku-md2docx-java-");
        Path input = dir.resolve("sample.md");
        Path output = dir.resolve("sample.docx");
        Path templateInput = dir.resolve("template.md");
        Path template = dir.resolve("template.docx");
        Path templatedOutput = dir.resolve("sample-templated.docx");
        Files.write(input, "# Sample\n\nHello.".getBytes(StandardCharsets.UTF_8));
        Files.write(templateInput, "# Template\n\nTemplate body.".getBytes(StandardCharsets.UTF_8));

        ByteArrayOutputStream out = new ByteArrayOutputStream();
        ByteArrayOutputStream err = new ByteArrayOutputStream();
        int exitCode = new MikuMd2docxCli().run(new String[] {
                input.toString(), "--out", output.toString(), "--summary"
        }, stream(out), stream(err));

        assertEquals(0, exitCode, err.toString());
        assertTrue(Files.size(output) > 100);
        assertTrue(out.toString().contains("headings: 1"));

        assertEquals(0, new MikuMd2docxCli().run(new String[] {
                templateInput.toString(), "--out", template.toString()
        }, stream(new ByteArrayOutputStream()), stream(new ByteArrayOutputStream())));
        assertEquals(0, new MikuMd2docxCli().run(new String[] {
                input.toString(), "--out", templatedOutput.toString(), "--template", template.toString()
        }, stream(new ByteArrayOutputStream()), stream(new ByteArrayOutputStream())));
        assertTrue(Files.size(templatedOutput) > 100);
    }

    private PrintStream stream(ByteArrayOutputStream bytes) {
        return new PrintStream(bytes, true);
    }
}
