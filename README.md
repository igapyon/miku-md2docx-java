# miku-md2docx-java

`miku-md2docx-java` is the Java straight-conversion runtime and CLI for
[`miku-md2docx`](https://github.com/igapyon/miku-md2docx).

The tool converts local Markdown files into editable Word `.docx` files. This
initial Java repository starts with the CLI runtime only. Maven plugin support
is intentionally out of the initial scope.

## Usage

Build:

```bash
mvn test
mvn package
```

Run:

```bash
java -jar target/miku-md2docx-java-0.5.0.1.jar ./sample.md --out ./sample.docx
```

Summary output:

```bash
java -jar target/miku-md2docx-java-0.5.0.1.jar ./sample.md --out ./sample.docx --summary
```

Show help or version:

```bash
java -jar target/miku-md2docx-java-0.5.0.1.jar --help
java -jar target/miku-md2docx-java-0.5.0.1.jar --version
```

## Current Scope

- Java source / target compatibility: `1.8`
- Build tool: Maven
- Test framework: JUnit Jupiter
- Primary verification: `mvn test`
- Runtime package: executable fat jar under `target/`
- Distribution package: `target/miku-md2docx-java-0.5.0.1-dist.zip`
- Maven plugin: out of initial scope

The current implementation is a first Java runtime skeleton with a thin CLI,
minimal DOCX package generation, and upstream vocabulary for the summary
fields. Full Markdown-to-OOXML parity with upstream remains a tracked migration
item.

GitHub Release asset workflow support is provided by
`.github/workflows/release-cli-runtime.yml`. It builds from `v*` tags or manual
`tag_name` dispatch and uploads the runtime jar plus sources jar.

## Upstream And Sister Reference

- Upstream Node.js / TypeScript repository: <https://github.com/igapyon/miku-md2docx>
- Upstream branch and compatibility source: `devel` HEAD
- Upstream snapshot checked locally: tag `v0.5.0.1`, commit `5fa828a984c6929d0911fde62ebb44d81f92c9a7`
- Local upstream checkout used for inventory: `../miku-md2docx`
- Primary sister Java project used as repository-shape reference: `../miku-docx2md-java`
- Additional same-layer sister references available locally: `../miku-xlsx2md-java`, `../miku-indexgen-java`, `../mikuproject-java`

See `docs/` for upstream snapshot, class, CLI, test, and migration status
documents.

## Repository Operation

`workplace/` is a local scratch area for upstream clones, generated comparison
outputs, extracted archives, and temporary verification artifacts. Only
`workplace/.gitkeep` is tracked.

`.mvn/jvm.config` is tracked for repository-local Maven JVM settings.

## License

Apache License 2.0. See [LICENSE](./LICENSE).
