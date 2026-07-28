# Controla Peso

Aplicativo Android nativo, local-first e offline-first para registrar peso por
uma balança BLE compatível com o advertising observado no OKOK International
ou por entrada manual. Não exige conta, não possui anúncios, analytics,
telemetria ou servidor e não declara permissão de Internet.

> O Controla Peso não é um dispositivo médico. Ele registra peso e apresenta
> estatísticas neutras. Métricas não recebidas da balança permanecem ausentes.

## Estado do projeto

- Captura BLE ampla, sem conexão GATT, preservada do baseline funcional.
- Parser da variante `Yoda1`/OKOK C0 preservado por testes dourados.
- Unidade anunciada bloqueada até confirmação no visor físico.
- Estabilidade determinada por uma janela de leituras, nunca apenas por
  `property = 0x25`.
- Histórico local Room, perfis, metas, dashboard, gráfico e edição.
- PDF, CSV e backup/restauração JSON gerados localmente.
- Compartilhamento por Sharesheet com `content://` via `FileProvider`.
- Health Connect opcional, inicialmente somente escrita de peso.
- Lembretes aproximados via WorkManager, sem alarmes exatos e sem scan em
  segundo plano.
- Modo de demonstração disponível somente em build debug.

## Requisitos

- Android Studio compatível com AGP 9.3.1;
- JDK 21;
- Android SDK 36, minor API 1;
- telefone ou emulador Android 7.0/API 24 ou superior;
- telefone físico com BLE para testar a balança.

O projeto preserva Gradle 9.5.0, Kotlin 2.2.10, Compose BOM 2026.02.01,
`compileSdk 36.1`, `targetSdk 36` e `minSdk 24`.

## Abrir e compilar

Abra esta pasta no Android Studio ou execute, na raiz:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

O APK é criado em:

```text
app/build/outputs/apk/debug/app-debug.apk
```

## Instalar

Com a depuração USB autorizada:

```bash
adb devices -l
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am force-stop br.com.paivalab.controlapeso
adb shell monkey -p br.com.paivalab.controlapeso 1
```

No primeiro uso, conclua ou pule o onboarding. Para uma medição real, crie um
perfil, feche o OKOK International, abra **Medir > Medir com a balança**,
confirme a unidade comparando com o visor e inicie a busca.

## Testar a balança

1. Feche completamente o OKOK International.
2. Ative Bluetooth e conceda Dispositivos próximos; em Android 11 ou anterior,
   conceda Localização durante o uso.
3. Inicie a medição no Controla Peso.
4. Suba na balança e permaneça parado até a indicação textual de estabilidade.
5. Compare o valor e a unidade com o visor antes de confirmar e salvar.
6. Use **Ajustes > Diagnóstico Bluetooth** para copiar ou compartilhar a
   captura técnica; masque o endereço ao enviar o arquivo.

Logs detalhados precisam ser ativados em **Ajustes > Diagnóstico** e só estão
disponíveis em debug:

```bash
adb logcat -s ControlaPesoBLE:D
```

## Arquitetura resumida

```text
Compose UI
    ↓ ações / StateFlow
ViewModels
    ↓ casos de uso e repositories
Room + DataStore + exportadores + Health Connect
    ↑
BleMeasurementSource
    ↑
BleScanner → OkOkAdvertisementParser → StableMeasurementDetector
```

`AppContainer` faz composição manual das dependências; não há framework de
injeção. O scanner e os parsers não dependem de Composables. O banco armazena
peso canônico em kg, `Instant`, offset original, origem e payload bruto quando
ele realmente existe.

## Privacidade

- Dados privados ficam no banco e DataStore internos.
- Backup automático do Android está desativado.
- Relatórios só são criados após ação do usuário e ficam no cache por tempo
  limitado, salvo quando o usuário escolhe um destino pelo SAF.
- Health Connect recebe apenas peso, horário, offset e metadados mínimos após
  permissão explícita; payload, endereço e observação não são enviados.
- Excluir dados do aplicativo não apaga registros já gravados no Health
  Connect.

## Documentação

- [Arquitetura](docs/ARCHITECTURE.md)
- [Protocolo BLE](docs/BLE_PROTOCOL.md)
- [Diagnóstico BLE](docs/BLE_DIAGNOSTIC.md)
- [Banco e backup](docs/DATABASE_AND_BACKUP.md)
- [Dependências](docs/DEPENDENCIES.md)
- [Formatos de relatório](docs/REPORT_FORMATS.md)
- [Privacidade](docs/PRIVACY.md)
- [Testes](docs/TESTING.md)
- [Entrega e inventário](docs/IMPLEMENTATION_DELIVERY.md)
- [Matriz de teste físico](docs/PHYSICAL_TEST_MATRIX.md)
- [Checklist de release](docs/RELEASE_CHECKLIST.md)

## Limitações

- A unidade e os limites de estabilidade ainda exigem nova validação física.
- O formato OKOK C0 foi confirmado somente na balança `Yoda1` observada.
- O valor secundário e as flags desconhecidas não são tratados como
  composição corporal.
- Não existe conexão GATT: a balança observada transmite peso por advertising.
- Health Connect exige API 26; Android 7/API 24–25 continua funcional sem essa
  integração.
- Testes de BLE, compartilhamento, notificações, Health Connect e TalkBack
  precisam de telefone físico.

Consulte [BLE_PHYSICAL_VALIDATION.md](docs/BLE_PHYSICAL_VALIDATION.md) antes de
alterar unidade, parser ou critérios de estabilidade.
