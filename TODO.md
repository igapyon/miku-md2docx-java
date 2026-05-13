# TODO

## Resume Note 2026-05-14

Current state:

- Java straight conversion has progressed beyond the initial skeleton into a first-cut runtime conversion.
- Maven plugin support remains intentionally out of scope.
- Latest verification:
  - `mvn test`: 11 tests passed.
  - `mvn package`: passed.
  - `scripts/compare-node-java-cli.sh`: passed; sample Node and Java summary outputs matched.

Recommended next step:

1. Harden `scripts/compare-node-java-cli.sh` to unzip both generated DOCX files and compare normalized XML entries under `word/`.
2. Add fixture-based parity using upstream `../miku-md2docx/tests/fixtures/smoke.md`.
3. Split the large `MikuMd2docxCore` into traceable Java class groups after parity coverage is strong enough:
   - Markdown parser / block scanner
   - OOXML block renderer
   - OOXML inline renderer
   - DOCX package writer
   - Image asset helpers

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
- Harden CLI comparison script with DOCX XML diff checks against upstream `scripts/miku-md2docx-cli.mjs`.
- Decide whether Maven plugin support should be added after runtime parity is stable.

## Completed In Current Java First-Cut

- Ported first-cut block handling for headings, paragraphs, lists, tables, code blocks, blockquotes, horizontal rules, and front matter.
- Ported first-cut inline handling for bold, italic, strike, inline code, links, limited HTML, and images.
- Ported first-cut OOXML package entries, document relationships, styles, numbering, external links, internal bookmarks, and image media entries.
- Added focused tests for representative OOXML structures and embedded image bytes.
- Added a first-cut Node-vs-Java CLI comparison script.

## Out Of Initial Scope

- Maven plugin module.
- Browser UI behavior from upstream.
- Java-side features that do not exist in upstream.
