# Publicar um release

## Pré-requisitos

Antes do primeiro release, crie e guarde uma chave de assinatura fora do Git.
O diretório local `.release/` é ignorado justamente para impedir o envio da
chave. Faça backup seguro dela: perder a chave impede atualizar instalações já
existentes com o mesmo `applicationId`.

Configure estes GitHub Actions secrets no repositório:

| Secret | Conteúdo |
|---|---|
| `ANDROID_KEYSTORE_BASE64` | Arquivo `.jks` codificado em Base64, sem quebras de linha. |
| `ANDROID_KEYSTORE_PASSWORD` | Senha do keystore. |
| `ANDROID_KEY_ALIAS` | Alias da chave de release. |
| `ANDROID_KEY_PASSWORD` | Senha da chave. |

O workflow falha intencionalmente se qualquer secret estiver ausente. Nunca
publique um APK release não assinado e nunca versione a chave ou as senhas.

## Gerar e conferir localmente

Com variáveis de ambiente ou propriedades Gradle configuradas:

```bash
./gradlew test lint assembleRelease bundleRelease \
  -PRELEASE_STORE_FILE=/caminho/seguro/controlapeso-release.jks \
  -PRELEASE_STORE_PASSWORD='senha-do-keystore' \
  -PRELEASE_KEY_ALIAS='controlapeso-release' \
  -PRELEASE_KEY_PASSWORD='senha-da-chave'
```

Os arquivos ficam em `app/build/outputs/apk/release/` e
`app/build/outputs/bundle/release/`. Conferir assinatura:

```bash
apksigner verify --verbose --print-certs app/build/outputs/apk/release/app-universal-release.apk
sha256sum app/build/outputs/apk/release/*.apk app/build/outputs/bundle/release/*.aab
```

O workflow publica o fingerprint em `SIGNING_CERTIFICATE.txt`. Confira-o e
instale ao menos o APK universal em um telefone físico antes da publicação.

## Publicar

1. Conclua [RELEASE_CHECKLIST.md](RELEASE_CHECKLIST.md).
2. Atualize `versionCode`, `versionName` e [CHANGELOG](../CHANGELOG.md).
3. Execute testes, lint e builds localmente.
4. Crie e envie a tag correspondente, por exemplo:

   ```bash
   git tag -a v1.0.0 -m "Controla Peso v1.0.0"
   git push origin v1.0.0
   ```

5. Acompanhe o workflow **Publish Android release** no GitHub Actions.
6. Confira os APKs, AAB, checksums, assinatura e notas antes de anunciar.

O workflow é idempotente: se o release já existir, ele substitui os assets com
o mesmo nome. Uma tag deve sempre coincidir com `v<versionName>`.
