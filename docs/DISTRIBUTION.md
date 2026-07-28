# Distribuição Android

## Compatibilidade

O app suporta Android 7.0/API 24 ou superior. O `targetSdk` é 36 e o artefato
universal inclui as bibliotecas nativas transitivas para ARM e x86.

| Arquivo do release | ABI | Quando escolher |
|---|---|---|
| `ControlaPeso-vX.Y.Z-universal.apk` | ARM 32/64 + x86 32/64 | Escolha padrão para instalação manual. |
| `ControlaPeso-vX.Y.Z-arm64-v8a.apk` | ARM 64 bits | Maioria dos smartphones Android atuais. |
| `ControlaPeso-vX.Y.Z-armeabi-v7a.apk` | ARM 32 bits | Aparelhos mais antigos ainda compatíveis. |
| `ControlaPeso-vX.Y.Z-x86_64.apk` | x86 64 bits | Emuladores e parte do ChromeOS. |
| `ControlaPeso-vX.Y.Z-x86.apk` | x86 32 bits | Emuladores legados. |
| `ControlaPeso-vX.Y.Z.aab` | Android App Bundle | Lojas que geram APKs otimizados por dispositivo. |

Os APKs por ABI não mudam a lógica do app; apenas evitam baixar bibliotecas
nativas destinadas a outras arquiteturas. O universal prioriza simplicidade.

## Instalação manual

1. Abra o [último release](https://github.com/guiloklex-hub/ControlaPeso/releases/latest).
2. Baixe o APK `universal` ou a variante da arquitetura do seu aparelho.
3. Baixe `SHA256SUMS.txt` e confira o arquivo antes de instalá-lo.
4. Autorize a instalação pelo navegador/gerenciador de arquivos quando o
   Android solicitar.
5. Abra o app e conclua o onboarding.

No Linux/macOS, a conferência pode ser feita assim:

```bash
sha256sum -c SHA256SUMS.txt --ignore-missing
```

No Windows PowerShell:

```powershell
Get-FileHash .\ControlaPeso-vX.Y.Z-universal.apk -Algorithm SHA256
```

Compare o hash mostrado com a linha correspondente de `SHA256SUMS.txt`.

## Assinatura

Artefatos de release são assinados por uma chave de assinatura do projeto.
Android só aceitará atualização quando o certificado for o mesmo de versões
anteriores. O fingerprint SHA-256 do certificado é publicado nas notas de cada
release como `SIGNING_CERTIFICATE.txt`, depois de conferido localmente.

Não instale APKs com assinatura, hash ou origem desconhecidos. Builds `debug`
são úteis para desenvolvimento, mas não são distribuídos como release estável.

## Reprodutibilidade

O workflow de release usa JDK 21, Android SDK 36.1 e o Gradle Wrapper
versionado. Ele executa testes e lint antes de criar APKs por ABI, um APK
universal, AAB e checksums.

Para gerar localmente um release assinado, leia [RELEASING.md](RELEASING.md).
