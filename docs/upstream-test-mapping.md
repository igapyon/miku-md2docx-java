# Upstream Test Mapping

| Upstream test intent | Java test | Status |
| --- | --- | --- |
| CLI prints version and help | `MikuMd2docxCliTest#printVersionAndHelp` | Implemented |
| CLI converts Markdown file and writes DOCX | `MikuMd2docxCliTest#convertsMarkdownFile` | Implemented |
| Core creates DOCX package entries | `MikuMd2docxCoreTest#createsDocxWithCoreEntriesAndSummary` | Implemented |
| Summary uses upstream vocabulary | `MikuMd2docxCoreTest#summaryFormatMatchesUpstreamVocabulary` | Implemented |
| Output is reproducible for same input | `MikuMd2docxCoreTest#outputIsDeterministicForSameInput` | Implemented |
| Representative OOXML for links, lists, tables, code blocks, quotes, rules | `MikuMd2docxCoreTest#rendersRepresentativeOoxmlForMarkdownStructures` | Implemented |
| Local image bytes become media entries and drawing XML | `MikuMd2docxCoreTest#embedsProvidedLocalImageBytes` | Implemented |
| Upstream fixture parity for `tests/fixtures/smoke.md` | Not yet implemented | Pending |
| Node-vs-Java CLI comparison | `scripts/compare-node-java-cli.sh` | Summary diff implemented; DOCX XML diff hardening pending |

Focused verification:

```bash
mvn test
```
