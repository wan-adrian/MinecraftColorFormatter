# Minecraft Color Formatter

An IntelliJ IDEA plugin that highlights Minecraft legacy color codes (Bedrock palette) in Java and Kotlin string literals.

## Features

- Real-time preview of `§` color codes (including Bedrock-only codes like `§g`, `§h`, ...)
- Works in Java and Kotlin string literals

## Installation

1. Download the plugin JAR from the releases page.
2. In IntelliJ IDEA, go to Settings > Plugins > Install Plugin from Disk.
3. Select the downloaded JAR file and restart IDEA.

## Usage

Use `§` color codes in your strings and the plugin will render the string content in the corresponding colors.

## Development

This plugin is built with Kotlin and Gradle.

To build:
```
./gradlew build
```

To run in development:
```
./gradlew runIde
```
