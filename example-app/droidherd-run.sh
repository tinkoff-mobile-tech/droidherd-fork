#!/usr/bin/env bash

export DROIDHERD_SERVICE_URL=${DROIDHERD_SERVICE_URL:-"http://localhost:8080"}
export DROIDHERD_EMULATORS=${DROIDHERD_EMULATORS:-"android-29:1,android-30:1"}

./gradlew \
  --no-daemon fork \
  -Pfork.droidherd.url="${DROIDHERD_SERVICE_URL}" \
  -Pfork.droidherd.emulators=${DROIDHERD_EMULATORS}
