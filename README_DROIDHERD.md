# Droidherd fork runner

This is Fork project with embedded API client to interact with [droidherd service](https://github.com/tinkoff-mobile-tech/droidherd) - k8s android farm.

Plugin also renamed to avoid any collision with original one.

## Usage

Functionality of plugin similar to original fork plugin by default. 

To run it with droidherd service you must set at least 2 variables:
- droidherd url
- emulators to request

Also, you do this using 2 ways:
1. Gradle plugin
2. Docker runner without gradle (e.g. standalone)

### Run with gradle plugin

To run fork with gradle plugin add plugin to your gradle.build:

```groovy
buildscript {
  dependencies {
    classpath 'ru.tinkoff.testops.droidherd:fork-gradle-plugin:1.0.0'
  }
}
```

Apply the Fork plugin

`apply plugin: 'ru.tinkoff.testops.droidherd.fork'`

To run tests using droidherd pass required parameters like service URL and required count of emulators:

```bash
./gradlew --no-daemon fork \
-Pfork.droidherd.url="${DROIDHERD_SERVICE_URL}" \
-Pfork.droidherd.emulators="android-29:1,android-30:1"
```
Note: pool strategy configuration will be ignored - tests will be run on random emulator. If you need to run each test on different android version - just setup several runs with required emulator version.

### Standalone run using docker

Use script [run-fork-in-docker.sh](run-fork-in-docker.sh) as starter and setup required parameters (like APKs).

## Example app

Provided as part of this repository. See [README](example-app/README.md) in example-app.

## Versioning

Droidherd fork plugin has own versioning which is mapped to 'fork' version.

We can be fetch changes from upstream at any moment and publish new version with fresh changes from fork main branch.
