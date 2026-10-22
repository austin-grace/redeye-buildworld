#!/usr/bin/env bash
# Builds redeye-buildworld-<version>.jar with plain javac (no Gradle).
# Usage: build.sh <Minecraft 1.21.1 Mojang-named jar> <worldswitcher-1.5.2.jar> <libraries dir>
#   e.g. from a NeoForge 21.1.256 install: libraries/net/minecraft/client/1.21.1-*/client-1.21.1-*-srg.jar
set -euo pipefail
here=$(cd "$(dirname "$0")" && pwd); mc=$1; ws=$2; libs=$3
version=$(sed -n -E 's/^version = "(.*)"/\1/p' "$here/src/main/resources/META-INF/neoforge.mods.toml")
cp="$mc:$ws:$(find "$libs" -name '*.jar' ! -name '*natives*' | tr '\n' ':')"
out=$(mktemp -d)
javac -nowarn --release 21 -proc:none -cp "$cp" -d "$out" $(find "$here/src/main/java" -name '*.java')
cp -r "$here/src/main/resources/." "$out/"
jar cf "$here/redeye-buildworld-$version.jar" -C "$out" .
rm -rf "$out"
echo "$here/redeye-buildworld-$version.jar"
