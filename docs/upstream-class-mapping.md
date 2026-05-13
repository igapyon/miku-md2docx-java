# Upstream Class Mapping

This document maps upstream Node.js / TypeScript files to Java classes or
future Java class groups.

| Upstream file | Java target | Status |
| --- | --- | --- |
| `scripts/miku-md2docx-cli.mjs` | `jp.igapyon.mikumd2docx.cli.MikuMd2docxCli` | Initial thin CLI implemented |
| `scripts/lib/cli-support.mjs` | `jp.igapyon.mikumd2docx.cli.CliOptions`, `MikuMd2docxCli` | Initial option shape implemented |
| `src/ts/core.ts` | `jp.igapyon.mikumd2docx.core.MikuMd2docxCore` | Callable API facade implemented |
| `src/ts/types.ts` | `Md2DocxResult`, `Md2DocxSummary` | Initial result and summary model implemented |
| `src/ts/markdown-parser.ts` | `MarkdownRenderer`, `MarkdownBlockRenderer`, `MarkdownText` line-oriented parser helpers | Split out from core; setext / break / reference / escape / entity / blockquote / list child / HTML edge / table edge / title attribute parity started; full remark parity pending |
| `src/ts/docx-package.ts` | `DocxPackageBuilder` | Split out from core |
| `src/ts/docx-templates.ts` | `DocxPackageBuilder` template methods | Split out from core |
| `src/ts/ooxml-primitives.ts` | `OoxmlPrimitives`, `RunStyle` | Split out from core |
| `src/ts/ooxml-*.ts` | `MarkdownBlockRenderer`, `InlineRenderer`, `OoxmlPrimitives` | First-cut block / inline / GFM autolink / link / image / escape / entity / blockquote / list child / HTML edge / table edge / title attribute behavior implemented |
| `src/ts/image-assets.ts` | `ImageAsset`, `Md2DocxOptions.ImageLoader`, `ImageAssets` | Split out from core |
| `src/ts/relationships.ts` | `Relationship`, `Relationships` | Split out from core |
| `src/ts/summary.ts` | `SummaryFormatter`, `Md2DocxSummary` | Split out from core |
| `src/ts/xml-utils.ts` | `XmlUtils` | Split out from core |
| `src/ts/zip-io.ts` | ZIP writing in `DocxPackageBuilder` | Split out from core |
