---
purpose: ai-agent-goal
read_when:
  - before_starting_work
  - before_finishing_work
  - when_scope_is_unclear
update_when:
  - goal_changes
  - done_conditions_change
  - stop_conditions_change
---

# Goal

This file defines what the AI agent is trying to accomplish.
Read this before starting work, before deciding that work is complete, and whenever scope becomes unclear.

## Objective

Bring `miku-md2docx-java` up to the current upstream Node `miku-md2docx`
behavior and complete integration of `miku-ms-office-core-java` for
product-neutral ZIP / OPC / relationship / content type / XML package helpers.

Use the managed vendored release jar workflow already used by
`miku-md2xlsx-java`: track the `miku-ms-office-core-java` release jar under
`vendor/miku-ms-office-core-java/`, document the release and SHA-256 in a vendor
README, and unpack the jar during Maven `generate-sources`.

## Done

- The latest relevant upstream Node `miku-md2docx` state is checked and
  recorded with concrete version / commit evidence.
- Java code is updated for any required upstream Node follow-up.
- `miku-ms-office-core-java` is integrated using the managed vendored release
  jar workflow.
- Product-neutral ZIP / OPC / relationship / content type / XML helper usage is
  moved to `miku-ms-office-core-java` where appropriate.
- DOCX document assembly, Markdown conversion semantics, Word-specific
  templates, and summary policy remain owned by `miku-md2docx-java`.
- `README.md`, `TODO.md`, `docs/remaining-migration-items.md`,
  `docs/upstream-class-mapping.md`, `DECISIONS.md`, and `HANDOFF.md` reflect
  the implemented state.
- Relevant verification passes, including at minimum `mvn test`, `mvn package`,
  and the existing focused Node-vs-Java comparison / roundtrip scripts when
  their prerequisites are available.

## Stop

- Stop and ask if the target `miku-ms-office-core-java` release version changes
  from `0.5.1`.
- Stop and ask if ZIP byte/package parity would change before a focused
  regression command is agreed.
- Stop and ask before removing product-specific DOCX assembly or Markdown
  conversion semantics from this repository.
- `TODO.md` の `Retry Log` に同じ原因の失敗が3回記録された
