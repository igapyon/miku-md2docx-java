#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
UPSTREAM_DIR="${UPSTREAM_DIR:-${ROOT_DIR}/../miku-md2docx}"
WORK_DIR="${ROOT_DIR}/target/node-java-cli"
JAVA_JAR="${ROOT_DIR}/target/miku-md2docx-java-0.5.0.1.jar"
NODE_CLI="${UPSTREAM_DIR}/scripts/miku-md2docx-cli.mjs"

mkdir -p "${WORK_DIR}"

if [ "${SKIP_BUILD:-false}" != "true" ]; then
  (cd "${ROOT_DIR}" && mvn -q package)
elif [ ! -f "${JAVA_JAR}" ]; then
  (cd "${ROOT_DIR}" && mvn -q package)
fi

cat > "${WORK_DIR}/sample.md" <<'MARKDOWN'
# Target

Plain **bold** and *italic* with `code`.

See [target](#target) and [site](https://example.com).

- first
  1. nested

| A | B |
| --- | --- |
| **x** | y |

> quoted

```js
const value = 1;
```
MARKDOWN

IMAGE_CASE_DIR="${WORK_DIR}/image-embedded-source"
mkdir -p "${IMAGE_CASE_DIR}/images"
cat > "${IMAGE_CASE_DIR}/image.md" <<'MARKDOWN'
# Image Fixture

![Small](images/smoke.png)
MARKDOWN
node -e 'require("fs").writeFileSync(process.argv[1], Buffer.from([0x89,0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a,0x00,0x00,0x00,0x0d,0x49,0x48,0x44,0x52,0x00,0x00,0x00,0x10,0x00,0x00,0x00,0x08]))' "${IMAGE_CASE_DIR}/images/smoke.png"

IMAGE_FORMATS_DIR="${WORK_DIR}/image-formats-source"
mkdir -p "${IMAGE_FORMATS_DIR}/images"
cat > "${IMAGE_FORMATS_DIR}/images.md" <<'MARKDOWN'
# Image Formats

![Gif alt](images/sample.gif)

![Jpeg alt](images/photo.jpeg)

![WebP alt](images/asset.webp)

![Binary alt](images/diagram.bin)

![Large alt](images/large.png)
MARKDOWN
node -e '
const fs = require("fs");
const dir = process.argv[1];
fs.writeFileSync(`${dir}/sample.gif`, Buffer.from([0x47,0x49,0x46,0x38,0x39,0x61,0x20,0x00,0x10,0x00]));
fs.writeFileSync(`${dir}/photo.jpeg`, Buffer.from([0xff,0xd8,0xff,0xc0,0x00,0x11,0x08,0x00,0x20,0x00,0x40,0x00]));
fs.writeFileSync(`${dir}/asset.webp`, Buffer.from([0x52,0x49,0x46,0x46,0x00,0x00,0x00,0x00,0x57,0x45,0x42,0x50]));
fs.writeFileSync(`${dir}/diagram.bin`, Buffer.from([1,2,3,4]));
fs.writeFileSync(`${dir}/large.png`, Buffer.from([0x89,0x50,0x4e,0x47,0x0d,0x0a,0x1a,0x0a,0x00,0x00,0x00,0x0d,0x49,0x48,0x44,0x52,0x00,0x00,0x03,0xe8,0x00,0x00,0x01,0xf4]));
' "${IMAGE_FORMATS_DIR}/images"

LINKS_HTML_DIR="${WORK_DIR}/links-html-source"
mkdir -p "${LINKS_HTML_DIR}"
cat > "${LINKS_HTML_DIR}/links-html.md" <<'MARKDOWN'
# Alpha

## Alpha

[Alpha link](#alpha) [missing link](#missing) [external](https://example.com/path?a=1&b=2)

A <BR/> B <INS>u</INS> C <A class='x' href='https://example.com/u'>upper</A> D <IMG alt='Pic' src='missing-upper.png'>

Before <span class="x">inside</span> after

<div><b>block</b></div>
MARKDOWN

FRONTMATTER_CODE_DIR="${WORK_DIR}/frontmatter-code-source"
mkdir -p "${FRONTMATTER_CODE_DIR}"
cat > "${FRONTMATTER_CODE_DIR}/frontmatter-code.md" <<'MARKDOWN'
---
title: Front Matter Case
draft: false
---

# After Front Matter

Paragraph after metadata.

```txt
first line
second line
```

> quoted after code
MARKDOWN

AUTOLINK_DIR="${WORK_DIR}/autolink-source"
mkdir -p "${AUTOLINK_DIR}"
cat > "${AUTOLINK_DIR}/autolink.md" <<'MARKDOWN'
# Autolink

Visit https://example.com/a?b=1 and www.example.org/path.

Mail dev@example.com.
MARKDOWN

SETEXT_BREAKS_DIR="${WORK_DIR}/setext-breaks-source"
mkdir -p "${SETEXT_BREAKS_DIR}"
cat > "${SETEXT_BREAKS_DIR}/setext-breaks.md" <<'MARKDOWN'
Setext One
==========

Setext Two
----------

Soft line
continues here.

Hard break with spaces  
continues after break.

Hard break with slash\
continues after slash.
MARKDOWN

TILDE_CODE_DIR="${WORK_DIR}/tilde-code-source"
mkdir -p "${TILDE_CODE_DIR}"
cat > "${TILDE_CODE_DIR}/tilde-code.md" <<'MARKDOWN'
# Tilde Code

~~~txt
tilde one
tilde two
~~~

after
MARKDOWN

INDENTED_CODE_DIR="${WORK_DIR}/indented-code-source"
mkdir -p "${INDENTED_CODE_DIR}"
cat > "${INDENTED_CODE_DIR}/indented-code.md" <<'MARKDOWN'
# Indented Code

    alpha
    beta

after
MARKDOWN

REFERENCE_DIR="${WORK_DIR}/reference-source"
mkdir -p "${REFERENCE_DIR}"
cat > "${REFERENCE_DIR}/reference.md" <<'MARKDOWN'
# Reference Case

See [Example][site] and [shortcut].

![Logo][logo]

[site]: https://example.com/ref "Example title"
[shortcut]: https://example.com/shortcut
[logo]: images/logo.png "Logo title"
MARKDOWN

ESCAPES_ENTITIES_DIR="${WORK_DIR}/escapes-entities-source"
mkdir -p "${ESCAPES_ENTITIES_DIR}"
cat > "${ESCAPES_ENTITIES_DIR}/escapes-entities.md" <<'MARKDOWN'
# Escapes &amp; Entities

Literal \*not emphasis\* and \[not link\].

Entity &amp; &copy; &#x41; &#65; &lt;tag&gt;.
MARKDOWN

NESTED_BLOCKQUOTE_DIR="${WORK_DIR}/nested-blockquote-source"
mkdir -p "${NESTED_BLOCKQUOTE_DIR}"
cat > "${NESTED_BLOCKQUOTE_DIR}/nested-blockquote.md" <<'MARKDOWN'
# Nested Quote

> Outer line
> continues
>
> > Inner line
> > Inner **bold**
>
> Back outer
MARKDOWN

BLOCKQUOTE_CHILDREN_DIR="${WORK_DIR}/blockquote-children-source"
mkdir -p "${BLOCKQUOTE_CHILDREN_DIR}"
cat > "${BLOCKQUOTE_CHILDREN_DIR}/blockquote-children.md" <<'MARKDOWN'
# Quote Children

> Intro
>
> - ignored list
> - ignored second
>
> ```txt
> ignored code
> ```
>
> Outro
MARKDOWN

LIST_CHILDREN_DIR="${WORK_DIR}/list-children-source"
mkdir -p "${LIST_CHILDREN_DIR}"
cat > "${LIST_CHILDREN_DIR}/list-children.md" <<'MARKDOWN'
# List Children

- first paragraph

  second paragraph ignored

  - nested child

      code ignored

- second item
MARKDOWN

HTML_EDGE_DIR="${WORK_DIR}/html-edge-source"
mkdir -p "${HTML_EDGE_DIR}"
cat > "${HTML_EDGE_DIR}/html-edge.md" <<'MARKDOWN'
# HTML Edge

<ins>Block underline</ins>

<a href="https://example.com/html">Block **link**</a>

<br>

Inline <ins>under **bold**</ins> and <a href="https://example.com/split">split **link**</a>.
MARKDOWN

TABLE_EDGE_DIR="${WORK_DIR}/table-edge-source"
mkdir -p "${TABLE_EDGE_DIR}"
cat > "${TABLE_EDGE_DIR}/table-edge.md" <<'MARKDOWN'
# Table Edge

| Left | Center | Right | Pipe |
| :--- | :---: | ---: | --- |
| l | c | r | a \| b |
| **bold** | plain | `code` | x |
MARKDOWN

TITLE_ATTR_DIR="${WORK_DIR}/title-attr-source"
mkdir -p "${TITLE_ATTR_DIR}"
cat > "${TITLE_ATTR_DIR}/title-attr.md" <<'MARKDOWN'
# Title Attr

[Titled](https://example.com/title "Link title") and [Plain](https://example.com/plain).

![Missing titled](missing-title.png "Image title")
MARKDOWN

node "${NODE_CLI}" --version > "${WORK_DIR}/node-version.txt"
java -jar "${JAVA_JAR}" --version > "${WORK_DIR}/java-version.txt"

node "${NODE_CLI}" --help > "${WORK_DIR}/node-help.txt"
java -jar "${JAVA_JAR}" --help > "${WORK_DIR}/java-help.txt"

normalize_xml_entry() {
  archive_path="$1"
  entry_path="$2"
  output_path="$3"
  if unzip -p "${archive_path}" "${entry_path}" > "${output_path}.raw" 2>/dev/null; then
    sed 's/></>\
</g' "${output_path}.raw" > "${output_path}"
  else
    printf '%s\n' "__missing_entry__:${entry_path}" > "${output_path}"
  fi
}

compare_xml_entry() {
  case_dir="$1"
  entry_path="$2"
  xml_diff_dir="${case_dir}/xml-diff"
  safe_name=$(printf '%s' "${entry_path}" | tr '/[]' '___')
  node_xml="${xml_diff_dir}/${safe_name}.node.xml"
  java_xml="${xml_diff_dir}/${safe_name}.java.xml"
  diff_path="${xml_diff_dir}/${safe_name}.diff"
  normalize_xml_entry "${case_dir}/node.docx" "${entry_path}" "${node_xml}"
  normalize_xml_entry "${case_dir}/java.docx" "${entry_path}" "${java_xml}"
  diff -u "${node_xml}" "${java_xml}" > "${diff_path}"
}

compare_binary_entry() {
  case_dir="$1"
  entry_path="$2"
  binary_diff_dir="${case_dir}/binary-diff"
  safe_name=$(printf '%s' "${entry_path}" | tr '/[]' '___')
  mkdir -p "${binary_diff_dir}"
  unzip -p "${case_dir}/node.docx" "${entry_path}" > "${binary_diff_dir}/${safe_name}.node.bin"
  unzip -p "${case_dir}/java.docx" "${entry_path}" > "${binary_diff_dir}/${safe_name}.java.bin"
  cmp "${binary_diff_dir}/${safe_name}.node.bin" "${binary_diff_dir}/${safe_name}.java.bin" > "${binary_diff_dir}/${safe_name}.cmp"
}

compare_case() {
  case_name="$1"
  input_path="$2"
  shift 2
  case_dir="${WORK_DIR}/${case_name}"
  xml_diff_dir="${case_dir}/xml-diff"
  mkdir -p "${xml_diff_dir}"

  node "${NODE_CLI}" "${input_path}" --out "${case_dir}/node.docx" --summary > "${case_dir}/node-summary.txt"
  java -jar "${JAVA_JAR}" "${input_path}" --out "${case_dir}/java.docx" --summary > "${case_dir}/java-summary.txt"

  diff -u "${case_dir}/node-summary.txt" "${case_dir}/java-summary.txt" > "${case_dir}/summary.diff"
  compare_xml_entry "${case_dir}" "word/document.xml"
  compare_xml_entry "${case_dir}" "word/_rels/document.xml.rels"
  compare_xml_entry "${case_dir}" "word/styles.xml"
  compare_xml_entry "${case_dir}" "word/numbering.xml"
  if [ "$#" -gt 0 ]; then
    compare_xml_entry "${case_dir}" "[Content_Types].xml"
  fi
  for media_entry in "$@"; do
    compare_binary_entry "${case_dir}" "${media_entry}"
  done

  printf '%s\n' "${case_name} Node summary:"
  cat "${case_dir}/node-summary.txt"
  printf '%s\n' "${case_name} Java summary:"
  cat "${case_dir}/java-summary.txt"
  printf '%s\n' "Wrote ${case_name} comparison artifacts to ${case_dir}"
}

compare_case "representative" "${WORK_DIR}/sample.md"
compare_case "smoke-fixture" "${UPSTREAM_DIR}/tests/fixtures/smoke.md"
compare_case "image-embedded" "${IMAGE_CASE_DIR}/image.md" "word/media/image-1.png"
compare_case "image-formats" "${IMAGE_FORMATS_DIR}/images.md" \
  "word/media/image-1.gif" \
  "word/media/image-2.jpeg" \
  "word/media/image-3.webp" \
  "word/media/image-4.bin" \
  "word/media/image-5.png"
compare_case "links-html" "${LINKS_HTML_DIR}/links-html.md"
compare_case "frontmatter-code" "${FRONTMATTER_CODE_DIR}/frontmatter-code.md"
compare_case "autolink" "${AUTOLINK_DIR}/autolink.md"
compare_case "setext-breaks" "${SETEXT_BREAKS_DIR}/setext-breaks.md"
compare_case "tilde-code" "${TILDE_CODE_DIR}/tilde-code.md"
compare_case "indented-code" "${INDENTED_CODE_DIR}/indented-code.md"
compare_case "reference" "${REFERENCE_DIR}/reference.md"
compare_case "escapes-entities" "${ESCAPES_ENTITIES_DIR}/escapes-entities.md"
compare_case "nested-blockquote" "${NESTED_BLOCKQUOTE_DIR}/nested-blockquote.md"
compare_case "blockquote-children" "${BLOCKQUOTE_CHILDREN_DIR}/blockquote-children.md"
compare_case "list-children" "${LIST_CHILDREN_DIR}/list-children.md"
compare_case "html-edge" "${HTML_EDGE_DIR}/html-edge.md"
compare_case "table-edge" "${TABLE_EDGE_DIR}/table-edge.md"
compare_case "title-attr" "${TITLE_ATTR_DIR}/title-attr.md"

printf '%s\n' "Wrote comparison artifacts to ${WORK_DIR}"
