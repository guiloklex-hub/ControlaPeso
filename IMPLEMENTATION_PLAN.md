# Plano de Implementação — ControlaPeso

## Progresso

| Fase | Estado | Evidência ou pendência |
| --- | --- | --- |
| 0 — Baseline e governança | Concluída em 27/07/2026 | Baseline registrado neste documento; build limpo, testes e lint passaram |
| 1 — Contrato BLE e balança física | Parcialmente validada em 28/07/2026 | `Yoda1` detectada, scan mudou para amostragem e UI mostrou peso estável; unidade/visor ainda pendentes |
| 2 — Fundação e dependências | Concluída em 27/07/2026 | Container, Clock, KSP, Room, DataStore, Navigation, WorkManager e Health Connect compatíveis |
| 3 — Persistência Room e repositórios | Concluída em 27/07/2026 | Schema v1 exportado, FKs, índices, transações e testes Room |
| 4 — Preferências, navegação, tema e onboarding | Concluída em 27/07/2026 | Onboarding, cinco destinos principais, temas e navegação adaptável |
| 5 — Perfis e medições manuais | Concluída em 27/07/2026 | CRUD, perfil ativo, validação, edição, exclusão e duplicidade |
| 6 — Medição BLE persistida | Implementação concluída; validação física parcial | Detector mostrou estado estável na `Yoda1`; persistência de um único registro ainda precisa ser acionada e comparada ao visor |
| 7 — Dashboard, histórico e detalhes | Concluída em 27/07/2026 | Gráfico acessível, estatísticas, filtros, agrupamento, edição e desfazer |
| 8 — Design, acessibilidade e adaptação | Implementação concluída; validação física pendente | Compact/medium/expanded, fonte ampliada, semântica e smoke tests compilados |
| 9 — Metas e IMC | Concluída em 27/07/2026 | Metas transacionais, progresso neutro e IMC derivado com aviso |
| 10 — CSV, JSON, backup e restauração | Concluída em 27/07/2026 | UTF-8 pt-BR, schema v1, prévia, mesclar/substituir e backup de segurança |
| 11 — PDF, compartilhamento e limpeza | Concluída em 27/07/2026 | PDF paginado, resumo, FileProvider, SAF e limpeza via WorkManager |
| 12 — Health Connect e lembretes | Implementação concluída; validação física pendente | Escrita idempotente opt-in e lembretes aproximados sem alarmes exatos |
| 13 — Dispositivos, diagnóstico, privacidade e demo | Concluída em 27/07/2026 | Balanças conhecidas, exportação técnica mascarada, exclusão total e demo somente debug |
| 14 — Qualidade e documentação final | Automação local concluída; repetição física pendente | `test`, lint, debug/release e APK instrumentado passaram; 16/16 instrumentados passaram no SM-S908E, mas a repetição exige telefone desbloqueado |

### Checkpoint final local — 27/07/2026

```bash
./gradlew clean test lint assembleDebug assembleRelease assembleDebugAndroidTest --continue
git diff --check
adb devices -l
```

- Gradle: sucesso, 139 tarefas.
- Testes JVM: 64, sem falhas, erros ou ignorados.
- Lint: sucesso, zero erros e 21 avisos não bloqueantes.
- APK debug: gerado.
- APK release não assinado: gerado.
- APK de testes instrumentados: gerado.
- Auditoria dos DEX release: fake BLE debug e `connectGatt` ausentes.
- `git diff --check`: sucesso.
- `connectedAndroidTest` e instalação não foram executados porque nenhum
  dispositivo ou AVD apareceu no ADB no checkpoint final.

### Checkpoint no telefone — 28/07/2026

```bash
./gradlew test lint assembleDebug assembleRelease assembleInstrumentedAndroidTest --continue
./gradlew connectedAndroidTest
adb shell am start -W -n br.com.paivalab.controlapeso/.MainActivity
```

- Build local: sucesso em 161 tarefas; testes JVM, lint, debug, release e APK
  instrumentado gerados sem falha.
- Testes instrumentados: 16/16 concluídos, sem falhas ou ignorados, no Samsung
  SM-S908E/Android 16/API 36.
- A variante de teste passou a usar package isolado
  `br.com.paivalab.controlapeso.instrumented`; o APK debug normal continuou
  instalado e abriu após a execução.
- BLE: o scan sem filtros iniciou em `LOW_POWER`, recebeu anúncio OKOK, mudou
  para `LOW_LATENCY` e a UI mostrou **Peso estabilizado** para a `Yoda1`.
- Não houve comparação com o visor nem toque em salvar. Unidade física e
  persistência de uma única medição continuam pendentes.
- Uma repetição posterior dos smoke tests Compose falhou porque
  `isKeyguardShowing=true`; repetir somente com o telefone desbloqueado e a
  tela acesa.

## 1. Baseline e invariantes

### Estado atual confirmado

- Branch: `feat/complete-weight-app`
- Commit: `1212cb4` — “BLE funcional antes da evolução visual”
- Package e namespace: `br.com.paivalab.controlapeso`
- Gradle Wrapper: 9.5.0
- Android Gradle Plugin: 9.3.1
- Kotlin: 2.2.10
- compileSdk: Android 36, minor API 1
- targetSdk: 36
- minSdk: 24
- Compose BOM: 2026.02.01
- UI atual: Jetpack Compose e Material 3
- Testes unitários atuais: 16
- Telefone detectado no baseline: Samsung SM-S908E, serial `RQCT3033YTT`
- Estado inicial do Git: nenhum arquivo rastreado alterado; apenas `GOAL.md` não rastreado.

O shell não possuía Java configurado. O baseline foi executado com JDK 21
temporário em `/tmp/controlapeso-jdk21.YVQjvw`.

| Comando | Resultado |
|---|---|
| `./gradlew test` | Sucesso |
| `./gradlew lint` | Sucesso |
| `./gradlew assembleDebug` | Sucesso |
| `./gradlew assembleRelease` | Sucesso |
| `git diff --check` | Sucesso |
| `git status --short` | Apenas `?? GOAL.md` |

Artefatos do baseline:

- Debug: 12 MB.
- Release não assinado: 7,8 MB.
- Aviso não bloqueante no release: `libandroidx.graphics.path.so` não pôde
  ser stripped e foi empacotada sem alteração.
- `connectedAndroidTest` não foi executado no baseline porque instalaria APKs
  no telefone.

### Contratos que não podem regredir

- Preservar `BleScanner`, seus callbacks, scan amplo sem filtros e limite total
  de 15 segundos.
- Preservar a troca de `LOW_POWER` para `LOW_LATENCY`, sem reiniciar o prazo
  total.
- Preservar deduplicação por endereço e atualização de RSSI e anúncio.
- Preservar captura integral de manufacturer data, service data, UUIDs e
  payload bruto.
- Preservar `OkOkAdvertisementParser` e a interpretação já validada do payload
  Yoda1:
  - marcador `0xC0`;
  - propriedades `0x24` e `0x25`;
  - peso bruto big-endian;
  - divisão por 100 reproduzindo o valor exibido pelo OKOK.
- Não declarar a unidade como confirmada sem teste físico.
- Não tratar `0x25` isoladamente como confirmação de estabilidade.
- Preservar `ChipseaParser` conservador, desacoplado e sem cálculo de métricas
  corporais.
- Preservar payload bruto em todo objeto persistido ou exportado proveniente
  do BLE.
- Não introduzir conexão GATT até existirem evidências de que seja necessária.
- Não introduzir Hilt, biblioteca de gráficos, biblioteca de PDF ou SDK
  proprietário.

## 2. Arquitetura e interfaces planejadas

### Camadas

```text
UI Compose
  ↓
ViewModels / UiState
  ↓
Casos de uso e interfaces de repositório
  ↓
Room, DataStore, BLE, Health Connect, arquivos e WorkManager
```

Um `ControlaPesoApplication` manterá um `AppContainer` simples, com criação
explícita de banco, repositórios, preferências, relógio e integrações. Não
haverá framework de injeção.

### Novos contratos principais

- `BleMeasurementSource`
  - expõe `StateFlow`;
  - inicia e encerra captura;
  - adapta o scanner atual sem movê-lo para Composables.
- `BleWeightReading`
  - endereço e identificação do dispositivo;
  - valor anunciado;
  - peso bruto;
  - unidade confirmada ou desconhecida;
  - sequência e propriedade;
  - instante da observação;
  - payload hexadecimal original.
- `StableMeasurementDetector`
  - recebe uma sequência de leituras;
  - nunca considera um pacote isolado estável;
  - emite no máximo um evento por sessão;
  - reinicia após estado ocioso, peso zero ou intervalo excessivo.
- `ProfileRepository`, `MeasurementRepository`, `GoalRepository` e
  `ScaleDeviceRepository`
  - expõem `Flow`;
  - não expõem DAOs diretamente à UI;
  - recebem `Clock` para testes determinísticos.
- `WeightUnit`
  - armazenamento interno em quilogramas;
  - apresentação em kg ou lb;
  - conversão centralizada e testada.
- `MeasurementSource`
  - `BLE`, `MANUAL`, `IMPORT`, `HEALTH_CONNECT` e `DEMO`.
- `ExportService` e `BackupService`
  - operam sobre modelos de domínio;
  - validam integralmente o conteúdo antes de gravar ou substituir dados.

### Dependências permitidas

Adicionar somente na fase em que forem usadas:

| Dependência | Versão planejada | Justificativa |
|---|---:|---|
| KSP | 2.3.6 | Processamento do Room e suporte ao Kotlin integrado do AGP 9 |
| Room runtime/ktx/compiler/testing | 2.8.4 | Persistência local tipada e testável |
| DataStore Preferences | 1.2.1 | Tema e preferências pequenas |
| Navigation Compose | 2.9.8 | Navegação declarativa entre telas |
| Material 3 Adaptive | 1.2.0 | Layouts para telefone, tablet e dobráveis |
| WorkManager | 2.11.2 | Lembretes aproximados e limpeza de temporários |
| Health Connect client | 1.1.0 | Exportação opcional de peso |
| Core Splashscreen | 1.2.0 | Splash compatível com as versões suportadas |
| kotlinx.serialization-json | 1.9.0 | Backup JSON versionado e validável |
| kotlinx-coroutines-test | compatível com o runtime resolvido | Testes determinísticos de Flow e ViewModel |

O gráfico será implementado com Compose Canvas. CSV será escrito diretamente
em UTF-8 e PDF usará `PdfDocument`. O seletor de arquivos usará Storage Access
Framework.

Se Navigation ou Room elevar transitivamente versões Lifecycle, executar
`dependencyInsight` e alinhar explicitamente somente os módulos Lifecycle
necessários. Não atualizar Compose, AGP, Kotlin, Activity ou Core apenas por
conveniência.

## 3. Fases de implementação

### Fase 0 — Confirmar baseline e governança

#### Alterações

- Usar este documento como checklist dos checkpoints.
- Confirmar que `AGENTS.md` e as invariantes BLE continuam válidos antes de
  iniciar alterações.
- Registrar no próprio checkpoint os comandos e resultados do baseline.
- Não alterar código de produção.

#### Arquivos

- `IMPLEMENTATION_PLAN.md`

#### Validação

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
git diff --check
git status --short
```

#### Conclusão objetiva

- Baseline documentado com commit, versões, resultados e limitações.
- Nenhum comportamento ou dependência alterado.

### Fase 1 — Congelar o contrato BLE e validar a balança física

#### Alterações

- Criar testes de regressão com payloads reais já observados.
- Criar `BleMeasurementSource` como adaptador do scanner existente.
- Criar detector de estabilidade independente do parser.
- Manter scanner e parsers atuais como fontes de verdade.
- Não persistir automaticamente enquanto unidade e estabilidade não forem
  confirmadas.
- Limitar logs e histórico em memória sem remover o payload técnico.

#### Arquivos

- `bluetooth/BleMeasurementSource.kt`
- `bluetooth/BleWeightReading.kt`
- `bluetooth/StableMeasurementDetector.kt`
- `bluetooth/OkOkBleMeasurementSource.kt`
- testes de `OkOkAdvertisementParser` e `StableMeasurementDetector`
- `docs/BLE_PROTOCOL_AND_CONNECTION.md`
- `docs/BLE_PHYSICAL_VALIDATION.md`

#### Critério conservador inicial de estabilidade

Após confirmação da unidade:

- somente leituras de medição `0x25`;
- pelo menos 8 leituras válidas;
- janela mínima de 2 segundos;
- variação total máxima de 0,10 kg;
- duas leituras finais com diferença máxima de 0,05 kg;
- apenas um evento até ocorrer `0x24`, peso zero ou lacuna superior a 2
  segundos.

Esses valores serão constantes nomeadas e testadas. Se o teste físico
contradizê-los, atualizar constantes e documentação com a evidência capturada,
sem alterar o parser bruto.

#### Teste físico obrigatório

1. Fechar completamente o OKOK.
2. Instalar a build debug.
3. Executar `adb logcat -s ControlaPesoBLE:D`.
4. Fazer no mínimo três pesagens com valores conhecidos.
5. Registrar display, valor anunciado, sequência, propriedade e duração.
6. Repetir uma pesagem oscilando propositalmente antes de estabilizar.
7. Confirmar se o valor anunciado está em kg ou exige conversão.

#### Validação

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb logcat -s ControlaPesoBLE:D
git diff --check
```

#### Conclusão objetiva

- Todos os payloads antigos continuam produzindo os mesmos resultados.
- Nenhum pacote isolado gera evento estável.
- Unidade confirmada com três amostras ou integração BLE marcada
  explicitamente como diagnóstico apenas.
- Scan de 15 segundos, permissões e captura ampla continuam funcionando no
  telefone.

### Fase 2 — Fundação de aplicação e dependências

#### Alterações

- Introduzir KSP, Room, DataStore e Navigation.
- Criar `ControlaPesoApplication`, `AppContainer` e abstração de relógio.
- Configurar schemas do Room no projeto.
- Criar source set debug para componentes de demonstração.
- Manter `MainActivity` pequena e sem lógica BLE.

#### Arquivos

- `gradle/libs.versions.toml`
- `build.gradle.kts`
- `app/build.gradle.kts`
- `AndroidManifest.xml`
- `ControlaPesoApplication.kt`
- `core/AppContainer.kt`
- `core/time/AppClock.kt`
- `core/time/SystemAppClock.kt`

#### Validação

```bash
./gradlew dependencyInsight --dependency room-runtime --configuration debugRuntimeClasspath
./gradlew dependencyInsight --dependency lifecycle-runtime --configuration debugRuntimeClasspath
./gradlew test
./gradlew lint
./gradlew assembleDebug
./gradlew assembleRelease
git diff --check
```

#### Riscos

- Compatibilidade entre AGP 9, Kotlin integrado e KSP.
- Atualização transitiva não intencional de Lifecycle.

#### Conclusão objetiva

- Build debug e release passam.
- KSP gera código sem warnings incompatíveis.
- Nenhuma dependência fora da lista aprovada aparece no runtime.

### Fase 3 — Persistência Room e repositórios

#### Alterações

- Criar banco versão 1, sem fallback destrutivo.
- Criar `ProfileEntity`, `WeightMeasurementEntity`, `GoalEntity` e
  `ScaleDeviceEntity`.
- Incluir `preferredWeightUnit` no perfil.
- Guardar peso internamente em kg.
- Guardar instante UTC e offset original.
- Preservar payload BLE, endereço, origem e timestamps.
- Adicionar índices para perfil e data, dispositivo e metas ativas.
- Implementar DAOs, mappers e repositórios.
- Exportar schemas do Room.

#### Arquivos

- `data/local/ControlaPesoDatabase.kt`
- `data/local/entity/*.kt`
- `data/local/dao/*.kt`
- `data/local/converter/*.kt`
- `data/repository/*.kt`
- `domain/model/*.kt`
- `domain/repository/*.kt`
- `app/schemas/`
- testes de banco, DAO, mapper e repositório

#### Testes

- CRUD de cada entidade.
- Exclusão em cascata do perfil.
- Consultas por intervalo local convertidas para UTC.
- Ordenação determinística.
- Conversão kg/lb sem alterar o valor armazenado.
- Payload bruto preservado.
- Repositórios reagindo via Flow.
- Banco criado a partir do schema exportado.

#### Validação

```bash
./gradlew test
./gradlew connectedAndroidTest
./gradlew lint
./gradlew assembleDebug
./gradlew assembleRelease
git diff --check
```

`connectedAndroidTest` pode usar telefone ou emulador.

#### Conclusão objetiva

- Banco v1 criado e reaberto sem perda.
- Todos os DAOs e repositórios possuem testes.
- Nenhuma UI acessa DAO diretamente.

### Fase 4 — Preferências, navegação, tema e onboarding

#### Alterações

- Criar preferências de tema e opções globais com DataStore.
- Criar grafo Navigation Compose.
- Criar shell com destinos Dashboard, Histórico, Medir e Configurações.
- Criar onboarding para primeiro perfil.
- Restaurar destino e estado após recriação.
- Adicionar splash compatível.

#### Arquivos

- `data/preferences/AppPreferences.kt`
- `ui/navigation/ControlaPesoNavHost.kt`
- `ui/navigation/Destinations.kt`
- `ui/app/ControlaPesoApp.kt`
- `ui/onboarding/*`
- `ui/theme/*`
- `MainActivity.kt`
- `AndroidManifest.xml`
- recursos em `res/values*`

#### Testes

- Preferências persistem após recriação.
- Tema sistema, claro e escuro.
- Primeiro acesso direciona ao onboarding.
- Perfil existente direciona ao Dashboard.
- Back stack previsível.

#### Validação

```bash
./gradlew test
./gradlew connectedAndroidTest
./gradlew lint
./gradlew assembleDebug
```

#### Conclusão objetiva

- Navegação funciona após rotação e processo recriado.
- Tela atual de diagnóstico permanece acessível.
- Modo claro e escuro mantêm contraste e legibilidade.

### Fase 5 — Perfis e medições manuais

#### Alterações

- CRUD de perfis.
- Apenas um perfil ativo por vez, garantido por transação.
- Cadastro manual, edição e exclusão de peso.
- Validação de datas, valores e campos opcionais.
- Confirmação explícita antes de excluir perfil com medições.
- Undo para exclusão de medição enquanto a tela permanecer ativa.

#### Arquivos

- `ui/profile/*`
- `ui/measurement/edit/*`
- `domain/usecase/profile/*`
- `domain/usecase/measurement/*`
- repositórios e recursos de texto relacionados

#### Testes

- Troca atômica de perfil ativo.
- Peso manual válido e inválido.
- Arredondamento somente na apresentação.
- Exclusão e undo.
- Perfil sem altura ou nascimento continua utilizável.

#### Validação

```bash
./gradlew test
./gradlew connectedAndroidTest
./gradlew lint
./gradlew assembleDebug
```

#### Conclusão objetiva

- Usuário consegue criar perfil e registrar, editar e excluir peso sem BLE.
- Nenhuma métrica corporal é inventada quando faltam dados.

### Fase 6 — Medição BLE persistida

#### Pré-condição

A Fase 1 deve ter confirmado unidade e critério de estabilidade. Caso
contrário, esta fase entrega somente visualização ao vivo e mantém o botão de
salvar desabilitado com explicação.

#### Alterações

- Integrar `BleMeasurementSource` à tela Medir.
- Mostrar leitura ao vivo, qualidade do sinal e progresso de estabilidade.
- Salvar apenas após evento estável.
- Permitir confirmação do usuário antes da gravação.
- Detectar provável duplicata sem impor índice único destrutivo.
- Associar endereço ao `ScaleDeviceEntity`.
- Manter diagnóstico técnico expansível.

#### Arquivos

- `ui/measurement/live/*`
- `domain/usecase/measurement/ObserveStableWeight.kt`
- `domain/usecase/measurement/SaveBleMeasurement.kt`
- `bluetooth/StableMeasurementDetector.kt`
- `data/repository/MeasurementRepositoryImpl.kt`
- `ui/scanner/*`

#### Testes

- Oscilação não salva.
- Estabilidade emite apenas uma vez.
- Estado ocioso libera nova sessão.
- Evento duplicado gera aviso.
- Bluetooth desligado, permissão revogada e timeout não salvam.
- Payload e endereço persistidos integralmente.

#### Telefone físico obrigatório

- Android 11 ou anterior, quando disponível.
- Android 12 ou superior.
- Bluetooth desligado durante scan.
- Permissão negada e negada permanentemente.
- Pesagem real e pesagem repetida.

#### Validação

```bash
./gradlew test
./gradlew connectedAndroidTest
./gradlew lint
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb logcat -s ControlaPesoBLE:D
```

#### Conclusão objetiva

- Uma pesagem estável gera uma única medição.
- Resultado persiste após encerrar e reabrir o aplicativo.
- Scanner e parsers preservam os testes de regressão.

### Fase 7 — Dashboard, histórico e detalhes

#### Alterações

- Dashboard com última medição, tendência e meta.
- Histórico paginado ou carregado por intervalos limitados.
- Filtros por período e origem.
- Detalhe da medição com edição e exclusão.
- Gráfico em Compose Canvas, sem biblioteca externa.
- Estados vazio, carregando e erro.

#### Arquivos

- `ui/dashboard/*`
- `ui/history/*`
- `ui/measurement/detail/*`
- `ui/components/WeightChart.kt`
- `domain/usecase/statistics/*`
- consultas Room agregadas necessárias

#### Testes

- Diferença absoluta e percentual.
- Intervalos diário, semanal, mensal e personalizado.
- Apenas uma medição.
- Valores iguais.
- Mudança de fuso e horário de verão.
- Filtros por origem.

#### Validação

```bash
./gradlew test
./gradlew connectedAndroidTest
./gradlew lint
./gradlew assembleDebug
```

#### Conclusão objetiva

- Dashboard e histórico refletem o banco em tempo real.
- Estatísticas produzem resultados determinísticos para datasets fixos.
- Gráfico não trava com histórico grande.

### Fase 8 — Design system, acessibilidade e layouts adaptativos

#### Alterações

- Adicionar Material 3 Adaptive somente após prova de compatibilidade.
- Criar componentes reutilizáveis de card, botões, campos e estados.
- Criar layouts compactos, médios e expandidos.
- Suportar orientação, tablet e dobráveis.
- Adicionar semântica, foco, tamanho mínimo de toque e escala de fonte.
- Usar animações discretas com redução quando apropriado.

#### Arquivos

- `ui/designsystem/*`
- `ui/components/*`
- telas Compose já criadas
- recursos `values`, `values-night` e dimensões

#### Testes

- Larguras de 320, 360, 600 e 840 dp.
- Fonte em 200%.
- Tema claro e escuro.
- TalkBack nos fluxos críticos.
- Navegação por teclado em layout expandido.

#### Validação

```bash
./gradlew test
./gradlew connectedAndroidTest
./gradlew lint
./gradlew assembleDebug
```

#### Requer dispositivo ou emulador

- Emulador de tablet.
- Emulador ou telefone dobrável, quando disponível.
- Telefone físico para TalkBack e escala de fonte.

#### Conclusão objetiva

- Nenhum conteúdo essencial corta ou fica inacessível.
- Todas as ações críticas têm rótulo semântico e área de toque adequada.

### Fase 9 — Metas e IMC

#### Alterações

- Meta de peso por perfil.
- Progresso e prazo opcional.
- IMC somente se altura válida estiver cadastrada.
- Classificação de IMC documentada como referência, sem diagnóstico.
- Nenhuma estimativa de gordura, água, músculo ou metabolismo.

#### Arquivos

- `ui/goals/*`
- `domain/usecase/goals/*`
- `domain/usecase/statistics/CalculateBmi.kt`
- recursos de texto e documentação

#### Testes

- Altura ausente, zero ou inválida.
- Limites das faixas de IMC.
- Meta acima, abaixo e igual ao peso atual.
- Prazo expirado.
- Troca de perfil.

#### Validação

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
```

#### Conclusão objetiva

- IMC nunca aparece sem altura válida.
- Textos não apresentam métricas como diagnóstico médico.

### Fase 10 — CSV, JSON, backup e restauração

#### Alterações

- Exportar CSV UTF-8 com separador documentado.
- Exportar backup JSON versionado.
- Importar usando Storage Access Framework.
- Validar arquivo inteiro antes de escrever.
- Oferecer modo mesclar e modo substituir.
- Criar backup de segurança antes de substituir.
- Executar importação em transação.
- Mostrar relatório de registros aceitos, ignorados e conflitantes.

#### Arquivos

- `data/export/CsvExportService.kt`
- `data/backup/JsonBackupService.kt`
- `data/backup/BackupSchema.kt`
- `data/import/ImportService.kt`
- `ui/settings/backup/*`
- `domain/usecase/backup/*`
- regras ProGuard ou R8 da serialização, se necessárias

#### Testes

- Caracteres acentuados.
- Campos vazios e delimitadores.
- Schema desconhecido.
- JSON truncado ou inválido.
- UUID duplicado.
- Mesclar sem sobrescrever dados mais novos.
- Substituição atômica.
- Round trip exportar e importar preserva dados.

#### Validação

```bash
./gradlew test
./gradlew connectedAndroidTest
./gradlew lint
./gradlew assembleDebug
./gradlew assembleRelease
```

#### Conclusão objetiva

- Backup válido restaura dataset equivalente.
- Arquivo inválido não modifica o banco.
- Usuário escolhe explicitamente o destino e o arquivo.

### Fase 11 — PDF, compartilhamento e limpeza

#### Alterações

- Gerar relatório PDF com `PdfDocument`.
- Compartilhar CSV, JSON e PDF via `FileProvider`.
- Usar somente diretório de cache para temporários.
- Limpar arquivos antigos de modo determinístico.
- Não expor caminhos internos ou permissões amplas.

#### Arquivos

- `data/export/PdfReportService.kt`
- `data/export/ShareFileService.kt`
- `provider_paths.xml`
- `AndroidManifest.xml`
- `worker/ReportCleanupWorker.kt`
- `ui/reports/*`

#### Testes

- PDF vazio, uma medição e múltiplas páginas.
- URI `content://`.
- MIME types corretos.
- Limpeza não remove arquivos recentes ou dados permanentes.
- Compartilhamento sem permissão de armazenamento.

#### Validação

```bash
./gradlew test
./gradlew connectedAndroidTest
./gradlew lint
./gradlew assembleDebug
```

#### Telefone físico obrigatório

- Abrir PDF em outro aplicativo.
- Compartilhar por pelo menos um destino instalado.
- Revogar acesso após encerramento do compartilhamento.

#### Conclusão objetiva

- Arquivos compartilhados abrem corretamente.
- Nenhum temporário permanece indefinidamente.

### Fase 12 — Health Connect e lembretes

#### Alterações

- Health Connect opcional e desativado por padrão.
- Solicitar somente permissão de escrita de peso.
- Usar UUID da medição como `clientRecordId`.
- Não duplicar registros já sincronizados.
- Criar lembretes aproximados via WorkManager.
- Solicitar notificações somente quando o usuário ativar lembretes.
- Não usar alarmes exatos.

#### Arquivos

- `health/HealthConnectWeightWriter.kt`
- `domain/usecase/health/*`
- `worker/MeasurementReminderWorker.kt`
- `ui/settings/health/*`
- `ui/settings/reminders/*`
- `AndroidManifest.xml`

#### Testes

- Health Connect indisponível.
- Permissão negada ou revogada.
- Escrita idempotente.
- WorkManager respeita preferência desativada.
- Mudança de horário e reinicialização do aparelho.

#### Validação

```bash
./gradlew test
./gradlew connectedAndroidTest
./gradlew lint
./gradlew assembleDebug
```

#### Telefone físico obrigatório

- Health Connect instalado e sem ele.
- Permissão concedida, negada e revogada.
- Notificações em Android 13 ou superior.
- Verificação real do registro de peso.

#### Conclusão objetiva

- Nenhuma informação sai do aparelho sem ação explícita.
- Sincronização repetida não cria duplicata.
- Lembretes podem ser desligados integralmente.

### Fase 13 — Dispositivos, diagnóstico, privacidade e modo demo

#### Alterações

- Criar tela de balanças conhecidas, último uso e remoção.
- Preservar tela técnica de diagnóstico BLE.
- Disponibilizar modo demo somente em build debug.
- Identificar dados demo como `DEMO` e permitir removê-los em uma ação.
- Documentar armazenamento, exportação e integrações.
- Desabilitar backup automático do Android para dados de saúde; o backup será
  manual.
- Remover ou mascarar logs sensíveis em release.
- Exibir versão do aplicativo e informações de suporte.

#### Arquivos

- `ui/settings/devices/*`
- `ui/settings/privacy/*`
- `src/debug/.../FakeBleMeasurementSource.kt`
- `src/debug/.../DemoDataSeeder.kt`
- `src/release/...`
- `AndroidManifest.xml`
- `backup_rules.xml`
- `data_extraction_rules.xml`
- `docs/PRIVACY_AND_DATA.md`
- documentação BLE existente

#### Testes

- Build release não contém entrada de modo demo.
- Logs release não exibem endereço ou payload.
- Dados demo não se misturam silenciosamente aos reais.
- Remoção de dispositivo não remove medições históricas.
- Regras de backup excluem banco e preferências sensíveis.

#### Validação

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
./gradlew assembleRelease
apkanalyzer manifest permissions app/build/outputs/apk/release/app-release-unsigned.apk
git diff --check
```

#### Conclusão objetiva

- Diagnóstico continua funcional.
- Build release não expõe dados técnicos desnecessários.
- Política de dados corresponde ao comportamento real.

### Fase 14 — Qualidade, release e documentação final

#### Alterações

- Completar testes instrumentados dos fluxos críticos.
- Revisar concorrência, cancelamento e lifecycle.
- Revisar índices e consultas Room.
- Revisar textos, acessibilidade e traduções.
- Atualizar documentação de arquitetura, BLE, banco, backup e instalação.
- Criar README raiz.
- Registrar matriz de compatibilidade e checklist de release.
- Não fazer commit automaticamente.

#### Arquivos

- `README.md`
- `AGENTS.md`
- `IMPLEMENTATION_PLAN.md`
- `docs/ARCHITECTURE.md`
- `docs/BLE_PROTOCOL_AND_CONNECTION.md`
- `docs/BLE_DIAGNOSTIC.md`
- `docs/DATABASE_AND_BACKUP.md`
- `docs/PHYSICAL_TEST_MATRIX.md`
- testes unitários e instrumentados restantes

#### Validação final

```bash
./gradlew clean
./gradlew test
./gradlew connectedAndroidTest
./gradlew lint
./gradlew assembleDebug
./gradlew assembleRelease
git diff --check
git status --short
```

No telefone:

```bash
adb devices -l
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am force-stop br.com.paivalab.controlapeso
adb shell monkey -p br.com.paivalab.controlapeso 1
adb logcat -s ControlaPesoBLE:D
```

#### Conclusão objetiva

- Todos os comandos passam sem desabilitar lint ou testes.
- Fluxos manual, BLE, histórico, backup e restauração passam em telefone
  físico.
- Scanner e parsers antigos mantêm os resultados dos testes dourados.
- Release não contém credenciais, SDK proprietário, Internet, serviço BLE em
  segundo plano ou banco remoto.
- Lista completa de arquivos modificados e diff resumido documentados.

## 4. Riscos e controles de regressão

| Risco | Controle |
|---|---|
| Quebrar BLE ao reorganizar ViewModels | Scanner e parsers ficam intactos; adaptação por interface e testes dourados |
| Salvar peso instável | Detector por janela, confirmação do usuário e apenas um evento por sessão |
| Unidade anunciada incorreta | Bloqueio da persistência BLE até validação física |
| Duplicar pesagens | Detecção de provável duplicata e idempotência por evento |
| Perder dados em importação | Validação completa, backup anterior e transação |
| Erros de fuso horário | `Instant` mais offset original e testes de fronteira |
| Vazamento de dados de saúde | Backup automático desativado, logs release reduzidos e integrações opt-in |
| Atualização transitiva de dependências | `dependencyInsight` e alinhamento explícito somente quando necessário |
| KSP incompatível com AGP 9 | Fase de fundação isolada e build debug e release antes de prosseguir |
| Degradação com histórico grande | Consultas limitadas ou agregadas, índices e testes de volume |
| Arquivos temporários esquecidos | Diretório de cache e WorkManager de limpeza |
| Health Connect duplicado | `clientRecordId` baseado no UUID da medição |
| Modo demo misturado com dados reais | Origem `DEMO`, aviso visual e build debug somente |
| UI inadequada em tablet ou dobrável | Material Adaptive, testes por largura e aparelho ou emulador |

## 5. Funcionalidades que exigem telefone físico

- Descoberta BLE real e permissões por versão do Android.
- Confirmação de unidade, sequência, estabilidade e repetição dos anúncios.
- Comportamento com Bluetooth desligado durante scan.
- Permissão negada permanentemente.
- Teste de múltiplas balanças próximas.
- TalkBack e escala de fonte real.
- Compartilhamento e abertura de PDF.
- Health Connect e notificações.
- Reabertura após encerramento do processo.
- Consumo de bateria durante scans repetidos.

Testes de Room, parsers, conversões, estatísticas, backup e ViewModels devem
permanecer executáveis sem telefone.

## 6. Critérios globais de conclusão

- Captura BLE e parsers atuais preservados por testes de regressão.
- Nenhuma pesagem BLE persistida sem estabilidade e unidade comprovadas.
- Dados armazenados localmente e utilizáveis offline.
- Funcionalidades de rede não adicionadas.
- Métricas corporais não inferidas sem protocolo validado.
- Aplicativo funcional em modo claro, escuro, compacto e expandido.
- Testes unitários cobrem parsers, estabilidade, persistência, estatísticas e
  importação.
- Testes instrumentados cobrem banco, navegação, SAF e fluxos críticos.
- `test`, `connectedAndroidTest`, `lint`, `assembleDebug` e `assembleRelease`
  passam.
- Documentação corresponde à implementação real.
- Alterações permanecem disponíveis para revisão, sem commit automático.

### Pressupostos definidos

- A interpretação `/100` continuará preservada, mas será chamada de “valor
  anunciado” até a unidade ser comprovada.
- `0x25` significa pacote de medição, não estabilidade.
- O banco começa na versão 1 e toda futura mudança exigirá migration testada.
- Peso é armazenado em kg somente após conversão validada.
- Tema é global; unidade é preferência do perfil.
- Health Connect é inicialmente somente escrita.
- Backup e exportação são iniciados pelo usuário.
- GATT permanece fora do escopo até surgir evidência concreta.
