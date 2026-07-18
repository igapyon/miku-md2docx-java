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
java -jar target/miku-md2docx-java-1.0.1.jar ./sample.md --out ./sample.docx
```

Summary output:

```bash
java -jar target/miku-md2docx-java-1.0.1.jar ./sample.md --out ./sample.docx --summary
```

Template reuse:

```bash
java -jar target/miku-md2docx-java-1.0.1.jar ./sample.md --out ./sample.docx --template ./template.docx
```

Show help or version:

```bash
java -jar target/miku-md2docx-java-1.0.1.jar --help
java -jar target/miku-md2docx-java-1.0.1.jar --version
```

## Current Scope

- Java source / target compatibility: `1.8`
- Build tool: Maven
- Test framework: JUnit Jupiter
- Primary verification: `mvn test`
- Runtime package: executable fat jar under `target/`
- Distribution package: `target/miku-md2docx-java-1.0.1-dist.zip`
- Maven plugin: out of initial scope

The current implementation follows upstream release `v1.0.1`, including
minimal DOCX settings with Word compatibility mode 15, remote-image summary
details, structural DOCX template reuse, and supplementary Unicode preservation
through `miku-ms-office-core-java` `v0.6.0`. Full `remark` edge-case parity
remains a tracked migration item.

GitHub Release asset workflow support is provided by
`.github/workflows/release-cli-runtime.yml`. It builds from `v*` tags or manual
`tag_name` dispatch and uploads the runtime jar plus sources jar.

## Upstream And Sister Reference

- Upstream Node.js / TypeScript repository: <https://github.com/igapyon/miku-md2docx>
- Upstream compatibility source: release tag `v1.0.1`
- Upstream snapshot checked locally: package version `1.0.1`, commit `fc13a426ddc5e184d4f0b7b2c2f7c12efd5bc00a`
- Local upstream checkout used for comparison: `../miku-md2docx`
- Primary sister Java project used as repository-shape reference: `../miku-docx2md-java`
- Additional same-layer sister references available locally: `../miku-xlsx2md-java`, `../miku-indexgen-java`, `../mikuproject-java`

See `docs/` for upstream snapshot, class, CLI, test, migration status, and
miku-soft reference documents.

## Repository Operation

`workplace/` is a local scratch area for upstream clones, generated comparison
outputs, extracted archives, and temporary verification artifacts. Only
`workplace/.gitkeep` is tracked.

`.mvn/jvm.config` is tracked for repository-local Maven JVM settings.

`vendor/miku-ms-office-core-java/` contains the shared Office core release jar
used for product-neutral ZIP / OPC / XML package helpers.

## License

Apache License 2.0. See [LICENSE](./LICENSE).
