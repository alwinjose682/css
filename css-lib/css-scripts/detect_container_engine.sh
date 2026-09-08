#!/bin/bash
set -euo pipefail

detect() {
  if command -v podman >/dev/null 2>&1 && podman info >/dev/null 2>&1; then
    echo "podman"
    return 0
  fi

  # Check docker
  if command -v docker >/dev/null 2>&1 && docker info >/dev/null 2>&1; then
    echo "docker"
    return 0
  fi

  return 1
}

containerEngine=$(detect) || {
  echo "ERROR: No container engine found(podman/docker)" >&2
  exit 1
}
