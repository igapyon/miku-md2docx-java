# Upstream Snapshot

## Source

- Upstream repository: <https://github.com/igapyon/miku-md2docx>
- Upstream branch: `devel`
- Snapshot tag checked during initial Java repository creation: `v0.5.0.1`
- Snapshot commit checked during initial Java repository creation: `5fa828a984c6929d0911fde62ebb44d81f92c9a7`
- Latest upstream follow-up checked locally: package version `0.9.1`, commit `4d024794fa91d44174a76abb114aba3731768077`
- Latest upstream change summary: main application uses `miku-ms-office-core` 0.5.1 for DOCX package writing and CLI / bundle version checks follow package metadata.
- Local upstream checkout used for inventory: `../miku-md2docx`
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
