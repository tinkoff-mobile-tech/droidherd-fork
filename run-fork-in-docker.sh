#!/usr/bin/env bash

# minimal script to run test using fork in docker

APK=${APK:-"/path-to/app-debug.apk"}
TEST_APK=${TEST_APK:-"/path-to/app-debug-androidTest.apk"}
DROIDHERD_EMULATORS=${DROIDHERD_EMULATORS:-"android-30:1"}
DROIDHERD_URL=${DROIDHERD_URL:-"http://localhost:8080"}
FORK_DOCKER_IMAGE=${FORK_DOCKER_IMAGE:-"droidherd-fork:local"}

mkdir -p apks
cp $APK apks/app.apk
cp $TEST_APK apks/test.apk

docker run --rm -it -v $(pwd)/apks:/apks \
  --env APK_FILE=/apks/app.apk --env TEST_APK_FILE=/apks/test.apk \
  --env DROIDHERD_URL="$DROIDHERD_URL" \
  --env DROIDHERD_EMULATORS="$DROIDHERD_EMULATORS" \
  $DOCKER_IMAGE_NAME /opt/fork/bin/run_fork.sh
