# Setup notes

## Gradle wrapper jar

This repo includes `gradlew` (Unix) and `gradle/wrapper/gradle-wrapper.properties`
but **not** `gradle/wrapper/gradle-wrapper.jar` because binary files don't
belong in source control.

Generate it with one of these methods:

### Method 1 — use Android Studio (recommended)

Open the `android/` directory in Android Studio. It will detect the missing
wrapper jar and offer to download Gradle to generate it. Accept and run
"Sync Project with Gradle Files" — this will create `gradle-wrapper.jar`.

### Method 2 — use a local Gradle install

```bash
# If you have Gradle 8.10+ installed:
gradle wrapper --gradle-version 8.10.2 --distribution-type bin

# This regenerates gradlew, gradlew.bat, and gradle-wrapper.jar.
```

### Method 3 — download the jar directly

```bash
curl -L -o gradle/wrapper/gradle-wrapper.jar \
  https://raw.githubusercontent.com/gradle/gradle/v8.10.2/gradle/wrapper/gradle-wrapper.jar
```

Then verify:

```bash
./gradlew --version
```

You should see Gradle 8.10.2.

## First build

Once the wrapper jar is in place:

```bash
./gradlew assembleDebug
```

This will download all dependencies (Compose, Hilt, Retrofit, etc.) on first run.
Expected time: 3-6 minutes on a fresh setup.