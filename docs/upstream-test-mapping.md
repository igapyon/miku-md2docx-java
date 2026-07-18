# Upstream Test Mapping

| Upstream test intent | Java test | Status |
| --- | --- | --- |
| CLI prints version and help | `MikuMd2docxCliTest#printVersionAndHelp` | Implemented |
| CLI converts Markdown file and writes DOCX | `MikuMd2docxCliTest#convertsMarkdownFile` | Implemented |
| Core creates DOCX package entries | `MikuMd2docxCoreTest#createsDocxWithCoreEntriesAndSummary` | Implemented |
| Default DOCX includes settings relationship/content type and compatibility mode 15 | `MikuMd2docxCoreTest#createsDocxWithCoreEntriesAndSummary` | Implemented |
| DOCX template body replacement and compatible part/style/section/settings preservation | `MikuMd2docxCoreTest#usesDocxTemplateWhileReplacingDocumentBody` | Implemented |
| Summary uses upstream vocabulary | `MikuMd2docxCoreTest#summaryFormatMatchesUpstreamVocabulary` | Implemented |
| Vendored XML sanitizer preserves supplementary Unicode and removes invalid XML characters | `MikuMd2docxCoreTest#preservesSupplementaryUnicodeAndRemovesInvalidXmlCharacters` | Implemented |
| Output is reproducible for same input | `MikuMd2docxCoreTest#outputIsDeterministicForSameInput` | Implemented |
| Representative OOXML for links, lists, tables, code blocks, quotes, rules | `MikuMd2docxCoreTest#rendersRepresentativeOoxmlForMarkdownStructures` | Implemented |
| Local image bytes become media entries and drawing XML | `MikuMd2docxCoreTest#embedsProvidedLocalImageBytes` | Implemented |
| Remote image URLs are separated from local missing-image details | `MikuMd2docxCoreTest#handlesImageFormatsRemoteImagesAndResizing` | Implemented |
| GFM autolink literals become external links | `MikuMd2docxCoreTest#rendersGfmAutolinks` | Implemented |
| Setext headings, soft line breaks, and hard breaks | `MikuMd2docxCoreTest#rendersSetextHeadingsAndMarkdownBreaks` | Implemented |
| Tilde fenced code blocks | `MikuMd2docxCoreTest#rendersTildeFencedCodeBlocks` | Implemented |
| Indented code blocks | `MikuMd2docxCoreTest#rendersIndentedCodeBlocks` | Implemented |
| Reference definitions, reference links, and reference images | `MikuMd2docxCoreTest#rendersReferenceDefinitionsLikeUpstream` | Implemented |
| Escaped Markdown punctuation and HTML entities | `MikuMd2docxCoreTest#rendersEscapesAndEntitiesLikeUpstream` | Implemented |
| Nested blockquote paragraph grouping | `MikuMd2docxCoreTest#rendersNestedBlockquotesLikeUpstream` | Implemented |
| Blockquote child list/code omission | `MikuMd2docxCoreTest#ignoresBlockquoteListAndCodeChildrenLikeUpstream` | Implemented |
| List child nested-list rendering and paragraph/code omission | `MikuMd2docxCoreTest#rendersListChildrenLikeUpstream` | Implemented |
| Supported HTML block and split inline HTML edge cases | `MikuMd2docxCoreTest#rendersHtmlBlockEdgesLikeUpstream` | Implemented |
| GFM table alignment and escaped pipe behavior | `MikuMd2docxCoreTest#rendersTableEscapedPipeLikeUpstream` | Implemented |
| Link and image title attributes ignored in generated DOCX/path targets | `MikuMd2docxCoreTest#ignoresLinkAndImageTitlesLikeUpstream` | Implemented |
| Upstream fixture parity for `tests/fixtures/smoke.md` | Not yet implemented | Pending |
| Node-vs-Java CLI comparison | `scripts/compare-node-java-cli.sh` | Summary and representative DOCX XML diffs enforced |
| Upstream smoke fixture comparison | `scripts/compare-node-java-cli.sh` smoke-fixture case | Summary and key DOCX XML diffs enforced |
| Embedded image package comparison | `scripts/compare-node-java-cli.sh` image-embedded case | Summary, key DOCX XML, content types, and media bytes enforced |
| Image format package comparison | `scripts/compare-node-java-cli.sh` image-formats case | Summary, key DOCX XML, content types, resized image, and media bytes enforced |
| Link and raw HTML comparison | `scripts/compare-node-java-cli.sh` links-html case | Summary and key DOCX XML diffs enforced |
| Front matter and code fence scanner comparison | `scripts/compare-node-java-cli.sh` frontmatter-code case | Summary and key DOCX XML diffs enforced |
| GFM autolink literal comparison | `scripts/compare-node-java-cli.sh` autolink case | Summary and key DOCX XML diffs enforced |
| Setext heading and Markdown break comparison | `scripts/compare-node-java-cli.sh` setext-breaks case | Summary and key DOCX XML diffs enforced |
| Tilde fenced code block comparison | `scripts/compare-node-java-cli.sh` tilde-code case | Summary and key DOCX XML diffs enforced |
| Indented code block comparison | `scripts/compare-node-java-cli.sh` indented-code case | Summary and key DOCX XML diffs enforced |
| Reference syntax comparison | `scripts/compare-node-java-cli.sh` reference case | Summary and key DOCX XML diffs enforced |
| Escaped punctuation and entity comparison | `scripts/compare-node-java-cli.sh` escapes-entities case | Summary and key DOCX XML diffs enforced |
| Nested blockquote comparison | `scripts/compare-node-java-cli.sh` nested-blockquote case | Summary and key DOCX XML diffs enforced |
| Blockquote child list/code comparison | `scripts/compare-node-java-cli.sh` blockquote-children case | Summary and key DOCX XML diffs enforced |
| List child comparison | `scripts/compare-node-java-cli.sh` list-children case | Summary and key DOCX XML diffs enforced |
| Supported HTML edge comparison | `scripts/compare-node-java-cli.sh` html-edge case | Summary and key DOCX XML diffs enforced |
| GFM table edge comparison | `scripts/compare-node-java-cli.sh` table-edge case | Summary and key DOCX XML diffs enforced |
| Link and image title attribute comparison | `scripts/compare-node-java-cli.sh` title-attr case | Summary and key DOCX XML diffs enforced |
| Remote-image summary comparison | `scripts/compare-node-java-cli.sh` remote-image case | Summary and key DOCX XML including settings enforced |
| Template-mode package comparison | `scripts/compare-node-java-cli.sh` template case | Summary, content types, and key DOCX XML including settings enforced |
| Markdown -> DOCX -> Markdown cross-tool smoke | `scripts/roundtrip-md-docx-md.sh` | Uses `miku-md2docx-java` and `miku-docx2md-java`; generated Markdown and summary are compared against focused expected outputs |

Focused verification:

```bash
mvn test
mvn package
scripts/compare-node-java-cli.sh
scripts/roundtrip-md-docx-md.sh
```
