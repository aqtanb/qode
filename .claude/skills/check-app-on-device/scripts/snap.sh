#!/bin/bash
# usage: snap.sh <output-dir> <name>
set -e
dir="$1"
name="$2"
mkdir -p "$dir"
adb exec-out screencap -p > "$dir/$name.png"
adb shell uiautomator dump /sdcard/ui.xml >/dev/null 2>&1
adb exec-out cat /sdcard/ui.xml > "$dir/$name.xml"
python3 -I - "$dir/$name.xml" <<'PY'
import sys
import xml.etree.ElementTree as ET

for node in ET.parse(sys.argv[1]).getroot().iter('node'):
    label = node.get('text') or node.get('content-desc')
    tappable = node.get('clickable') == 'true'
    if label or tappable:
        prefix = '[tap] ' if tappable else '      '
        print(prefix + (label or '(no label)')[:70], node.get('bounds'))
PY
