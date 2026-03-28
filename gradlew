#!/bin/sh
# Gradle wrapper — downloads and runs Gradle
# In production, use `gradle wrapper` to generate this properly

GRADLE_VERSION=8.7
GRADLE_URL="https://services.gradle.org/distributions/gradle-${GRADLE_VERSION}-bin.zip"
GRADLE_DIR="$HOME/.gradle/wrapper/dists/gradle-${GRADLE_VERSION}-bin"

if [ ! -d "$GRADLE_DIR" ]; then
    echo "Downloading Gradle ${GRADLE_VERSION}..."
    mkdir -p "$GRADLE_DIR"
    curl -sL "$GRADLE_URL" -o "/tmp/gradle-${GRADLE_VERSION}.zip"
    unzip -q "/tmp/gradle-${GRADLE_VERSION}.zip" -d "$GRADLE_DIR"
    rm "/tmp/gradle-${GRADLE_VERSION}.zip"
fi

exec "$GRADLE_DIR/gradle-${GRADLE_VERSION}/bin/gradle" "$@"
