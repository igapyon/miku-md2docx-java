# Development

## Repository Shape

- `pom.xml`: single-module Maven runtime jar.
- `src/main/java`: Java CLI and core runtime.
- `src/test/java`: focused JUnit Jupiter tests.
- `docs/`: upstream mapping, migration status, and miku-soft basic documents.
- `workplace/`: local scratch area; only `workplace/.gitkeep` is tracked.

## Commands

```bash
mvn test
mvn package
java -jar target/miku-md2docx-java-0.8.0.1.jar --version
scripts/compare-node-java-cli.sh
scripts/roundtrip-md-docx-md.sh
```

## Conversion Policy

The compatibility source is upstream `miku-md2docx` `devel` HEAD. The latest
local follow-up checked package version `0.8.0` at commit
`77b798a7848df8da841527446245ebed4029f0bb`.

This repository starts with a Maven-plugin-free Java CLI runtime. Preserve
upstream vocabulary and observable CLI behavior while porting implementation
units. Keep Java-side original extensions separate from upstream-derived
behavior.
