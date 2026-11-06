#!/bin/bash
pkill -f "process\." ; sleep 0.5                       # clear leftovers from the last run
javac -d build $(find src -name "*.java") || exit 1    # stop if it doesn't compile
trap 'pkill -f "process\."' EXIT                       # Ctrl+C kills everything, A included
java -cp build process.HeavyWeight A &
java -cp build process.HeavyWeight B