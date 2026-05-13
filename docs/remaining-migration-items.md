# Remaining Migration Items

## Resume Note 2026-05-14

Stop point:

- First-cut Java runtime conversion is implemented.
- Maven plugin is not included.
- Node-vs-Java sample summary parity is established through
  `scripts/compare-node-java-cli.sh`.
- Full DOCX XML parity is not established yet.

Recommended first command after resuming:

```bash
mvn test
scripts/compare-node-java-cli.sh
```

Recommended next implementation slice:

- Extend `scripts/compare-node-java-cli.sh` to unzip `node.docx` and
  `java.docx`, normalize XML whitespace, and write diffs for:
  - `word/document.xml`
  - `word/_rels/document.xml.rels`
  - `word/styles.xml`
  - `word/numbering.xml`
- Use those diffs to decide the next upstream renderer parity fix.

## Completed

- Maven Java 8 runtime skeleton.
- Executable shaded jar packaging.
- Distribution zip packaging.
- Thin CLI entrypoint with upstream option names.
- Callable core API.
- DOCX package writer with core package entries, styles, numbering, relationships, and image media entries.
- Upstream summary field vocabulary.
- First-cut Markdown block handling for headings, paragraphs, lists, tables, code blocks, blockquotes, horizontal rules, and front matter.
- First-cut inline handling for bold, italic, strike, inline code, links, limited HTML, and images.
- Focused JUnit tests.
- Upstream class, CLI, and test mapping documents.
- miku-soft basic documents copied into `docs/`.

## Pending

- Complete Markdown AST parsing parity with upstream `remark` behavior.
- Complete OOXML block and inline renderer parity for edge cases.
- Complete image embedding parity for all upstream image cases.
- Add byte / XML-level fixture parity tests from upstream.
- Harden Node-vs-Java CLI comparison script with DOCX XML diff checks.

## Follow-up Candidates

- Add Maven plugin support after runtime core and CLI parity are stable.

## Latest Verification

Run:

```bash
mvn test
```

Latest checked on 2026-05-14:

- `mvn test`: 7 tests passed, covering CLI help/version/conversion, DOCX package entries, summary vocabulary, deterministic output, representative OOXML structures, and embedded image bytes.
- `mvn test`: later expanded to 11 tests, adding image format, resize, raw HTML, and complex summary coverage.
- `scripts/compare-node-java-cli.sh`: sample Node and Java summary outputs matched.
