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
