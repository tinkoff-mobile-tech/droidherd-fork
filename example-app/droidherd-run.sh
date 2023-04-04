#!/usr/bin/env bash

export DROIDHERD_SERVICE_URL="http://localhost:31252"

./gradlew \
  --no-daemon fork \
  -Pfork.droidherd.url="${DROIDHERD_SERVICE_URL}" \
  -Pfork.droidherd.emulators="android-29:1,android-30:1"
