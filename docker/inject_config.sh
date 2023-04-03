#!/bin/bash

OUTPUT_DIR=${OUTPUT_DIR:-result} \
TEST_PACKAGE=${TEST_PACKAGE} \
TEST_CLASS=${TEST_CLASS} \
COVERAGE_ENABLED=${COVERAGE_ENABLED:-false}  \
SCREENRECORD_ENABLED=${SCREENRECORD_ENABLED:-false} \
TEST_TIMEOUT=${TEST_TIMEOUT:-60000} \
ANNOTATION_FILTER=${ANNOTATION_FILTER} \
EXCLUDED_ANNOTATIONS=${EXCLUDED_ANNOTATIONS} \
ADB_USAGE_TYPE=${ADB_TYPE} \
INSTRUMENTATION_ARGS=${INSTRUMENTATION_ARGS:-null} \
DROIDHERD_EMULATOR_PARAMETERS=${DROIDHERD_EMULATOR_PARAMETERS:-""} \
DROIDHERD_URL=${DROIDHERD_URL:-"http://localhost:8080/"} \
DROIDHERD_MIN_EMULATORS=${DROIDHERD_MIN_EMULATORS:-1} \
DROIDHERD_EMULATORS=${DROIDHERD_EMULATORS} \
DROIDHERD_AUTH_PROVIDER=${DROIDHERD_AUTH_PROVIDER:-""} \
AUTO_GRANT_PERMISSIONS=${AUTO_GRANT_PERMISSIONS:-null} \
TOTAL_RETRIES=${TOTAL_RETRIES:-0} \
RETRY_PER_TEST=${RETRY_PER_TEST:-1} \
IGNORE_FAILURES=${IGNORE_FAILURES:-false} \
envsubst < "$1" > "$2"