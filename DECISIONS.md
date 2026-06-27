---
purpose: ai-agent-decisions
read_when:
  - before_starting_work
  - when_making_decision
  - when_looping_or_repeating_work
update_when:
  - important_decision_is_made
  - option_is_rejected
  - work_is_deferred
---

# Decisions

This file records important decisions for the AI agent.
Read this before making or revisiting decisions, especially when the work seems to loop.

## 2026-06-28: Implement miku-ms-office-core-java Adoption After Upstream Follow-Up

Reason:
`miku-ms-office-core-java` already owns product-neutral ZIP / OPC / relationship
/ content type / XML helpers for Java miku-soft Office tooling. `miku-md2xlsx-java`
has an existing working intake pattern that vendors the release jar, records
release metadata and SHA-256, and unpacks the jar during Maven
`generate-sources`.

Decision:
The goal is not complete at planning time. The completed state requires both
following the latest relevant Node `miku-md2docx` state and integrating
`miku-ms-office-core-java` into `miku-md2docx-java`.

Use the `miku-md2xlsx-java` managed vendored release jar pattern rather than
waiting for Maven repository publication.

Impact:
Implementation should migrate helpers in small slices: XML helper review,
relationship model/XML builder, content types and part paths, then ZIP package
writing last. Product-specific DOCX assembly and Markdown conversion semantics
remain local to `miku-md2docx-java`.

Result:
The implementation follows this decision using `miku-ms-office-core-java`
`0.5.1`, vendored under `vendor/miku-ms-office-core-java/`, with Maven antrun
unpack during `generate-sources`.
