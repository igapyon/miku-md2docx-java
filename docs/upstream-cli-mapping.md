# Upstream CLI Mapping

Upstream CLI:

```bash
npm run cli -- <input.md> --out <output.docx>
npm run cli -- --help
npm run cli -- --version
```

Java CLI:

```bash
java -jar target/miku-md2docx-java-0.5.0.1.jar <input.md> --out <output.docx>
java -jar target/miku-md2docx-java-0.5.0.1.jar --help
java -jar target/miku-md2docx-java-0.5.0.1.jar --version
```

| Upstream option | Java option | Status |
| --- | --- | --- |
| positional `<input.md>` | positional `<input.md>` | Implemented |
| `--out <file>` | `--out <file>` | Implemented |
| `--summary` | `--summary` | Implemented |
| `--summary-out <file>` | `--summary-out <file>` | Implemented |
| `--verbose` | `--verbose` | Implemented |
| `--help` | `--help` | Implemented |
| `--version` | `--version` | Implemented |

Known initial differences:

- Java version prints `0.5.0.1`, matching the compatibility tag used for this repository.
- DOCX content parity with upstream is not complete yet.
- Image embedding is not complete yet; image summary fields exist as migration targets.
