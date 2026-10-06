#!/usr/bin/env sh
set -eu
cd "$(dirname "$0")"
mkdir -p build/classes
find src -name '*.java' > build/sources.txt
if [ "${1:-}" = test ]; then find tests -name '*.java' >> build/sources.txt; fi
javac --release 17 -encoding UTF-8 -Xlint:all -d build/classes @build/sources.txt
case "${1:-}" in
  test) java -ea -cp build/classes bloodbank.service.SystemTest ;;
  run) java -cp build/classes bloodbank.Main "${2:-data/v2}" "${3:-reports/v2}" ;;
esac
