# Upstream Snapshot

## Source

- Upstream repository: <https://github.com/igapyon/miku-md2docx>
- Upstream branch: `devel`
- Current compatibility tag: `v1.1.0`
- Snapshot tag checked during initial Java repository creation: `v0.5.0.1`
- Snapshot commit checked during initial Java repository creation: `5fa828a984c6929d0911fde62ebb44d81f92c9a7`
- Latest upstream follow-up checked publicly: package version `1.1.0`, commit `a25d302c742e6950183d60e0cda88f97bc65a265`
- Latest upstream change summary: DEFLATE package generation, Release
  Asset-oriented CLI contracts, automatic output-parent creation, separated
  exit codes, and complete Release-version propagation.
- Published upstream CLI used for the latest comparison:
  `miku-md2docx-1.1.0.mjs`
- Local upstream checkout used for unchanged fixtures and repository reference:
  `../miku-md2docx`
- Upstream reference method: published Release Asset plus local checkout, not
  vendored

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
