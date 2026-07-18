# Upstream Class Mapping

This document maps upstream Node.js / TypeScript files to Java classes or
future Java class groups.

| Upstream file | Java target | Status |
| --- | --- | --- |
| `scripts/miku-md2docx-cli.mjs` | `jp.igapyon.mikumd2docx.cli.MikuMd2docxCli` | Initial thin CLI implemented |
| `scripts/lib/cli-support.mjs` | `jp.igapyon.mikumd2docx.cli.CliOptions`, `MikuMd2docxCli` | Initial option shape implemented |
| `src/ts/core.ts` | `jp.igapyon.mikumd2docx.core.MikuMd2docxCore` | Callable API facade implemented |
| `src/ts/types.ts` | `Md2DocxOptions`, `Md2DocxResult`, `Md2DocxSummary`, `RenderState` | Result, template option, render state, and remote-image summary model implemented |
| `src/ts/markdown-parser.ts` | `MarkdownRenderer`, `MarkdownBlockRenderer`, `MarkdownText` line-oriented parser helpers | Split out from core; setext / break / reference / escape / entity / blockquote / list child / HTML edge / table edge / title attribute parity started; full remark parity pending |
| `src/ts/docx-package.ts` | `DocxPackageBuilder`, `jp.igapyon.mikumsofficecore` | ZIP/OPC handling, settings part, and structural template package reuse implemented |
| `src/ts/docx-template-loader.ts` | `LoadedDocxTemplatePackage` | Template ZIP loading and required `word/document.xml` validation implemented |
| `src/ts/docx-templates.ts` | `DocxPackageBuilder` template/settings methods | Styles, numbering, properties, settings compatibility mode 15, and section defaults implemented |
| `src/ts/ooxml-primitives.ts` | `OoxmlPrimitives`, `RunStyle` | Split out from core |
| `src/ts/ooxml-*.ts` | `MarkdownBlockRenderer`, `InlineRenderer`, `OoxmlPrimitives` | First-cut block / inline / image behavior implemented, including remote-image detail classification |
| `src/ts/image-assets.ts` | `ImageAsset`, `Md2DocxOptions.ImageLoader`, `ImageAssets` | Split out from core |
| `src/ts/relationships.ts` | `Relationship`, `Relationships`, `jp.igapyon.mikumsofficecore.OpcRelationships` | Split out from core; XML generation uses shared Office core |
| `src/ts/summary.ts` | `SummaryFormatter`, `Md2DocxSummary` | Summary fields implemented, including `remoteImages` and `remoteImageDetails` |
| `src/ts/xml-utils.ts` | `XmlUtils`, `jp.igapyon.mikumsofficecore.XmlHelper` | Split out from core; escaping uses shared Office core |
| `src/ts/zip-io.ts`, `src/vendor/miku-ms-office-core-0.6.0.mjs` | `jp.igapyon.mikumsofficecore.ZipPackage` | Deterministic ZIP entry order, timestamps, and XML 1.0 supplementary Unicode sanitization now come from shared Office core |

## miku-ms-office-core-java Integration

`miku-ms-office-core-java` is the expected Java owner for product-neutral Office
package plumbing. Use the `miku-md2xlsx-java` managed vendored release jar
pattern instead of depending on Maven repository publication: track the release
jar under
`vendor/miku-ms-office-core-java/`, document its release and SHA-256, and unpack
it during Maven `generate-sources`.

| Current local class / area | Shared Office core target | Status |
| --- | --- | --- |
| `XmlUtils` | `jp.igapyon.mikumsofficecore.XmlHelper` | XML escaping delegates to shared Office core; HTML stripping remains local. |
| `Relationship`, `Relationships` | `OpcRelationship`, `OpcRelationships` | Relationship XML generation delegates to shared Office core; DOCX relationship type constants remain local. |
| `[Content_Types].xml` handling in `DocxPackageBuilder` | `OpcContentTypes` | Content type XML generation delegates to shared Office core; DOCX content type policy remains local. |
| DOCX package part paths | Office core OPC normalization through `ZipPackage` / `OpcContentTypes` | Product-specific entry names remain local. |
| ZIP writing in `DocxPackageBuilder` | `ZipPackage`, `ZipEntryInput` | DOCX package ZIP writing delegates to shared Office core. |
