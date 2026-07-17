# Upstream Snapshot

## Source

- Upstream repository: <https://github.com/igapyon/miku-md2docx>
- Upstream branch: `devel`
- Current compatibility tag: `v1.0.0`
- Snapshot tag checked during initial Java repository creation: `v0.5.0.1`
- Snapshot commit checked during initial Java repository creation: `5fa828a984c6929d0911fde62ebb44d81f92c9a7`
- Latest upstream follow-up checked locally: package version `1.0.0`, commit `65d26eaf67f37c126261031fd91409a6e6afa5a8`
- Latest upstream change summary: structural DOCX template reuse, `word/settings.xml` compatibility mode 15, remote-image-specific summary details, and runtime/release bundle surfaces.
- Local upstream checkout used for comparison: `workplace/upstream-miku-md2docx-v1.0.0`
- Upstream reference method: local checkout, not vendored

## Sister Reference

- Primary same-layer sister Java repository: `../miku-docx2md-java`
- Additional same-layer sister repositories observed locally: `../miku-xlsx2md-java`, `../miku-indexgen-java`, `../mikuproject-java`

The sister project influenced these initial repository-shape decisions:

- Maven as the build tool
- Java 8 source and target compatibility
- JUnit Jupiter tests
- thin CLI entrypoint delegating to core runtime
- single executable shaded runtime jar for the initial Maven-plugin-free scope
- distribution zip packaging
- `docs/` mapping and status documents
- `workplace/.gitkeep` as the only tracked workplace file

Maven plugin support was explicitly kept out of the initial scope.
