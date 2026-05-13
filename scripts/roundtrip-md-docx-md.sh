#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
WORK_DIR="${ROOT_DIR}/target/roundtrip-md-docx-md"
MD2DOCX_JAR="${MD2DOCX_JAR:-${ROOT_DIR}/target/miku-md2docx-java-0.5.0.1.jar}"
DOCX2MD_JAR="${DOCX2MD_JAR:-${ROOT_DIR}/../miku-docx2md-java/miku-docx2md/target/miku-docx2md-0.9.0.jar}"

if [ "${SKIP_BUILD:-false}" != "true" ]; then
  (cd "${ROOT_DIR}" && mvn -q package)
elif [ ! -f "${MD2DOCX_JAR}" ]; then
  (cd "${ROOT_DIR}" && mvn -q package)
fi

if [ ! -f "${DOCX2MD_JAR}" ]; then
  printf '%s\n' "miku-docx2md Java jar not found: ${DOCX2MD_JAR}" >&2
  printf '%s\n' "Set DOCX2MD_JAR to an existing miku-docx2md runtime jar." >&2
  exit 1
fi

rm -rf "${WORK_DIR}"
mkdir -p "${WORK_DIR}"

SOURCE_MD="${WORK_DIR}/source.md"
DOCX_OUT="${WORK_DIR}/roundtrip.docx"
ACTUAL_MD="${WORK_DIR}/roundtrip.md"
EXPECTED_MD="${WORK_DIR}/expected.md"
SUMMARY_OUT="${WORK_DIR}/roundtrip.summary.txt"
EXPECTED_SUMMARY="${WORK_DIR}/expected.summary.txt"

cat > "${SOURCE_MD}" <<'MARKDOWN'
# Round Trip

Plain paragraph with **bold**, *italic*, and `code`.

- first item
- second item

| Name | Value |
| --- | --- |
| Alpha | 1 |
| Beta | 2 |

> quoted line
MARKDOWN

cat > "${EXPECTED_MD}" <<'MARKDOWN'
<a id="round-trip"></a>
# **Round Trip**

Plain paragraph with **bold**, *italic*, and code.

- first item
- second item

| **Name** | **Value** |
| --- | --- |
| Alpha | 1 |
| Beta | 2 |

*quoted line*
MARKDOWN

cat > "${EXPECTED_SUMMARY}" <<'SUMMARY'
paragraphs: 2
headings: 1
listItems: 2
tables: 1
images: 0
imageAssets: 0
drawingLikeUnsupported: 0
links: 0
internalLinks: 0
externalLinks: 0
unsupportedElements: 1
unsupportedCommentTraces: 1
SUMMARY

java -jar "${MD2DOCX_JAR}" "${SOURCE_MD}" --out "${DOCX_OUT}"
java -jar "${DOCX2MD_JAR}" "${DOCX_OUT}" --out "${ACTUAL_MD}" --summary-out "${SUMMARY_OUT}"

awk '{ print }' "${EXPECTED_MD}" > "${WORK_DIR}/expected.normalized.md"
awk '{ print }' "${ACTUAL_MD}" > "${WORK_DIR}/roundtrip.normalized.md"
awk '{ print }' "${EXPECTED_SUMMARY}" > "${WORK_DIR}/expected.normalized.summary.txt"
awk '{ print }' "${SUMMARY_OUT}" > "${WORK_DIR}/roundtrip.normalized.summary.txt"

diff -u "${WORK_DIR}/expected.normalized.md" "${WORK_DIR}/roundtrip.normalized.md" > "${WORK_DIR}/markdown.diff"
diff -u "${WORK_DIR}/expected.normalized.summary.txt" "${WORK_DIR}/roundtrip.normalized.summary.txt" > "${WORK_DIR}/summary.diff"

printf '%s\n' "Round-trip Markdown and summary matched."
printf '%s\n' "Wrote artifacts to ${WORK_DIR}"
