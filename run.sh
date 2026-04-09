#!/usr/bin/env bash
set -euo pipefail
if [ -f ./gradlew ]; then
  ./gradlew desktop:run
elif command -v gradle >/dev/null 2>&1; then
  gradle desktop:run
else
  echo "Gradle is required to run this project because the wrapper jar could not be vendored in the offline assembly environment." >&2
  exit 1
fi
