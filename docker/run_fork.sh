#!/bin/bash

apk=${APK_FILE:-app-debug.apk}
testApk=${TEST_APK_FILE:-app-debug-androidTest.apk}
$FORK_DIR/inject_config.sh "$FORK_DIR/config_template.json" "$FORK_DIR/config.json"

$FORK_DIR/fork-runner --config "$FORK_DIR/config.json" --test-apk ${testApk} --apk ${apk}
