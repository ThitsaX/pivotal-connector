#!/bin/sh
set -eu

if ! command -v pre-commit >/dev/null 2>&1; then
  if command -v brew >/dev/null 2>&1; then
    brew install pre-commit
  elif command -v pipx >/dev/null 2>&1; then
    pipx install pre-commit
  elif command -v apt-get >/dev/null 2>&1; then
    sudo apt-get update
    sudo apt-get install -y pre-commit
  elif command -v dnf >/dev/null 2>&1; then
    sudo dnf install -y pre-commit
  elif command -v pacman >/dev/null 2>&1; then
    sudo pacman -S --needed pre-commit
  else
    echo "Install pre-commit with your OS package manager or pipx, then rerun make setup-secrets." >&2
    exit 1
  fi
fi

pre-commit install --install-hooks
