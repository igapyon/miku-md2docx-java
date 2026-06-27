---
purpose: ai-agent-handoff
read_when:
  - before_resuming_work
  - before_handing_off_work
  - when_context_is_missing
update_when:
  - work_is_paused
  - handoff_summary_changes
  - verification_status_changes
---

# Handoff

This file summarizes the current working state for the next human or AI agent.
Keep it concise. Do not use this as a full work log or a replacement for `TODO.md` and `DECISIONS.md`.

## Current State

- Node `miku-md2docx` follow-up is updated to package version `0.9.1`, commit
  `4d024794fa91d44174a76abb114aba3731768077`.
- `miku-ms-office-core-java` `0.5.1` is integrated using the
  `miku-md2xlsx-java` managed vendored release jar pattern.
- Product-neutral XML escaping, OPC relationship XML, OPC content type XML, and
  ZIP package writing now delegate to `miku-ms-office-core-java`.
- DOCX assembly, Word-specific templates, Markdown conversion semantics, and
  summary policy remain local.

## Next Action

- Review the final diff and commit when ready.

## Relevant Files

- `GOAL.md`: current objective, done conditions, and stop conditions.
- `TODO.md`: project TODO plus AI Agent current task section.
- `DECISIONS.md`: decision to use the `miku-md2xlsx-java` managed vendored jar
  pattern and keep product semantics local.
- `docs/remaining-migration-items.md`: migration follow-up notes.
- `docs/upstream-class-mapping.md`: local helper to `miku-ms-office-core-java`
  candidate mapping.

## Watch Outs

- ZIP package writing should migrate last because byte/package parity is
  sensitive.
- Keep DOCX document assembly and Markdown conversion semantics local to
  `miku-md2docx-java`.

## Last Verification

- `mvn test`: passed, 23 tests.
- `scripts/compare-node-java-cli.sh`: passed.
- `mvn package`: passed.
- `scripts/roundtrip-md-docx-md.sh`: passed.
- `java -jar target/miku-md2docx-java-0.9.1.jar --version`: printed `0.9.1`.
- Jar contents include shared Office core classes.
