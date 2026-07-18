# Upstream CLI Mapping

Upstream CLI:

```bash
npm run cli -- <input.md> --out <output.docx>
npm run cli -- --help
npm run cli -- --version
```

Java CLI:

```bash
java -jar target/miku-md2docx-java-1.0.1.jar <input.md> --out <output.docx>
java -jar target/miku-md2docx-java-1.0.1.jar <input.md> --out <output.docx> --template <template.docx>
java -jar target/miku-md2docx-java-1.0.1.jar --help
java -jar target/miku-md2docx-java-1.0.1.jar --version
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

- Java version prints `1.0.1`, tracking upstream release `v1.0.1`.
- Java help text follows the upstream `1.0.1` section structure and adapts the
  examples to the executable jar form.
- Focused Node-vs-Java DOCX and summary parity checks are maintained in
  `scripts/compare-node-java-cli.sh`.
