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
java -jar target/miku-md2docx-java-0.9.1.jar --version
scripts/compare-node-java-cli.sh
scripts/roundtrip-md-docx-md.sh
```

## Conversion Policy

The compatibility source is upstream `miku-md2docx` `devel` HEAD. The latest
local follow-up checked package version `0.9.1` at commit
`4d024794fa91d44174a76abb114aba3731768077`.

This repository starts with a Maven-plugin-free Java CLI runtime. Preserve
upstream vocabulary and observable CLI behavior while porting implementation
units. Keep Java-side original extensions separate from upstream-derived
behavior.
