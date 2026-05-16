# Upstream Follow-up Log

## 2026-05-14

- Created initial Java runtime repository from upstream `devel` HEAD,
  `v0.5.0.1` / `5fa828a984c6929d0911fde62ebb44d81f92c9a7`.
- Used `../miku-docx2md-java` as the closest same-layer Java companion shape
  reference.
- Kept Maven plugin support out of initial scope by explicit request.
- Added minimal CLI and DOCX package generation so Maven tests and packaging
  have executable behavior from the start.

## Pending Upstream Checks

- Compare Java CLI stdout, stderr, exit codes, and generated `.docx` package
  entries against upstream `scripts/miku-md2docx-cli.mjs`.
- Port upstream parser and OOXML renderer behavior before claiming content
  parity.

## 2026-05-17 Upstream Core/Node Follow-up

- Checked local upstream `../miku-md2docx` at package version `0.8.0`, commit
  `77b798a7848df8da841527446245ebed4029f0bb`.
- Upstream main application now owns product core, CLI, and CLI release bundle;
  browser UI files have been separated to `miku-md2docx-web`.
- Upstream CLI imports the built runtime from `dist/core.js` and expanded
  `--help` with Arguments, Required options, Examples, and Notes sections.
- Updated Java runtime version to `0.8.0.1` and aligned Java CLI help text with
  the upstream `0.8.0` structure while keeping jar-based examples.

## 2026-05-14 Follow-up

- Expanded the Java core from a minimal placeholder into a first-cut
  straight-conversion runtime for Markdown block structures, inline formatting,
  links, limited HTML, relationships, styles, numbering, and image media.
- Added focused Java tests for representative OOXML and embedded image behavior.
- Remaining work is now parity hardening rather than initial executable shape.

## 2026-05-14 Parity Expansion

- Added Java coverage for upstream image format cases, large image resizing,
  remote-image missing behavior, supported raw HTML, unsupported raw HTML, and
  complex summary stability.
- Added `scripts/compare-node-java-cli.sh` as the first Node-vs-Java CLI
  comparison entrypoint.

## 2026-05-14 Stop Point

- Verified `mvn test`, `mvn package`, and `scripts/compare-node-java-cli.sh`.
- Current comparison script checks summary parity for a representative sample.
- Resume by adding DOCX XML diff checks to the comparison script, then use those
  diffs to continue renderer parity work.

## 2026-05-14 XML Diff Setup

- Extended `scripts/compare-node-java-cli.sh` to unzip Node and Java DOCX files,
  normalize key XML entries, and write diff artifacts for `word/document.xml`,
  `word/_rels/document.xml.rels`, `word/styles.xml`, and `word/numbering.xml`.
- Representative summary and key XML diffs are now script failure conditions.
- Fixed Java styles and numbering template output so the representative sample
  matches upstream key XML entries.

## 2026-05-14 Smoke Fixture Parity

- Extended `scripts/compare-node-java-cli.sh` to run both the representative
  generated sample and upstream `tests/fixtures/smoke.md`.
- Fixed Java list summary counting for nested list blocks so smoke fixture
  summary matches upstream.
- Fixed Java task-list rendering so `[ ] ` and `[x] ` prefixes are emitted as
  separate runs like upstream.
- Verified both comparison cases have empty key XML diffs for `word/document.xml`,
  `word/_rels/document.xml.rels`, `word/styles.xml`, and `word/numbering.xml`.

## 2026-05-14 Embedded Image Parity

- Added an `image-embedded` comparison case to `scripts/compare-node-java-cli.sh`.
- The script now creates a local PNG fixture under `target/`, runs both Node and
  Java CLI paths, and enforces summary, key DOCX XML, `[Content_Types].xml`, and
  `word/media/image-1.png` byte parity.
- Verified the embedded PNG media bytes match exactly between Node and Java
  outputs.

## 2026-05-14 Image Format Parity

- Added an `image-formats` comparison case to `scripts/compare-node-java-cli.sh`.
- The case covers GIF, JPEG, WebP, unknown binary image paths, and a large PNG
  that exercises resize summary behavior.
- The comparison now enforces summary parity, key DOCX XML parity,
  `[Content_Types].xml` parity, and byte parity for all generated media entries
  in that case.

## 2026-05-14 Link And Raw HTML Parity

- Added a `links-html` comparison case to `scripts/compare-node-java-cli.sh`.
- The case covers duplicate heading anchors, unresolved internal links, external
  links, supported raw HTML tags, missing images from raw HTML, and unsupported
  raw HTML fallback.
- Adjusted Java unsupported raw HTML handling so summary counts and generated
  key DOCX XML match upstream for this case.

## 2026-05-14 Core Split Start

- Split summary formatting, XML helpers, OOXML primitive generation, run style,
  and image asset helpers out of `MikuMd2docxCore`.
- Kept parser, block rendering, inline rendering, package writing, and
  relationship writing in `MikuMd2docxCore` for now.
- Verified the split with `mvn test` and `scripts/compare-node-java-cli.sh`.

## 2026-05-14 Package And Relationship Split

- Split DOCX package assembly, package templates, deterministic ZIP writing, and
  relationship XML/model helpers out of `MikuMd2docxCore`.
- `MikuMd2docxCore` now delegates package output to `DocxPackageBuilder` and
  relationship type constants/XML writing to `Relationships`.
- Parser, block renderer, and inline renderer responsibilities remain in
  `MikuMd2docxCore` as the next straight-conversion split target.
- Verified the split with `mvn test`, `mvn package`, and
  `scripts/compare-node-java-cli.sh`.

## 2026-05-14 Renderer Split

- Split render state into `RenderState`.
- Split heading/link plain-text and anchor normalization into `MarkdownText`.
- Split line-oriented Markdown block rendering and heading bookmark collection
  into `MarkdownRenderer`.
- Split inline formatting, links, raw HTML inline handling, and image rendering
  into `InlineRenderer`.
- `MikuMd2docxCore` is now a small callable facade over the renderer,
  package builder, and summary formatter.
- Verified the split with `mvn test`, `mvn package`, and
  `scripts/compare-node-java-cli.sh`.

## 2026-05-14 Block Renderer Split

- Split Markdown block rendering out of `MarkdownRenderer` into
  `MarkdownBlockRenderer`.
- `MarkdownRenderer` now focuses on line scanning, front matter skipping, code
  fence state, and dispatching to block renderers.
- Added a `frontmatter-code` Node-vs-Java comparison case for front matter,
  fenced code blocks, paragraphs, headings, and blockquotes.
- Verified the split and added comparison case with `mvn test`, `mvn package`,
  and `scripts/compare-node-java-cli.sh`.

## 2026-05-14 GFM Autolink Parity

- Added Java inline support for GFM autolink literals covering `https://`,
  `www.`, and email address text.
- Matched upstream relationship targets for autolinks:
  `https://...` remains unchanged, `www.` becomes `http://...`, and email
  addresses become `mailto:...`.
- Added focused Java test coverage and an `autolink` Node-vs-Java comparison
  case.
- Verified with `mvn test`, `mvn package`, and
  `scripts/compare-node-java-cli.sh`.

## 2026-05-14 Setext And Break Parity

- Added Java line-scanner support for setext headings using `===` and `---`
  underline syntax.
- Added paragraph line grouping so soft line breaks stay inside one paragraph.
- Added Markdown hard break support for two trailing spaces and trailing
  backslash line endings.
- Added focused Java test coverage and a `setext-breaks` Node-vs-Java
  comparison case.
- Verified with `mvn test`, `mvn package`, and
  `scripts/compare-node-java-cli.sh`.

## 2026-05-14 Tilde Fence Parity

- Added Java line-scanner support for `~~~` fenced code blocks in addition to
  backtick fences.
- Added focused Java test coverage and a `tilde-code` Node-vs-Java comparison
  case.
- Verified with `mvn test`, `mvn package`, and
  `scripts/compare-node-java-cli.sh`.

## 2026-05-14 Indented Code Parity

- Added Java line-scanner support for four-space and tab-indented code blocks.
- Added focused Java test coverage and an `indented-code` Node-vs-Java
  comparison case.
- Verified with `mvn test`, `mvn package`, and
  `scripts/compare-node-java-cli.sh`.

## 2026-05-14 Reference Syntax Parity

- Matched upstream behavior for reference definitions, reference links, shortcut
  references, and reference images.
- Reference definitions are collected for syntax recognition but are not
  rendered.
- Reference links render as text only, and reference images render as empty
  inline content, matching the current upstream renderer behavior.
- Added focused Java test coverage and a `reference` Node-vs-Java comparison
  case.
- Verified with `mvn test`, `mvn package`, and
  `scripts/compare-node-java-cli.sh`.

## 2026-05-14 Escape And Entity Parity

- Matched upstream behavior for Markdown backslash escapes for punctuation in
  plain inline text.
- Matched upstream generated XML for common HTML entities and numeric character
  references, including `&amp;`, `&copy;`, `&#x41;`, `&#65;`, and `&lt;...&gt;`.
- Prevented escaped bracket syntax such as `\[text\]` from being interpreted as
  shortcut reference syntax.
- Added focused Java test coverage and an `escapes-entities` Node-vs-Java
  comparison case.
- Verified with `mvn test` and `scripts/compare-node-java-cli.sh`.

## 2026-05-14 Nested Blockquote Parity

- Matched upstream behavior for contiguous blockquote lines: one blockquote
  summary entry, and separated Quote-style paragraphs inside the blockquote.
- Matched nested blockquote rendering by stripping repeated `>` prefixes and
  rendering nested paragraph content with the same Quote style.
- Fixed OOXML `xml:space="preserve"` detection for runs that contain a newline
  and end with whitespace, matching upstream generated XML for inline formatting
  after soft line breaks.
- Added focused Java test coverage and a `nested-blockquote` Node-vs-Java
  comparison case.
- Verified with `mvn test` and `scripts/compare-node-java-cli.sh`.

## 2026-05-14 Blockquote Child List And Code Parity

- Confirmed current upstream renderer omits list and code children inside a
  blockquote rather than rendering them as lists or code blocks.
- Kept Java blockquote handling aligned with that behavior: only paragraph-like
  quote content is rendered, and list/code children do not affect list,
  list-item, or code-block summary counts.
- Added focused Java test coverage and a `blockquote-children` Node-vs-Java
  comparison case.
- Verified with `mvn test` and `scripts/compare-node-java-cli.sh`.

## 2026-05-14 List Child Parity

- Confirmed current upstream list-item rendering uses the first paragraph and
  nested lists, but omits additional paragraph and code children inside the
  list item.
- Adjusted Java list scanning so blank-line continuations and indented
  paragraph/code children are consumed without rendering, while nested list
  lines still render at the expected list level.
- Added focused Java test coverage and a `list-children` Node-vs-Java
  comparison case.
- Verified with `mvn test` and `scripts/compare-node-java-cli.sh`.

## 2026-05-14 HTML Edge Parity

- Added a focused HTML edge fixture covering standalone supported HTML blocks
  and split inline HTML with Markdown formatting inside `<ins>` and `<a>`.
- Confirmed current upstream emits standalone `<br>` as a paragraph without
  `Normal` paragraph style, while other supported inline HTML in paragraph
  context keeps normal paragraph rendering.
- Adjusted Java paragraph rendering so standalone `<br>` matches that block
  paragraph style behavior.
- Added focused Java test coverage and an `html-edge` Node-vs-Java comparison
  case.
- Verified with `mvn test` and `scripts/compare-node-java-cli.sh`.

## 2026-05-14 GFM Table Edge Parity

- Confirmed current upstream table rendering does not emit column alignment
  into OOXML; alignment markers only affect Markdown parsing.
- Fixed Java table cell splitting so escaped pipes such as `a \| b` stay inside
  the same table cell and are rendered as `a | b`.
- Added focused Java test coverage and a `table-edge` Node-vs-Java comparison
  case.
- Verified with `mvn test` and `scripts/compare-node-java-cli.sh`.

## 2026-05-14 Link And Image Title Attribute Parity

- Confirmed current upstream does not emit Markdown link/image title attributes
  into generated DOCX.
- Fixed Java inline destination handling so Markdown titles are not included in
  hyperlink relationship targets or image paths.
- Added focused Java test coverage and a `title-attr` Node-vs-Java comparison
  case.
- Verified with `mvn test` and `scripts/compare-node-java-cli.sh`.
