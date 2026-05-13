# Upstream Class Mapping

This document maps upstream Node.js / TypeScript files to Java classes or
future Java class groups.

| Upstream file | Java target | Status |
| --- | --- | --- |
| `scripts/miku-md2docx-cli.mjs` | `jp.igapyon.mikumd2docx.cli.MikuMd2docxCli` | Initial thin CLI implemented |
| `scripts/lib/cli-support.mjs` | `jp.igapyon.mikumd2docx.cli.CliOptions`, `MikuMd2docxCli` | Initial option shape implemented |
| `src/ts/core.ts` | `jp.igapyon.mikumd2docx.core.MikuMd2docxCore` | Initial callable core implemented |
| `src/ts/types.ts` | `Md2DocxResult`, `Md2DocxSummary` | Initial result and summary model implemented |
| `src/ts/markdown-parser.ts` | `MikuMd2docxCore` line-oriented parser | First-cut behavior implemented; full remark parity pending |
| `src/ts/docx-package.ts` | `MikuMd2docxCore` package writer | First-cut package entries implemented |
| `src/ts/docx-templates.ts` | `MikuMd2docxCore` template methods | First-cut styles and numbering implemented |
| `src/ts/ooxml-*.ts` | `MikuMd2docxCore` renderer methods | First-cut block / inline / link / image behavior implemented |
| `src/ts/image-assets.ts` | `ImageAsset`, `Md2DocxOptions.ImageLoader`, `MikuMd2docxCore` image helpers | First-cut image embedding implemented |
| `src/ts/relationships.ts` | `MikuMd2docxCore` relationship model / writer | First-cut relationships implemented |
| `src/ts/summary.ts` | `MikuMd2docxCore#formatSummary`, `Md2DocxSummary` | Initial summary vocabulary implemented |
| `src/ts/xml-utils.ts` | Future XML escaping and helpers | Minimal XML escaping implemented |
| `src/ts/zip-io.ts` | ZIP writing in `MikuMd2docxCore` | Initial minimal implementation |
