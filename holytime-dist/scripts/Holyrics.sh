#!/bin/sh
export J2D_UISCALE=96
cd "$(dirname "$0")/.."

AOT_FILE="holytime/holyrics.aot"

if [ -f "$AOT_FILE" ]; then
    holytime/bin/java -XX:AOTCache="$AOT_FILE" -jar Holyrics.jar
else
    holytime/bin/java -XX:AOTCacheOutput="$AOT_FILE" -jar Holyrics.jar
fi