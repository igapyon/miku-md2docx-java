# TODO

## Resume Note 2026-05-17

Current state:

- Java straight conversion has progressed beyond the initial skeleton into a first-cut runtime conversion.
- Maven plugin support remains intentionally out of scope.
- Upstream `miku-md2docx` core/CLI follow-up has been checked at package
  version `0.8.0`, commit `77b798a7848df8da841527446245ebed4029f0bb`.
- Java runtime version is now `0.8.0.1`; CLI help follows the upstream `0.8.0`
  Arguments / Required options / Examples / Notes structure.
- Latest verification:
  - `mvn test`: 23 tests passed.
  - `mvn package`: passed.
  - `scripts/compare-node-java-cli.sh`: passed; focused Node and Java summary / key DOCX XML outputs matched.
  - `scripts/roundtrip-md-docx-md.sh`: passed with local `../miku-docx2md-java` jar.

Recommended next step:

1. Continue hardening feature parity against upstream fixtures and generated DOCX XML.
2. Add focused Node-vs-Java comparison cases for the remaining remark / remark-gfm edge cases.
3. Use the split renderer classes for the next targeted parity fixes instead of expanding `MikuMd2docxCore`.
4. Treat the committed helper-class split and parity coverage as the current checkpoint before starting larger parser work.

Important files to inspect first when resuming:

- `src/main/java/jp/igapyon/mikumd2docx/core/MikuMd2docxCore.java`
- `src/test/java/jp/igapyon/mikumd2docx/MikuMd2docxCoreTest.java`
- `scripts/compare-node-java-cli.sh`
- `docs/remaining-migration-items.md`
- `docs/upstream-class-mapping.md`

## Initial Java Conversion

- Complete full upstream Markdown parser behavior from `src/ts/markdown-parser.ts`.
- Complete full upstream OOXML package generation parity from `src/ts/docx-package.ts` and related renderers.
- Add byte / XML-level fixture parity tests based on upstream `tests/fixtures/smoke.md`.
- Keep Node-vs-Java package parity checks current as upstream fixtures evolve.
- Decide whether Maven plugin support should be added after runtime parity is stable.

## Remaining Parser / Renderer Parity

- No previously identified focused parser / renderer parity case remains open.
- Continue adding focused Node-vs-Java comparison cases when new upstream
  `remark` / `remark-gfm` edge cases are found.
- Decide whether the Java line scanner remains sufficient or whether a small Java AST/token layer is needed for the remaining cases.
- Keep behavioral changes in `MarkdownRenderer`, `MarkdownBlockRenderer`, `InlineRenderer`, and related helpers; avoid growing `MikuMd2docxCore`.
- Keep the focused Markdown -> DOCX -> Markdown round-trip smoke current with
  `miku-docx2md-java` when conversion behavior changes.

## Packaging / Repo Checkpoint

- Helper-class split, comparison script expansion, tests, and docs have been committed as the current checkpoint.
- Before the next parser / renderer change, rerun:
  - `mvn test`
  - `mvn package`
  - `scripts/compare-node-java-cli.sh`
  - `scripts/roundtrip-md-docx-md.sh`

## Completed In Current Java First-Cut

- Ported first-cut block handling for headings, paragraphs, lists, tables, code blocks, blockquotes, horizontal rules, and front matter.
- Ported first-cut inline handling for bold, italic, strike, inline code, links, limited HTML, and images.
- Ported first-cut OOXML package entries, document relationships, styles, numbering, external links, internal bookmarks, and image media entries.
- Added focused tests for representative OOXML structures and embedded image bytes.
- Added a first-cut Node-vs-Java CLI comparison script.
- Added normalized DOCX XML diff artifact generation to the Node-vs-Java CLI comparison script.
- Hardened the comparison script so representative DOCX XML diffs fail the script when non-empty.
- Extended the comparison script to enforce summary and key DOCX XML parity for upstream `tests/fixtures/smoke.md`.
- Extended the comparison script to enforce summary, key DOCX XML, `[Content_Types].xml`, and `word/media/image-1.png` byte parity for an image-embedded fixture.
- Extended the comparison script to enforce package parity for GIF, JPEG, WebP, unknown binary image, and large resized PNG cases.
- Extended the comparison script to enforce package parity for duplicate heading anchors, unresolved internal links, supported raw HTML, and unsupported raw HTML fallback.
- Split summary formatting, XML helpers, OOXML primitives, run style, and image asset helpers out of `MikuMd2docxCore`.
- Split DOCX package writing, package templates, ZIP output, and relationship XML/model helpers out of `MikuMd2docxCore`.
- Split render state, Markdown text helpers, Markdown block rendering, and inline rendering out of `MikuMd2docxCore`.
- Split Markdown block rendering into `MarkdownBlockRenderer`, leaving `MarkdownRenderer` focused on line scanning and orchestration.
- Added Node-vs-Java comparison coverage for front matter plus fenced code block scanning.
- Added GFM autolink literal support for `https://`, `www.`, and email addresses, with Java tests and Node-vs-Java comparison coverage.
- Added setext heading, soft line break, and Markdown hard break support, with Java tests and Node-vs-Java comparison coverage.
- Added tilde fenced code block support, with Java tests and Node-vs-Java comparison coverage.
- Added indented code block support, with Java tests and Node-vs-Java comparison coverage.
- Added upstream-compatible reference definition/link/image handling, with Java tests and Node-vs-Java comparison coverage.
- Added upstream-compatible escaped Markdown punctuation and HTML entity handling, with Java tests and Node-vs-Java comparison coverage.
- Added upstream-compatible nested blockquote paragraph grouping, with Java tests and Node-vs-Java comparison coverage.
- Added upstream-compatible blockquote child list/code omission behavior, with Java tests and Node-vs-Java comparison coverage.
- Added upstream-compatible list child handling: nested lists render, while additional paragraphs/code children are omitted, with Java tests and Node-vs-Java comparison coverage.
- Added upstream-compatible supported HTML block/inline edge handling, including standalone `<br>` block paragraph style and split inline HTML with formatting, with Java tests and Node-vs-Java comparison coverage.
- Added upstream-compatible GFM table alignment/escaped pipe behavior: alignment is not emitted in OOXML, and escaped pipes stay inside the cell, with Java tests and Node-vs-Java comparison coverage.
- Added upstream-compatible link/image title attribute behavior: titles are not emitted in DOCX and are not included in relationship targets or image paths, with Java tests and Node-vs-Java comparison coverage.
- Added a focused Markdown -> DOCX -> Markdown round-trip smoke script using
  `miku-md2docx-java` and `miku-docx2md-java`.

## Out Of Initial Scope

- Maven plugin module.
- Browser UI behavior from upstream.
- Java-side features that do not exist in upstream.
