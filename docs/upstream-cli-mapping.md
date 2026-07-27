# Upstream CLI Mapping

Upstream GitHub Release asset:

```bash
node miku-md2docx-1.1.0.mjs <input.md> --out <output.docx>
node miku-md2docx-1.1.0.mjs --help
node miku-md2docx-1.1.0.mjs --version
```

Java GitHub Release asset:

```bash
java -jar miku-md2docx-java-1.1.0.jar <input.md> --out <output.docx>
java -jar miku-md2docx-java-1.1.0.jar <input.md> --out <output.docx> --template <template.docx>
java -jar miku-md2docx-java-1.1.0.jar --help
java -jar miku-md2docx-java-1.1.0.jar --version
```

| Upstream option | Java option | Status |
| --- | --- | --- |
| positional `<input.md>` | positional `<input.md>` | Implemented |
| `--out <file>` | `--out <file>` | Implemented |
| `--summary` | `--summary` | Implemented |
| `--summary-out <file>` | `--summary-out <file>` | Implemented |
| `--template <docx>` | `--template <docx>` | Implemented |
| `--verbose` | `--verbose` | Implemented |
| `--help` | `--help` | Implemented |
| `--version` | `--version` | Implemented |

Current notes:

- Java version prints `1.1.0`, tracking upstream release `v1.1.0`.
- Java help text follows the upstream `1.1.0` contract sections and adapts the
  examples to the downloaded executable Release asset form rather than a
  source-tree-relative `target/` path.
- Missing parent directories for `--out` and `--summary-out` are created
  automatically. Existing files are overwritten.
- `--summary` writes human-readable text to stdout. CLI usage errors,
  conversion/file-system failures, and verbose progress use stderr.
  The summary text is not a stable machine-readable API.
- Exit codes are `0` for success/help/version, `1` for conversion or
  file-system failure, and `2` for invalid CLI usage, including unknown
  options.
- Unknown short or long options return exit code `2`.
- Release tags may add a dot suffix to the Maven project version. The complete
  Release version is embedded in the runtime JAR Manifest and appears in
  `--version`, help, and the Asset filename.
- Conversion generates only the requested DOCX and optional summary file.
- Focused Node-vs-Java DOCX and summary parity checks are maintained in
  `scripts/compare-node-java-cli.sh`.
