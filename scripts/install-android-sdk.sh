#!/usr/bin/env bash

set -euo pipefail

sdk_root="${ANDROID_HOME:?ANDROID_HOME precisa estar configurado}"
if command -v cygpath > /dev/null 2>&1; then
  sdk_root="$(cygpath -u "$sdk_root")"
fi

sdkmanager_path="$(find "$sdk_root/cmdline-tools" -type f \
  \( -name sdkmanager -o -name sdkmanager.bat \) -print -quit 2> /dev/null || true)"

if [[ -z "$sdkmanager_path" ]]; then
  echo "sdkmanager não encontrado em $sdk_root/cmdline-tools" >&2
  exit 1
fi

# sdkmanager fecha a entrada quando termina de aceitar licenças. Com pipefail,
# o processo `yes` então retorna SIGPIPE; isso é esperado e não mascara a
# instalação que vem logo em seguida.
yes | "$sdkmanager_path" --licenses > /dev/null || true
"$sdkmanager_path" --install \
  "platform-tools" \
  "platforms;android-36.1" \
  "build-tools;36.0.0"
