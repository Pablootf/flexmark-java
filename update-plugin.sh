#!/usr/bin/env bash
HOME_DIR="$(cd "$(dirname "${BASH_SOURCE[0]}")" >/dev/null 2>&1 && pwd)"

cd "${HOME_DIR}" || exit

function UpdProjects() {
  JAR="$1"

  echo updating "${JAR}" in
  for PROJECT in "${@:2}"; do
    echo "   " "${PROJECT}"
    cp "out/artifacts/${JAR}" "${PROJECT}"
  done
}

function UpdJars() {
  PROJECT="$1"

  echo updating "${PROJECT}" with
  for JAR in "${@:2}"; do
    echo "   " "${JAR}"
    cp "out/artifacts/${JAR}" "${PROJECT}"
  done
}

# Update flexmark-util.jar flexmark-tree-iteration.jar
UpdJars ../plugin-util/lib flexmark-util.jar flexmark-tree-iteration.jar
UpdJars ../MissingInActions/lib flexmark-util.jar flexmark-tree-iteration.jar
UpdJars ../touch-typists-completion-caddy/lib flexmark-util.jar flexmark-tree-iteration.jar

# Update flexmark-test-util.jar
UpdJars ../CLionArduinoPlugin/lib flexmark-test-util.jar flexmark-formatter.jar flexmark-util.jar

UpdProjects flexmark-parent.jar ../idea-multimarkdown1/lib ../idea-multimarkdown2/lib ../idea-multimarkdown3/lib ../markdown-profiling/lib ../MarkdownTest/lib
