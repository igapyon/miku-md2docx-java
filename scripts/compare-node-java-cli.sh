#!/usr/bin/env sh
set -eu

ROOT_DIR=$(CDPATH= cd -- "$(dirname -- "$0")/.." && pwd)
UPSTREAM_DIR="${UPSTREAM_DIR:-${ROOT_DIR}/../miku-md2docx}"
WORK_DIR="${ROOT_DIR}/target/node-java-cli"
JAVA_JAR="${ROOT_DIR}/target/miku-md2docx-java-0.5.0.1.jar"
NODE_CLI="${UPSTREAM_DIR}/scripts/miku-md2docx-cli.mjs"

mkdir -p "${WORK_DIR}"

if [ ! -f "${JAVA_JAR}" ]; then
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

node "${NODE_CLI}" --version > "${WORK_DIR}/node-version.txt"
java -jar "${JAVA_JAR}" --version > "${WORK_DIR}/java-version.txt"

node "${NODE_CLI}" --help > "${WORK_DIR}/node-help.txt"
java -jar "${JAVA_JAR}" --help > "${WORK_DIR}/java-help.txt"

node "${NODE_CLI}" "${WORK_DIR}/sample.md" --out "${WORK_DIR}/node.docx" --summary > "${WORK_DIR}/node-summary.txt"
java -jar "${JAVA_JAR}" "${WORK_DIR}/sample.md" --out "${WORK_DIR}/java.docx" --summary > "${WORK_DIR}/java-summary.txt"

diff -u "${WORK_DIR}/node-summary.txt" "${WORK_DIR}/java-summary.txt" > "${WORK_DIR}/summary.diff"

printf '%s\n' "Node summary:"
cat "${WORK_DIR}/node-summary.txt"
printf '%s\n' "Java summary:"
cat "${WORK_DIR}/java-summary.txt"

printf '%s\n' "Wrote comparison artifacts to ${WORK_DIR}"
