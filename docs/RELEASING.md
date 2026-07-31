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

O `./gradlew signingReport` deve mostrar uma configuração `release` real antes
da publicação. Se mostrar `Config: none`, o build local é apenas um artefato de
validação e não pode substituir uma instalação distribuída.

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

Cada tag e cada conjunto de assets publicado é imutável. Se a release já
existir, o workflow falha deliberadamente em vez de sobrescrever APK, AAB,
checksum ou notas. Uma tag deve sempre coincidir com `v<versionName>`; para
corrigir uma versão publicada, aumente `versionCode`/`versionName` e publique
uma nova versão.

## Rollback

O rollback é feito por uma nova release assinada, nunca por downgrade ou
substituição de assets:

1. interrompa a divulgação da versão problemática e preserve seus assets;
2. identifique a última versão estável no GitHub e no changelog;
3. corrija o problema ou reverta o código para essa base;
4. aumente `versionCode` e publique um novo `versionName` patch;
5. confira assinatura compatível, `SHA256SUMS.txt`, `applicationId` e testes;
6. publique a nova release com changelog explicando a correção.

O Android não aceita instalar um APK com `versionCode` menor sobre uma versão
mais nova. A chave release permanece a mesma; sem ela, não há rollback de
instalação preservando dados.

Teste o backup local, o Sharesheet e o APK universal futuro assinado em aparelho
físico, inclusive checksum, fontes desconhecidas e confirmação do
`PackageInstaller`.
