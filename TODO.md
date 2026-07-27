# TODO

## AI Agent Current Tasks

This section tracks active work items for AI agents.
Update this section while working. Do not rewrite unrelated TODO items.

### Tasks

- [x] Follow upstream Node Release `v1.1.0` and update the Java runtime,
  CLI/help contract, exit-code tests, version, workflow, and documentation.
- [x] Embed complete dot-suffixed Release versions in JAR `--version` and help.
- [x] Complete GitHub Issue #10 with normal/template DEFLATE regression tests.
- [x] Complete the `miku-md2docx-java` scope of GitHub Issue #9: Release asset
  help, parent-directory creation, exit-code/stream contracts, generated
  artifacts, and multiline list-item continuation parity.
- [x] Compare upstream Node Release `v1.0.1` with the previous `v1.0.0`
  compatibility source.
- [x] Replace vendored `miku-ms-office-core-java` `0.5.1` with published
  Release `v0.6.0` and record its SHA-256.
- [x] Add product-level supplementary Unicode XML regression coverage.
- [x] Update the Java runtime, Maven project, README, mappings, snapshot,
  follow-up, and migration docs to `1.0.1`.
- [x] Fix the upstream compatibility source to Release `v1.0.0`.
- [x] Compare upstream `v1.0.0` with the previous `0.9.1` compatibility commit.
- [x] Port settings compatibility mode 15, DOCX template reuse, and remote-image
  summary details to Java core and CLI.
- [x] Extend Node-vs-Java checks for settings, remote images, and template mode.
- [x] Update version, README, mappings, snapshot, follow-up, and migration docs.
- [x] Create igapyon agent state files for the corrected implementation goal.
- [x] Check the latest upstream Node `miku-md2docx` version / commit and record
  the follow-up target.
- [x] Compare current Java behavior and docs against the latest upstream Node
  `miku-md2docx` state.
- [x] Vendor `miku-ms-office-core-java` using the `miku-md2xlsx-java` managed
  release jar pattern.
- [x] Move appropriate product-neutral XML / OPC relationship / content type /
  part path helpers to `miku-ms-office-core-java`.
- [x] Move ZIP package writing to `miku-ms-office-core-java` after smaller
  helper migrations are covered.
- [x] Update README, TODO, mapping docs, decisions, and handoff to the
  implemented state.
- [x] Run relevant verification: `mvn test`, `mvn package`,
  `scripts/compare-node-java-cli.sh`, and `scripts/roundtrip-md-docx-md.sh`
  when prerequisites are available.

### Blockers

- None.

### Retry Log

Use this section only when the same task or error is repeated.
If the same failure appears 3 times, stop and ask the user.

- None.

## Resume Note 2026-07-27

Current state:

- Java runtime and Maven project now track upstream Release `v1.1.0`, commit
  `a25d302c742e6950183d60e0cda88f97bc65a265`.
- GitHub Issues #9 and #10 are implemented locally.
- Generated DOCX entries use ZIP DEFLATE for normal and template paths.
- The CLI creates output parent directories, rejects unknown short and long
  options with exit code 2, and documents Release asset execution and stream
  contracts.
- Release suffixes such as `1.1.0.2` are embedded in JAR version/help output.
- Bullet and ordered list first-paragraph continuation lines match Node output.

Latest verification:

- `mvn test`: 27 tests passed.
- `mvn package`: passed and produced the runtime jar, sources jar, and
  distribution zip.
- `mvn package -Dmiku.release.version=1.1.0.2`: passed; runtime version/help
  and Manifest reported `1.1.0.2`.
- `scripts/compare-node-java-cli.sh`: passed, including the new
  `list-continuation` case, against the published upstream
  `miku-md2docx-1.1.0.mjs` Release Asset.
- `scripts/roundtrip-md-docx-md.sh`: passed.
- Packaged CLI smoke: missing output parents were created and all nine
  generated DOCX entries were reported as DEFLATE by `unzip`.

## Resume Note 2026-07-18

Current state:

- Java runtime and Maven project track upstream Release `v1.0.1`, commit
  `fc13a426ddc5e184d4f0b7b2c2f7c12efd5bc00a`.
- Vendored `miku-ms-office-core-java` tracks published Release `v0.6.0`; XML
  sanitization preserves valid supplementary Unicode and removes invalid XML
  characters.
- Default DOCX packages include `word/settings.xml` with Word compatibility
  mode 15.
- Core options and CLI support structural DOCX template reuse through
  `templateDocx` / `--template <docx>`.
- Remote image URLs use `remoteImages` / `remoteImageDetails` while remaining
  included in `missingImages`.
- Latest verification: 25 JUnit tests passed; Node-vs-Java summary and key
  OOXML comparisons passed against the `v1.0.1` checkout, including settings,
  remote-image, and template-mode cases; Maven packaging and the focused
  Markdown -> DOCX -> Markdown round-trip passed.

Recommended next step:

1. Continue focused `remark` / `remark-gfm` parity hardening only when a new
   upstream edge case is identified.
2. Keep Release tags—not moving `devel` HEAD—as compatibility checkpoints.
3. Keep Maven plugin support out of scope until runtime parity is judged stable.

## Historical Resume Note 2026-05-17

Current state:

- Java straight conversion has progressed beyond the initial skeleton into a first-cut runtime conversion.
- Maven plugin support remains intentionally out of scope.
- Upstream `miku-md2docx` core/CLI follow-up has been checked at package
  version `0.9.1`, commit `4d024794fa91d44174a76abb114aba3731768077`.
- Java runtime version is now `0.9.1`; CLI help follows the upstream `0.9.1`
  Arguments / Required options / Examples / Notes structure.
- `miku-ms-office-core-java` `0.5.1` is vendored under
  `vendor/miku-ms-office-core-java/` and unpacked during Maven
  `generate-sources`.
- Latest verification:
  - `mvn test`: 23 tests passed.
  - `mvn package`: passed.
  - `scripts/compare-node-java-cli.sh`: passed; focused Node and Java summary / key DOCX XML outputs matched.
  - `scripts/roundtrip-md-docx-md.sh`: passed with local `../miku-docx2md-java` jar.
  - `java -jar target/miku-md2docx-java-0.9.1.jar --version`: printed `0.9.1`.

Recommended next step:

1. Continue hardening feature parity against upstream fixtures and generated DOCX XML.
2. Add focused Node-vs-Java comparison cases for the remaining remark / remark-gfm edge cases.
3. Use the split renderer classes for the next targeted parity fixes instead of expanding `MikuMd2docxCore`.
4. Treat the committed helper-class split and parity coverage as the current checkpoint before starting larger parser work.
5. Keep the `miku-ms-office-core-java` vendored jar and upstream Node snapshot
   current when either project publishes a new release.

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
- `miku-ms-office-core-java` has been integrated using the `miku-md2xlsx-java`
  pattern:
  vendor the release jar under `vendor/miku-ms-office-core-java/`, record
  release metadata and SHA-256, and unpack the jar during Maven
  `generate-sources`.
- Before the next parser / renderer change, rerun:
  - `mvn test`
  - `mvn package`
  - `scripts/compare-node-java-cli.sh`
  - `scripts/roundtrip-md-docx-md.sh`

## miku-ms-office-core-java Integration

- Treat `miku-ms-office-core-java` as the owner for
  product-neutral ZIP / OPC / relationship / content type / XML helper behavior.
- Keep DOCX document assembly, Markdown conversion semantics, Word-specific
  templates, and summary policy in `miku-md2docx-java`.
- Uses the managed vendored release jar workflow already used by
  `miku-md2xlsx-java` over depending on Maven repository publication.
- Implemented integration:
  1. `vendor/miku-ms-office-core-java/miku-ms-office-core-0.6.0.jar`.
  2. `vendor/miku-ms-office-core-java/README.md` records source repository,
     release tag, jar file name, and SHA-256.
  3. `miku.ms.office.core.version` and Maven antrun unpack wiring expand the
     vendored jar into `${project.build.outputDirectory}` during
     `generate-sources`.
  4. `XmlUtils` delegates XML escaping to `XmlHelper`.
  5. Relationship and content type XML generation delegate to `OpcRelationships`
     and `OpcContentTypes`.
  6. DOCX ZIP package writing delegates to `ZipPackage`.
- Preserve the current Node-vs-Java comparison script as the regression gate
  for every migration slice.

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
