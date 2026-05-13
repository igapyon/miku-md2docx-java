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
java -jar target/miku-md2docx-java-0.5.0.1.jar --version
```

## Conversion Policy

The compatibility source is upstream `miku-md2docx` `devel` HEAD at tag
`v0.5.0.1`.

This repository starts with a Maven-plugin-free Java CLI runtime. Preserve
upstream vocabulary and observable CLI behavior while porting implementation
units. Keep Java-side original extensions separate from upstream-derived
behavior.
