# Development

## Repository Shape

- `pom.xml`: single-module Maven runtime jar.
- `src/main/java`: Java CLI and core runtime.
- `src/test/java`: focused JUnit Jupiter tests.
- `docs/`: upstream mapping, migration status, and project-local miku-soft
  reference notes.
- `workplace/`: local scratch area; only `workplace/.gitkeep` is tracked.

## Commands

```bash
mvn test
mvn package
java -jar target/miku-md2docx-java-1.0.0.jar --version
scripts/compare-node-java-cli.sh
scripts/roundtrip-md-docx-md.sh
```

## Conversion Policy

The compatibility source is upstream `miku-md2docx` release tag `v1.0.0`,
package version `1.0.0`, commit
`65d26eaf67f37c126261031fd91409a6e6afa5a8`.

This repository starts with a Maven-plugin-free Java CLI runtime. Preserve
upstream vocabulary and observable CLI behavior while porting implementation
units. Keep Java-side original extensions separate from upstream-derived
behavior.
