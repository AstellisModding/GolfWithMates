#!/bin/bash
exec /usr/bin/gamescope \
  -w 1280 -h 720 \
  -W 1280 -H 720 \
  --force-grab-cursor \
  -- "$@"
