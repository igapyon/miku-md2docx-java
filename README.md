# miku-md2docx-java

`miku-md2docx-java` is the Java straight-conversion runtime and CLI for
[`miku-md2docx`](https://github.com/igapyon/miku-md2docx).

The tool converts local Markdown files into editable Word `.docx` files. This
initial Java repository starts with the CLI runtime only. Maven plugin support
is intentionally out of the initial scope.
Generated DOCX package entries use ZIP DEFLATE compression.

## Usage

Run the downloaded GitHub Release asset:

```bash
java -jar miku-md2docx-java-1.1.0.jar ./sample.md --out ./sample.docx
```

The parent directory for `--out` is created automatically when it does not
exist. Existing output files are overwritten.

Summary output:

```bash
java -jar miku-md2docx-java-1.1.0.jar ./sample.md --out ./sample.docx --summary
```

`--summary` writes human-readable summary text to stdout. Use `--summary-out`
to write the same text to a file; its parent directory is also created
automatically. The summary text is not a stable machine-readable API.

Template reuse:

```bash
java -jar miku-md2docx-java-1.1.0.jar ./sample.md --out ./sample.docx --template ./template.docx
```

Show help or version:

```bash
java -jar miku-md2docx-java-1.1.0.jar --help
java -jar miku-md2docx-java-1.1.0.jar --version
```

Exit codes are `0` for success, help, or version; `1` for conversion or
file-system failure; and `2` for invalid CLI usage. stdout is reserved for
requested summary output, while usage errors, failures, and verbose progress
use stderr.

For development, build and test the source tree with:

```bash
mvn test
mvn package
```

## Current Scope

- Java source / target compatibility: `1.8`
- Build tool: Maven
- Test framework: JUnit Jupiter
- Primary verification: `mvn test`
- Runtime package: executable fat jar under `target/`
- Distribution package: `target/miku-md2docx-java-1.1.0-dist.zip`
- Maven plugin: out of initial scope

The current implementation follows upstream release `v1.1.0`, including
minimal DOCX settings with Word compatibility mode 15, remote-image summary
details, structural DOCX template reuse, and supplementary Unicode preservation
through `miku-ms-office-core-java` `v0.6.0`. Full `remark` edge-case parity
remains a tracked migration item.

GitHub Release asset workflow support is provided by
`.github/workflows/release-cli-runtime.yml`. It builds from `v*` tags or manual
`tag_name` dispatch and uploads the runtime jar plus sources jar. A Release tag
may add a dot suffix such as `v1.1.0.2`; the runtime Asset name, `--version`,
and help text then use the complete Release version.

## Upstream And Sister Reference

- Upstream Node.js / TypeScript repository: <https://github.com/igapyon/miku-md2docx>
- Upstream compatibility source: release tag `v1.1.0`
- Upstream snapshot checked publicly: package version `1.1.0`, commit `a25d302c742e6950183d60e0cda88f97bc65a265`
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
