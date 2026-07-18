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

- Node `miku-md2docx` follow-up is updated to Release `v1.0.1`, package version
  `1.0.1`, commit `fc13a426ddc5e184d4f0b7b2c2f7c12efd5bc00a`.
- `miku-ms-office-core-java` `0.6.0` is integrated using the
  `miku-md2xlsx-java` managed vendored release jar pattern.
- Product-neutral XML escaping, OPC relationship XML, OPC content type XML, and
  ZIP package writing now delegate to `miku-ms-office-core-java`.
- DOCX assembly, Word-specific templates, Markdown conversion semantics, and
  summary policy remain local.
- Product-level regression coverage verifies that generated XML preserves
  supplementary Unicode and removes invalid XML characters.

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

- ZIP/package byte parity remains sensitive when the shared core changes.
- Keep DOCX document assembly and Markdown conversion semantics local to
  `miku-md2docx-java`.

## Last Verification

- `mvn test`: passed, 25 tests.
- `scripts/compare-node-java-cli.sh`: passed against Node `v1.0.1`.
- `mvn package`: passed as part of the comparison and round-trip scripts.
- `scripts/roundtrip-md-docx-md.sh`: passed with explicit front-matter
  exclusion for the current `miku-docx2md-java` contract.
- `java -jar target/miku-md2docx-java-1.0.1.jar --version`: printed `1.0.1`.
- Packaged core metadata reports `miku-ms-office-core` `0.6.0`.
