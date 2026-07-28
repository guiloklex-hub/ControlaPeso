# Entrega da implementação

Checkpoint local concluído em 27/07/2026, na branch
`feat/complete-weight-app`, sem commit, push ou publicação.

## Resultado executivo

O Controla Peso agora é um aplicativo local-first com onboarding, perfis,
entrada manual, captura BLE por advertising, confirmação conservadora de
unidade, estabilidade por janela, histórico Room, metas, dashboard, gráfico,
edição/exclusão, PDF, CSV, backup JSON, FileProvider/SAF, Health Connect
opt-in, lembretes, privacidade, diagnóstico e modo demo somente debug.

A captura BLE anterior e os parsers foram preservados. Não há conexão GATT,
permissão de Internet, backend, telemetria, cálculo inventado de composição
corporal ou scan em segundo plano.

## Arquitetura

```text
Compose + Material 3
  → ViewModels + StateFlow
    → casos de uso + interfaces de repositories
      → Room / DataStore / arquivos / Health Connect / WorkManager
  → BleMeasurementSource
    → BleScanner → parser OKOK → detector de estabilidade
```

`AppContainer` faz composição manual. O banco v1 possui `profiles`,
`weight_measurements`, `goals` e `scale_devices`, com FKs, índices,
transações para perfil/meta ativos e sem fallback destrutivo.

## Permissões

- `BLUETOOTH`, `BLUETOOTH_ADMIN` e `ACCESS_FINE_LOCATION`: até API 30;
- `BLUETOOTH_SCAN` e `BLUETOOTH_CONNECT`: API 31+;
- `POST_NOTIFICATIONS`: solicitada apenas ao ativar lembretes em API 33+;
- `android.permission.health.WRITE_WEIGHT`: somente após ação explícita.

`BLUETOOTH_SCAN` mantém `neverForLocation` pela evidência registrada no
Samsung/API 36. O compromisso de filtragem está em
[BLE_DIAGNOSTIC.md](BLE_DIAGNOSTIC.md). Não há `INTERNET`.

## Dependências adicionadas

As versões e justificativas completas estão em
[DEPENDENCIES.md](DEPENDENCIES.md): KSP, Room, DataStore, Navigation Compose,
Lifecycle Compose/ViewModel, Core SplashScreen, kotlinx.serialization JSON,
WorkManager, Health Connect, Material icons core e coroutines-test. O layout
adaptável usa APIs do Compose já disponíveis; Material Adaptive não foi
adicionado porque não foi necessário.

## Proteção contra duplicatas

O detector de estabilidade emite no máximo uma medição por sessão. Antes da
inserção, o caso de uso procura peso, perfil, horário e endereço compatíveis
em uma janela tolerante. Uma ocorrência provável exige revisão explícita do
usuário. O Health Connect usa o UUID como `clientRecordId`.

## Telas e comportamento adaptável

- onboarding;
- Início/dashboard;
- Histórico, gráfico, filtros e painel lista-detalhe em 840 dp;
- Medir por BLE e entrada manual;
- detalhe, edição, exclusão e desfazer;
- Perfis e Metas;
- Relatórios e restauração;
- Ajustes, Balanças, Diagnóstico, Privacidade e Sobre.

A navegação usa barra abaixo de 600 dp e rail a partir de 600 dp. Dashboard e
Histórico ganham painéis lado a lado em largura expandida. Tema claro, escuro
e sistema, cores dinâmicas, alto contraste, efeitos reduzidos, fonte ampliada
e semântica do gráfico estão implementados.

## Relatórios e integrações

- PDF paginado por `PdfDocument`;
- CSV UTF-8, `;`, decimal pt-BR e proteção contra fórmulas;
- JSON schema v1, prévia, validação, merge/replace e backup de segurança;
- importação limitada a 20 Mi caracteres;
- compartilhamento por `content://` e cópia via SAF;
- Health Connect somente escrita de peso, opcional e idempotente;
- lembretes aproximados pelo WorkManager, sem alarme exato e sem scan.

PDF/CSV/Health Connect não recebem endereço ou payload. JSON preserva esses
campos por ser backup completo.

## Testes e validação

Comando final:

```bash
./gradlew clean test lint assembleDebug assembleRelease assembleDebugAndroidTest --continue
git diff --check
adb devices -l
```

Resultados:

- Gradle: sucesso, 139 tarefas;
- JVM: 64 testes, zero falhas, erros ou ignorados;
- lint: sucesso, zero erros e 21 avisos não bloqueantes;
- debug, release não assinado e APK instrumentado: gerados;
- auditoria de cinco DEX release: fake BLE debug e `connectGatt` ausentes;
- `git diff --check`: sucesso;
- ADB: lista vazia; cinco testes instrumentados compilados, não executados.

Artefatos:

```text
app/build/outputs/apk/debug/app-debug.apk
app/build/outputs/apk/release/app-release-unsigned.apk
app/build/outputs/apk/androidTest/debug/app-debug-androidTest.apk
app/build/reports/tests/testDebugUnitTest/index.html
app/build/reports/lint-results-debug.html
```

### Validação posterior no telefone — 28/07/2026

No Samsung `SM-S908E`, Android 16/API 36:

- `./gradlew test lint assembleDebug assembleRelease
  assembleInstrumentedAndroidTest --continue` concluiu com sucesso;
- `./gradlew connectedAndroidTest` executou 16/16 testes, sem falhas;
- a atividade normal `br.com.paivalab.controlapeso/.MainActivity` continuou
  instalada e abriu após os testes;
- a busca BLE encontrou a `Yoda1`, trocou de `LOW_POWER` para `LOW_LATENCY`
  após anúncio OKOK e apresentou **Peso estabilizado** (`102,85 kg`) na UI.

A observação BLE não comparou o valor com o visor nem acionou o salvamento,
portanto não confirma unidade nem persistência. Durante uma execução anterior,
direta na variante `debug`, o cleanup do runner removeu o pacote-alvo e os
dados locais dessa instalação. Para impedir repetição, `connectedAndroidTest`
agora usa a variante isolada `instrumented`, com outro package. Nenhuma
tentativa de restaurar dados locais foi feita sem um backup JSON fornecido pelo
usuário. Uma repetição posterior dos smoke tests Compose precisa ser feita com
o telefone desbloqueado: com a tela bloqueada, quatro testes não receberam a
hierarquia de UI.

## Testes criados

Unitários:

```text
bluetooth/BleDiagnosticFormatterTest.kt
bluetooth/BleWeightReadingTest.kt
bluetooth/StableMeasurementDetectorTest.kt
core/time/MeasurementTimeFormatterTest.kt
data/backup/BackupValidatorTest.kt
data/backup/LimitedTextReaderTest.kt
data/export/CsvExportServiceTest.kt
data/export/ReportFileNamesTest.kt
data/export/ReportTextSummaryFormatterTest.kt
data/healthconnect/HealthConnectWeightMapperTest.kt
data/local/DatabaseConvertersTest.kt
data/local/EntityMappersTest.kt
domain/model/WeightConversionsTest.kt
domain/usecase/goals/CalculateGoalProgressTest.kt
domain/usecase/measurement/ManualMeasurementValidatorTest.kt
domain/usecase/measurement/ProbableDuplicatePolicyTest.kt
domain/usecase/profile/ProfileSelectionPolicyTest.kt
domain/usecase/statistics/CalculateBmiTest.kt
domain/usecase/statistics/MeasurementStatisticsTest.kt
ui/components/WeightChartDownsamplingTest.kt
ui/history/HistoryDateRangeCalculatorTest.kt
ui/profiles/ProfilesViewModelTest.kt
worker/ReminderPolicyTest.kt
worker/ReportCacheCleanerTest.kt
```

Instrumentados:

```text
bluetooth/BlePermissionManifestTest.kt
data/export/ReportServicesInstrumentedTest.kt
data/local/ControlaPesoDatabaseTest.kt
data/preferences/AppPreferencesRepositoryTest.kt
ui/UiSmokeInstrumentedTest.kt
```

Os testes preexistentes de hexadecimal e parsers Chipsea/OKOK continuam
presentes e verdes.

## Inventário de arquivos

### Criados

Raiz, schema e recursos:

```text
IMPLEMENTATION_PLAN.md
README.md
app/schemas/br.com.paivalab.controlapeso.data.local.ControlaPesoDatabase/1.json
app/src/main/res/drawable/ic_notification.xml
app/src/main/res/xml/provider_paths.xml
app/src/debug/java/br/com/paivalab/controlapeso/demo/DemoBleSourceFactory.kt
app/src/release/java/br/com/paivalab/controlapeso/demo/DemoBleSourceFactory.kt
```

Produção:

```text
app/AppContainer.kt
app/ControlaPesoApplication.kt
bluetooth/BleDiagnosticFormatter.kt
bluetooth/BleDiagnosticLogging.kt
bluetooth/BleMeasurementSource.kt
bluetooth/BleWeightReading.kt
bluetooth/OkOkBleMeasurementSource.kt
bluetooth/StableMeasurementDetector.kt
core/id/IdGenerator.kt
core/time/AppClock.kt
core/time/MeasurementTimeFormatter.kt
core/time/SystemAppClock.kt
data/backup/BackupCodec.kt
data/backup/BackupSchema.kt
data/backup/BackupValidator.kt
data/backup/JsonBackupManager.kt
data/backup/LimitedTextReader.kt
data/export/CsvExportService.kt
data/export/PdfReportService.kt
data/export/ReportFileNames.kt
data/export/ReportModels.kt
data/export/ReportTextSummaryFormatter.kt
data/export/ShareFileService.kt
data/healthconnect/HealthConnectWeightMapper.kt
data/healthconnect/HealthConnectWeightWriter.kt
data/local/ControlaPesoDatabase.kt
data/local/converter/DatabaseConverters.kt
data/local/dao/GoalDao.kt
data/local/dao/MeasurementDao.kt
data/local/dao/ProfileDao.kt
data/local/dao/ScaleDeviceDao.kt
data/local/entity/GoalEntity.kt
data/local/entity/ProfileEntity.kt
data/local/entity/ScaleDeviceEntity.kt
data/local/entity/WeightMeasurementEntity.kt
data/local/mapper/EntityMappers.kt
data/preferences/AppPreferences.kt
data/preferences/AppPreferencesRepository.kt
data/privacy/DataDeletionService.kt
data/repository/GoalRepositoryImpl.kt
data/repository/MeasurementRepositoryImpl.kt
data/repository/ProfileRepositoryImpl.kt
data/repository/ScaleDeviceRepositoryImpl.kt
domain/model/MeasurementSource.kt
domain/model/Profile.kt
domain/model/ScaleDevice.kt
domain/model/WeightGoal.kt
domain/model/WeightMeasurement.kt
domain/model/WeightUnit.kt
domain/repository/GoalRepository.kt
domain/repository/MeasurementRepository.kt
domain/repository/ProfileRepository.kt
domain/repository/ScaleDeviceRepository.kt
domain/usecase/goals/CalculateGoalProgress.kt
domain/usecase/measurement/CreateManualMeasurement.kt
domain/usecase/measurement/ManualMeasurementValidator.kt
domain/usecase/measurement/ProbableDuplicatePolicy.kt
domain/usecase/measurement/SaveBleMeasurement.kt
domain/usecase/profile/ProfileSelectionPolicy.kt
domain/usecase/statistics/CalculateBmi.kt
domain/usecase/statistics/MeasurementStatistics.kt
ui/about/AboutScreen.kt
ui/app/AppUiState.kt
ui/app/AppViewModel.kt
ui/app/ControlaPesoApp.kt
ui/components/WeightChart.kt
ui/dashboard/DashboardScreen.kt
ui/dashboard/DashboardViewModel.kt
ui/devices/DevicesScreen.kt
ui/devices/DevicesViewModel.kt
ui/goals/GoalsScreen.kt
ui/goals/GoalsViewModel.kt
ui/history/HistoryDateRangeCalculator.kt
ui/history/HistoryScreen.kt
ui/history/HistoryViewModel.kt
ui/measurement/detail/MeasurementDetailScreen.kt
ui/measurement/detail/MeasurementDetailViewModel.kt
ui/measurement/edit/ManualMeasurementScreen.kt
ui/measurement/edit/ManualMeasurementViewModel.kt
ui/measurement/live/BleMeasurementViewModel.kt
ui/measurement/live/LiveMeasurementScreen.kt
ui/navigation/AppDestination.kt
ui/navigation/ControlaPesoNavHost.kt
ui/onboarding/OnboardingScreen.kt
ui/onboarding/OnboardingViewModel.kt
ui/privacy/HealthPermissionsRationaleActivity.kt
ui/privacy/PrivacyScreen.kt
ui/privacy/PrivacyViewModel.kt
ui/profiles/ProfilesScreen.kt
ui/profiles/ProfilesViewModel.kt
ui/reports/ReportsScreen.kt
ui/reports/ReportsViewModel.kt
ui/settings/SettingsScreen.kt
ui/settings/SettingsViewModel.kt
worker/MeasurementReminderScheduler.kt
worker/MeasurementReminderWorker.kt
worker/ReminderPolicy.kt
worker/ReportCleanupWorker.kt
```

Todos os caminhos desse bloco são relativos a
`app/src/main/java/br/com/paivalab/controlapeso/`.

Também foram criados os 29 arquivos de teste listados acima e:

```text
docs/BASELINE.md
docs/BLE_PHYSICAL_VALIDATION.md
docs/BLE_PROTOCOL.md
docs/DATABASE_AND_BACKUP.md
docs/DEPENDENCIES.md
docs/IMPLEMENTATION_DELIVERY.md
docs/PHYSICAL_TEST_MATRIX.md
docs/PRIVACY.md
docs/RELEASE_CHECKLIST.md
docs/REPORT_FORMATS.md
docs/TESTING.md
```

`GOAL.md` já estava não rastreado no baseline e não é contado como criação
desta implementação.

### Alterados

```text
AGENTS.md
app/build.gradle.kts
app/src/main/AndroidManifest.xml
app/src/main/java/br/com/paivalab/controlapeso/MainActivity.kt
app/src/main/java/br/com/paivalab/controlapeso/bluetooth/BleScanner.kt
app/src/main/java/br/com/paivalab/controlapeso/ui/scanner/ScannerScreen.kt
app/src/main/java/br/com/paivalab/controlapeso/ui/theme/Color.kt
app/src/main/java/br/com/paivalab/controlapeso/ui/theme/Theme.kt
app/src/main/java/br/com/paivalab/controlapeso/ui/theme/Type.kt
app/src/main/res/values/colors.xml
app/src/main/res/values/strings.xml
app/src/main/res/values/themes.xml
app/src/main/res/xml/backup_rules.xml
app/src/main/res/xml/data_extraction_rules.xml
build.gradle.kts
docs/ARCHITECTURE.md
docs/BLE_DIAGNOSTIC.md
docs/BLE_PROTOCOL_AND_CONNECTION.md
docs/README.md
gradle/libs.versions.toml
```

Removido:

```text
app/src/androidTest/java/br/com/paivalab/controlapeso/ExampleInstrumentedTest.kt
```

O teste de template foi substituído pelos testes instrumentados reais.

## Instalar e testar

Quando `adb devices -l` listar o telefone como `device`:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am force-stop br.com.paivalab.controlapeso
adb shell monkey -p br.com.paivalab.controlapeso 1
adb logcat -s ControlaPesoBLE:D
```

Para a balança:

1. feche completamente o OKOK International;
2. abra **Medir > Medir com a balança**;
3. conceda a permissão e inicie a busca;
4. suba, permaneça parado e compare número/unidade com o visor;
5. confirme a unidade somente depois da comparação;
6. confirme que há um único salvamento estável;
7. repita três vezes e exporte o diagnóstico mascarado.

Para compartilhamento:

1. gere PDF, CSV e JSON em Relatórios;
2. revise o resumo;
3. abra o Sharesheet e teste um destino real;
4. salve uma cópia pelo SAF e reabra cada arquivo;
5. importe o JSON, valide prévia, merge e replace;
6. confirme que o backup de segurança pode ser salvo.

Para tablet/adaptabilidade:

1. teste 320, 360, 600 e 840 dp;
2. use retrato, paisagem e tela dividida;
3. valide NavigationBar/NavigationRail e Histórico lista-detalhe;
4. teste claro/escuro/sistema, fonte 200%, TalkBack e teclado.

## Pendências físicas e dados necessários

Ainda exigem aparelho: unidade do advertising, estabilidade no visor, três
balanças próximas, permissões negadas, Bluetooth desligado, persistência após
encerrar processo, Sharesheet/SAF reais, Health Connect, notificações,
TalkBack e API 24/25.

Para evoluir o protocolo, fornecer por pesagem:

- modelo/foto da etiqueta e unidade configurada;
- valor exibido no visor;
- sequência completa ocioso → variação → estável → zero;
- diagnóstico mascarado com payloads, timestamps, RSSI, `rawWeight`,
  propriedade e sequência;
- quantidade de registros criada;
- qualquer serviço/característica/notificação real, caso uma variante mostre
  evidência GATT.
