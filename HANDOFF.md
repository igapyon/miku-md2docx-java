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

- Node `miku-md2docx` follow-up is updated to Release `v1.1.0`, package version
  `1.1.0`, commit `a25d302c742e6950183d60e0cda88f97bc65a265`.
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

- `mvn test`: passed, 27 tests.
- `mvn package`: passed for base `1.1.0` and Release version `1.1.0.2`.
- `scripts/compare-node-java-cli.sh`: passed against the published Node
  `miku-md2docx-1.1.0.mjs` Release Asset.
- `scripts/roundtrip-md-docx-md.sh`: passed with explicit front-matter
  exclusion for the current `miku-docx2md-java` contract.
- Packaged runtime `--version` and help reflected `1.1.0` and the simulated
  full Release version `1.1.0.2`.
- Packaged core metadata reports `miku-ms-office-core` `0.6.0`.
