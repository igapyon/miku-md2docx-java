# Remaining Migration Items

## Resume Note 2026-05-14

Stop point:

- First-cut Java runtime conversion is implemented.
- Maven plugin is not included.
- Node-vs-Java sample summary parity is established through
  `scripts/compare-node-java-cli.sh`.
- Focused Node-vs-Java DOCX XML parity checks have been expanded and pass for
  the currently identified parser / renderer edge cases.
- Full upstream `remark` / `remark-gfm` parity is still tracked as migration
  work because new edge cases may be found during upstream follow-up.

Recommended first command after resuming:

```bash
mvn test
scripts/compare-node-java-cli.sh
scripts/roundtrip-md-docx-md.sh
```

Recommended next implementation slice:

- Continue parity hardening against upstream fixtures and generated DOCX XML.
- Use the split renderer classes for targeted parity fixes instead of expanding
  `MikuMd2docxCore`.
- Add focused Node-vs-Java comparison cases for remaining remark / remark-gfm
  edge cases before implementing each behavior.
- Treat the committed helper-class split and parity coverage as the current
  checkpoint before starting larger parser changes.
- Keep the focused Markdown -> DOCX -> Markdown smoke current as the first
  cross-tool round-trip guard.
- Keep latest relevant upstream Node `miku-md2docx` state and
  `miku-ms-office-core-java` integration current when upstream package helpers
  evolve.

## Completed

- Maven Java 8 runtime skeleton.
- Executable shaded jar packaging.
- Distribution zip packaging.
- Thin CLI entrypoint with upstream option names.
- Callable core API.
- DOCX package writer with core package entries, styles, numbering, relationships, and image media entries.
- DOCX package writer, relationship helpers, XML helpers, OOXML primitives, summary formatter, and image asset helpers split out of `MikuMd2docxCore`.
- Render state, Markdown text helpers, Markdown block rendering, and inline rendering split out of `MikuMd2docxCore`.
- Markdown line scanning and Markdown block rendering split between `MarkdownRenderer` and `MarkdownBlockRenderer`.
- GFM autolink literals for `https://`, `www.`, and email addresses.
- Setext headings, soft line breaks, and Markdown hard breaks.
- Tilde fenced code blocks.
- Indented code blocks.
- Upstream-compatible reference definitions, reference links, and reference images.
- Escaped Markdown punctuation and common HTML entity handling.
- Nested blockquote paragraph grouping.
- Blockquote child list/code omission behavior that matches current upstream.
- List child handling that matches current upstream: nested lists render, while
  additional paragraphs/code children are omitted.
- Supported HTML block/inline edge handling, including standalone `<br>` block
  paragraph style and split inline HTML formatting.
- GFM table alignment / escaped pipe behavior: alignment is not emitted in
  OOXML, and escaped pipes stay inside the cell.
- Link and image title attribute behavior: titles are not emitted in DOCX and
  are not included in relationship targets or image paths.
- Upstream summary field vocabulary.
- First-cut Markdown block handling for headings, paragraphs, lists, tables, code blocks, blockquotes, horizontal rules, and front matter.
- First-cut inline handling for bold, italic, strike, inline code, links, limited HTML, and images.
- Focused JUnit tests.
- Focused Markdown -> DOCX -> Markdown round-trip smoke script using
  `miku-docx2md-java` as the reverse converter.
- Upstream class, CLI, and test mapping documents.
- Project-local miku-soft reference document added under `docs/`, replacing
  copied shared basic documents.
- Followed upstream Node `miku-md2docx` package version `0.9.1`, commit
  `4d024794fa91d44174a76abb114aba3731768077`.
- Vendored `miku-ms-office-core-java` `0.5.1` under
  `vendor/miku-ms-office-core-java/` and unpack it during Maven
  `generate-sources`.
- Delegated product-neutral XML escaping, OPC relationship XML, OPC content
  type XML, and ZIP package writing to `miku-ms-office-core-java`.
- Followed upstream release `v1.0.0`, package version `1.0.0`, commit
  `65d26eaf67f37c126261031fd91409a6e6afa5a8`.
- Added `word/settings.xml` with Word compatibility mode 15.
- Added structural DOCX template reuse through core options and CLI
  `--template <docx>`.
- Added `remoteImages` and `remoteImageDetails` summary parity.
- Followed upstream release `v1.0.1`, package version `1.0.1`, commit
  `fc13a426ddc5e184d4f0b7b2c2f7c12efd5bc00a`.
- Updated the published vendored `miku-ms-office-core-java` jar to `v0.6.0`
  and added product-level regression coverage for supplementary Unicode and
  invalid XML character sanitization.

## Pending

- Complete Markdown AST parsing parity with upstream `remark` behavior.
- Complete OOXML block and inline renderer parity for edge cases.
- Complete image embedding parity for all upstream image cases.
- Add byte / XML-level fixture parity tests from upstream.
- Keep Node-vs-Java parity checks current as upstream fixtures evolve.
- Broaden Markdown -> DOCX -> Markdown round-trip coverage after the core
  parser / renderer behavior is stable enough for more source fixtures.
- No previously identified focused parser / renderer parity case remains open.
- Add new focused comparison cases when new upstream `remark` / `remark-gfm`
  edge cases are found.
- Decide whether the current Java line scanner should evolve into a small
  Java AST/token layer for remaining remark parity.
- Helper-class split and focused parity coverage have been committed as the
  current checkpoint.
- Keep staged adoption of `miku-ms-office-core-java` current for any future
  product-neutral ZIP, OPC relationship, content type, part path, and XML helper
  behavior. Keep DOCX assembly and Markdown conversion semantics in this
  repository.

## Follow-up Candidates

- Add Maven plugin support after runtime core and CLI parity are stable.
- Re-check `miku-ms-office-core-java` release version before future package
  helper changes.

## Latest Verification

Run:

```bash
mvn test
scripts/compare-node-java-cli.sh
scripts/roundtrip-md-docx-md.sh
```

Latest checked on 2026-07-18:

- `mvn test`: 25 tests passed.
- `mvn package`: passed through the Node-vs-Java comparison build.
- `scripts/compare-node-java-cli.sh`: passed against upstream Release `v1.0.1`,
  package version `1.0.1`, commit
  `fc13a426ddc5e184d4f0b7b2c2f7c12efd5bc00a`; settings, remote-image, and
  template-mode comparisons are enforced.
- `scripts/roundtrip-md-docx-md.sh`: passed with local
  `../miku-docx2md-java/target/miku-docx2md-1.0.0.jar`.
- `java -jar target/miku-md2docx-java-1.0.1.jar --version`: prints `1.0.1`.
- Packaged Maven metadata reports `miku-ms-office-core` `0.6.0`.

Previously checked on 2026-06-28:

- `mvn test`: 23 tests passed.
- `mvn package`: passed.
- `scripts/compare-node-java-cli.sh`: passed against upstream Node package
  version `0.9.1`, commit `4d024794fa91d44174a76abb114aba3731768077`.
- `scripts/roundtrip-md-docx-md.sh`: passed with local
  `../miku-docx2md-java/target/miku-docx2md-1.0.0.jar`.
- `java -jar target/miku-md2docx-java-0.9.1.jar --version`: printed `0.9.1`.
- `target/miku-md2docx-java-0.9.1.jar` contains
  `jp/igapyon/mikumsofficecore/ZipPackage.class`,
  `OpcRelationships.class`, `OpcContentTypes.class`, and `XmlHelper.class`.

Latest checked on 2026-05-17:

- `mvn test`: 23 tests passed.
- `mvn package`: passed through `scripts/compare-node-java-cli.sh`.
- `scripts/compare-node-java-cli.sh`: passed against local upstream package
  version `0.8.0`, commit `77b798a7848df8da841527446245ebed4029f0bb`.
- `scripts/roundtrip-md-docx-md.sh`: passed with local
  `../miku-docx2md-java/target/miku-docx2md-1.0.0.jar`.

Latest checked on 2026-05-14:

- `mvn test`: 7 tests passed, covering CLI help/version/conversion, DOCX package entries, summary vocabulary, deterministic output, representative OOXML structures, and embedded image bytes.
- `mvn test`: later expanded to 11 tests, adding image format, resize, raw HTML, and complex summary coverage.
- `scripts/compare-node-java-cli.sh`: sample Node and Java summary outputs matched.
- `scripts/compare-node-java-cli.sh`: now also writes normalized XML diff artifacts for key DOCX entries.
- `scripts/compare-node-java-cli.sh`: representative `word/document.xml`, `word/_rels/document.xml.rels`, `word/styles.xml`, and `word/numbering.xml` diffs are empty and enforced.
- `scripts/compare-node-java-cli.sh`: upstream `tests/fixtures/smoke.md` summary and key DOCX XML diffs are empty and enforced.
- `scripts/compare-node-java-cli.sh`: image-embedded fixture summary, key DOCX XML, `[Content_Types].xml`, and `word/media/image-1.png` byte comparisons are enforced and pass.
- `scripts/compare-node-java-cli.sh`: image-formats fixture summary, key DOCX XML, `[Content_Types].xml`, and media byte comparisons for GIF, JPEG, WebP, unknown binary, and large PNG are enforced and pass.
- `scripts/compare-node-java-cli.sh`: links-html fixture summary and key DOCX XML comparisons for duplicate heading anchors, unresolved internal links, supported raw HTML, and unsupported raw HTML fallback are enforced and pass.
- Core split started: `SummaryFormatter`, `XmlUtils`, `OoxmlPrimitives`, `RunStyle`, and `ImageAssets` now hold responsibilities that previously lived in `MikuMd2docxCore`.
- Core split continued: `DocxPackageBuilder`, `Relationship`, and `Relationships` now hold package writing and relationship responsibilities that previously lived in `MikuMd2docxCore`.
- Core split continued: `RenderState`, `MarkdownText`, `MarkdownRenderer`, and `InlineRenderer` now hold render state, heading/link text normalization, block rendering, and inline rendering responsibilities that previously lived in `MikuMd2docxCore`.
- Block renderer split continued: `MarkdownBlockRenderer` now owns heading, list, table, blockquote, rule, paragraph, code block, and unsupported HTML block rendering.
- `scripts/compare-node-java-cli.sh`: frontmatter-code fixture summary and key DOCX XML comparisons are enforced and pass.
- `scripts/compare-node-java-cli.sh`: autolink fixture summary and key DOCX XML comparisons for `https://`, `www.`, and email addresses are enforced and pass.
- `scripts/compare-node-java-cli.sh`: setext-breaks fixture summary and key DOCX XML comparisons for setext headings, soft line breaks, and Markdown hard breaks are enforced and pass.
- `scripts/compare-node-java-cli.sh`: tilde-code fixture summary and key DOCX XML comparisons for tilde fenced code blocks are enforced and pass.
- `scripts/compare-node-java-cli.sh`: indented-code fixture summary and key DOCX XML comparisons for indented code blocks are enforced and pass.
- `scripts/compare-node-java-cli.sh`: reference fixture summary and key DOCX XML comparisons for reference definitions, reference links, and reference images are enforced and pass.
- `mvn test`: later expanded to 17 tests, adding escaped Markdown punctuation and HTML entity handling coverage.
- `scripts/compare-node-java-cli.sh`: escapes-entities fixture summary and key DOCX XML comparisons for escaped punctuation and HTML entities are enforced and pass.
- `mvn test`: later expanded to 18 tests, adding nested blockquote paragraph grouping coverage.
- `scripts/compare-node-java-cli.sh`: nested-blockquote fixture summary and key DOCX XML comparisons are enforced and pass.
- `mvn test`: later expanded to 19 tests, adding blockquote child list/code omission coverage.
- `scripts/compare-node-java-cli.sh`: blockquote-children fixture summary and key DOCX XML comparisons are enforced and pass.
- `mvn test`: later expanded to 20 tests, adding list child paragraph/code omission and nested-list coverage.
- `scripts/compare-node-java-cli.sh`: list-children fixture summary and key DOCX XML comparisons are enforced and pass.
- `mvn test`: later expanded to 21 tests, adding supported HTML block/inline edge coverage.
- `scripts/compare-node-java-cli.sh`: html-edge fixture summary and key DOCX XML comparisons are enforced and pass.
- `mvn test`: later expanded to 22 tests, adding GFM table escaped-pipe coverage.
- `scripts/compare-node-java-cli.sh`: table-edge fixture summary and key DOCX XML comparisons are enforced and pass.
- `mvn test`: later expanded to 23 tests, adding link/image title attribute coverage.
- `scripts/compare-node-java-cli.sh`: title-attr fixture summary and key DOCX XML comparisons are enforced and pass.
