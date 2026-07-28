# Plano de redesign UI/UX — ControlaPeso

## Objetivo e limites

Construir uma experiência Compose calma, confiável, privada e clara, com
progresso sem culpa e tecnologia discreta. O redesign é somente de UI/UX.

Não reescrever ViewModels, repositories, use cases, Room, BLE, parsers,
detector de estabilidade, exportação, Health Connect ou lembretes. Uma mudança
de estado só é aceitável para representar corretamente um estado funcional já
existente. Scanner e parsers permanecem fora de Composables; payload bruto não
vai para superfícies comuns.

## Base confirmada

- test, lint, assembleDebug e assembleRelease concluíram no baseline.
- Há tema claro/escuro/dinâmico/alto contraste e efeitos reduzidos.
- Não há previews, screenshot tests ou capturas visuais versionadas.
- A adaptação atual usa 600/840 dp de forma manual.
- Não há Material 3 Adaptive nas dependências atuais.

## Decisão visual antes de implementar

Criar UI_REFERENCE_NOTES.md e DESIGN_DIRECTIONS.md antes de alterar uma tela.
Pesquisar apenas documentação Android/Material/Compose/acessibilidade e
referências secundárias de saúde; registrar fonte, padrão, benefício e risco.

Comparar as direções Calm Health, Data Confidence e Warm Progress:

| Critério | Peso |
| --- | ---: |
| Usabilidade e clareza | 25% |
| Hierarquia da informação | 20% |
| Acessibilidade | 20% |
| Consistência e escalabilidade | 15% |
| Adaptabilidade | 10% |
| Desempenho e simplicidade técnica | 10% |

A maior pontuação vence. Empate é resolvido por acessibilidade, menor risco
funcional, menor complexidade e legibilidade de dados. Não usar glassmorphism
sistemático, blur em listas, gradientes onipresentes, gamificação agressiva,
cores morais para peso, fontes remotas ou bibliotecas pesadas.

## Fases verificáveis

### 0. Baseline e governança — concluída

- Arquivos: `docs/ui-ux/UI_UX_BASELINE.md`,
  `docs/ui-ux/UI_INVENTORY.md` e `docs/ui-ux/UI_UX_PLAN.md`.
- Validação:
  `JAVA_HOME=/tmp/controlapeso-jdk21.YVQjvw ./gradlew test lint assembleDebug`;
  `git diff --check`; `git status --short`.
- Conclusão: contratos críticos e baseline técnico documentados sem tocar em
  código de produção.

### 1. Pesquisa, direções e Figma

- Arquivos: UI_REFERENCE_NOTES.md, DESIGN_DIRECTIONS.md, UI_UX_PROGRESS.md e a
  primeira entrada de VISUAL_REVIEW.md.
- Figma conectado: criar/usar a página ControlaPeso UI Exploration, tokens
  conceituais e frames de Dashboard compact/expanded, Medição, Histórico e
  Ajustes. Figma valida a direção; Compose continua a fonte de verdade.
- Validação: matriz preenchida e revisão de claro/escuro, compact/expanded e
  texto ampliado; `git diff --check` para os documentos. Não há comando Gradle
  obrigatório porque esta fase não altera o APK.
- Conclusão: direção escolhida com justificativa e risco técnico.

### 2. Design system local e adaptação

- Arquivos previstos:
  - ui/designsystem/ControlaPesoDesignSystem.kt;
  - ui/designsystem/tokens/{Color,Typography,Shape,Spacing,Elevation,Motion,Size}Tokens.kt;
  - ui/designsystem/components/ para app bar, superfície, seção, métrica,
    status, estados vazio/erro/loading, item de ajuste e linha de medição;
  - ui/theme/{Color,Theme,Type}.kt e uma abstração local de largura.
- Escopo: escala tipográfica completa, números tabulares quando adequados,
  Shapes, espaçamento, elevação tonal, tokens semânticos e movimento curto que
  respeite VisualEffects.REDUCED.
- Dependências: nenhuma por padrão. Material 3 e Compose atuais já cobrem
  tokens, CompositionLocal, insets e animações curtas.
- Adaptive: verificar compatibilidade de Material 3 Adaptive antes de
  adicionar. Se pedir atualização em cascata, manter a abstração local.
- Validação: testes existentes, lint, builds debug/release e UI Check em
  360/600/840 dp:
  `./gradlew test lint assembleDebug assembleRelease`.
- Conclusão: tokens chegam ao MaterialTheme; sem wrappers vazios nem alteração
  de domínio.

### 3. Fixtures, previews e contrato visual

- Arquivos: ui/preview/PreviewProfiles.kt, PreviewMeasurements.kt,
  PreviewGoals.kt, PreviewDevices.kt, PreviewStates.kt e
  ControlaPesoPreviews.kt.
- Fixtures: objetos imutáveis e determinísticos: IDs, nomes, Instant, offset,
  pesos, textos longos, meta, erro e dispositivo BLE somente visual. Não
  acessam Room, Bluetooth, Health Connect, arquivos, relógio real ou rede.
- Tela pura: extrair somente quando necessário um Composable que recebe UiState
  e callbacks; NavHost/ViewModel seguem como adaptadores.
- Multi-preview: pt-BR, claro/escuro, 360/600/840 dp e fonte 1.0/1.5+ nas
  combinações mais arriscadas.
- Validação: Android Studio Preview, overflow, semântica e compilação.
  Comandos: `./gradlew testDebugUnitTest assembleDebug` e, quando a ferramenta
  de screenshot for aprovada, a tarefa específica descoberta por
  `./gradlew tasks --all`.
- Conclusão: Dashboard, Medição, Histórico, Ajustes e formulário cobrem
  loading, vazio, erro e conteúdo.

### 4. Navegação e app bars adaptáveis

- Arquivos: ui/navigation/ControlaPesoNavHost.kt, app bars e contêineres do
  design system; telas raiz apenas para insets/estrutura.
- Escopo: preservar cinco abas e retorno à raiz; padronizar app bars, insets e
  prioridade de Medir. Compact usa barra inferior, medium usa rail e expanded
  usa painéis somente quando diminuírem esforço de leitura.
- Validação: testes de navegação, 360/600/840 dp, teclado e fonte ampliada.
  Comandos: `./gradlew testDebugUnitTest assembleDebug` e
  `./gradlew connectedAndroidTest` com telefone/emulador desbloqueado.
- Conclusão: rotas, argumentos e back stack permanecem intactos.

### 5. Telas prioritárias

1. Dashboard: hero do último peso, um CTA Medir agora, tendência neutra,
   gráfico resumido, meta e estatísticas secundárias.
2. Medição ao vivo: valor em foco, status textual, perfil e cancelar;
   transições curtas apenas com efeitos completos. Não mudar estabilidade nem
   salvamento.
3. Histórico: período, gráfico, seleção, resumo e lista; em expanded,
   lista/detalhe seguro.

- Arquivos: ui/dashboard/*, ui/measurement/live/*, ui/history/*,
  ui/components/WeightChart.kt e componentes específicos de produto.
- Validação: previews de estados, testes JVM atuais, testes Compose e revisão
  clara/escura/fonte 200%. Comandos:
  `./gradlew test lint assembleDebug` e `./gradlew connectedAndroidTest`.
- Conclusão: hero, próxima ação e estados consistentes sem regressão de dados
  ou BLE.

### 6. Fluxos secundários

- Ordem: Ajustes, Perfis, Metas, Relatórios, formulários, Detalhe,
  Dispositivos, Privacidade, Sobre e Diagnóstico.
- Arquivos: telas em ui/settings, profiles, goals, reports, measurement/edit,
  measurement/detail, devices, privacy, about e scanner; componentes estritamente
  necessários.
- Escopo: Ajustes como lista organizada, formulários consistentes, perfil
  ativo/meta neutra, menos cartões e Diagnóstico técnico expansível.
- Validação: testes de dados, SAF, Health Connect, exclusão e erro.
  Comandos: `./gradlew test lint assembleDebug assembleRelease`; para fluxos
  integrados, `./gradlew connectedAndroidTest`.
- Conclusão: sem cards aninhados ou CTAs concorrentes injustificados.

### 7. Regressão visual e refinamento

- Arquivos: docs/ui-ux/screenshots/{before,after}/, manifestos,
  VISUAL_REVIEW.md, testes de screenshot se aprovados e ajustes pontuais.
- Rodada 1: hierarquia, espaçamento, contraste, overflow, ações e telas grandes.
- Rodada 2: tema escuro, fonte ampliada, vazios, efeitos reduzidos, navegação e
  consistência.
- Validação final: test, lint, assembleDebug, assembleRelease,
  connectedAndroidTest desbloqueado e git diff --check:

  ```bash
  ./gradlew test
  ./gradlew lint
  ./gradlew assembleDebug
  ./gradlew assembleRelease
  ./gradlew connectedAndroidTest
  git diff --check
  git status --short
  ```
- Conclusão: duas rodadas de evidência sem regressão BLE/privacidade.

## Previews, dados fake e evidências

### Previews locais

Usar fixtures estáticas, ControlaPesoTheme e callbacks vazios/gravadores de
evento; nunca AppContainer. Tempo, offsets e séries de gráfico serão fixos,
evitando imagens dependentes de data ou locale do computador.

### Screenshot tests

Fazer uma prova da ferramenta oficial Compose Preview Screenshot Testing com
AGP 9.3.1, Kotlin 2.2.10 e Compose BOM 2026.02.01 antes de adicionar plugin.
Só adotar se não pedir atualização em cascata e produzir referências estáveis.
Alternativa: variante isolada instrumented, AVD fixo, captura e asserções
semânticas/funcionais existentes.

Referências mínimas: Dashboard claro/escuro/expanded; Medição aguardando,
variando e estável; Histórico com dados, vazio e expanded; Ajustes claro/escuro
e formulário com fonte grande.

### Capturas documentais

Diretórios previstos:

    docs/ui-ux/screenshots/before/
    docs/ui-ux/screenshots/after/
    docs/ui-ux/screenshots/manifest.md

O manifesto terá rota, fixture/demo, dp, px, densidade, orientação, tema, cor
dinâmica, alto contraste, font scale, efeitos reduzidos, API/AVD ou telefone,
commit/build e data. Nunca salvar payload, endereço BLE ou dados pessoais.
Para integração, usar somente seeder demo debug ou variante instrumented;
dados precisam ser determinísticos, removíveis e marcados DEMO.

## Matriz de arquivos prevista por fase

Todos os caminhos de Kotlin abaixo partem de
`app/src/main/java/br/com/paivalab/controlapeso/`. A lista é deliberadamente
explícita para limitar o diff; um arquivo só entra se o refinamento daquela
fase realmente exigir.

| Fase | Arquivos novos | Arquivos existentes candidatos |
| --- | --- | --- |
| 1 — pesquisa/Figma | `docs/ui-ux/UI_REFERENCE_NOTES.md`, `DESIGN_DIRECTIONS.md`, `UI_UX_PROGRESS.md`, `VISUAL_REVIEW.md` | nenhum Kotlin/Gradle |
| 2 — design system | `ui/designsystem/ControlaPesoDesignSystem.kt`, `ui/designsystem/tokens/{ColorTokens,TypographyTokens,ShapeTokens,SpacingTokens,ElevationTokens,MotionTokens,SizeTokens}.kt`, componentes em `ui/designsystem/components/` | `ui/theme/Color.kt`, `Theme.kt`, `Type.kt` |
| 3 — fixtures/previews | `ui/preview/{PreviewProfiles,PreviewMeasurements,PreviewGoals,PreviewDevices,PreviewStates,ControlaPesoPreviews}.kt` | funções de conteúdo das telas prioritárias somente se a separação estado/callback for necessária |
| 4 — navegação | componentes adaptáveis em `ui/designsystem/components/` | `ui/navigation/ControlaPesoNavHost.kt`, `AppDestination.kt`, `ui/app/ControlaPesoApp.kt` |
| 5 — prioritárias | componentes específicos mínimos em `ui/designsystem/components/` | `ui/dashboard/DashboardScreen.kt`, `ui/history/HistoryScreen.kt`, `ui/measurement/live/LiveMeasurementScreen.kt`, `ui/components/WeightChart.kt` |
| 6 — secundárias | nenhum pacote novo fora do design system sem justificativa | `ui/settings/SettingsScreen.kt`, `ui/profiles/ProfilesScreen.kt`, `ui/goals/GoalsScreen.kt`, `ui/reports/ReportsScreen.kt`, `ui/measurement/edit/ManualMeasurementScreen.kt`, `ui/measurement/detail/MeasurementDetailScreen.kt`, `ui/devices/DevicesScreen.kt`, `ui/privacy/PrivacyScreen.kt`, `ui/about/AboutScreen.kt`, `ui/scanner/ScannerScreen.kt`, `ui/onboarding/OnboardingScreen.kt` |
| 7 — evidência | `docs/ui-ux/screenshots/{before,after}/`, `docs/ui-ux/screenshots/manifest.md`; testes visuais somente se a prova for aprovada | testes em `app/src/androidTest/`; ajustes pontuais nos arquivos tocados nas fases 2–6 |

ViewModels, repositories, DAOs, entidades, banco, parsers, scanner e detector
de estabilidade não são arquivos previstos. Se uma necessidade visual parecer
exigir alterá-los, a fase para e registra a justificativa antes de ampliar o
escopo.

## Dependências e justificativas

| Dependência | Necessária agora? | Decisão |
| --- | --- | --- |
| Compose/Material 3 já presentes | Sim | Reutilizar as versões atuais; nenhuma atualização. |
| Material 3 Adaptive | Não comprovada | Só adicionar após prova isolada de compatibilidade se reduzir código e risco em 600/840 dp. Caso contrário, usar abstração local. |
| Compose Preview Screenshot Testing | Não comprovada | Só adicionar plugin/biblioteca se a prova mantiver AGP 9.3.1, Kotlin 2.2.10 e Compose BOM 2026.02.01. |
| Biblioteca de gráficos | Não | Preservar `WeightChart` em Canvas. |
| DI, imagens remotas ou fontes externas | Não | Fora de escopo e sem benefício técnico necessário. |

Qualquer dependência nova exige registrar versão, tamanho/impacto, motivo,
alternativa sem dependência e resultado da prova. Falha na prova significa
adotar a alternativa local, não atualizar a stack.

## Gates objetivos por fase

| Fase | Gate de saída |
| --- | --- |
| 1 | Três direções comparadas pela matriz; uma escolhida; frames críticos inspecionados em claro/escuro e compact/expanded. |
| 2 | Tema compila; tokens cobrem cor/tipo/forma/espaço/elevação/tamanho/movimento; alto contraste e efeitos reduzidos permanecem funcionais. |
| 3 | Fixtures não acessam Android/I/O/relógio; previews críticos renderizam estados e larguras definidos. |
| 4 | As cinco abas e suas raízes são preservadas; compact usa bar e medium/expanded usam estrutura apropriada sem perder estado. |
| 5 | Dashboard, Medição e Histórico exibem valor/estado/ação prioritários e passam regressões BLE/dados. |
| 6 | Todas as rotas inventariadas usam padrões consistentes; fluxos destrutivos e integrações mantêm confirmação e retorno. |
| 7 | Duas revisões documentadas; matriz visual preenchida; comandos finais verdes ou bloqueio externo explicitamente reproduzido. |

## Riscos e controles

| Risco | Controle |
| --- | --- |
| UI altera BLE | Mesmos UiState; golden tests de parser/detector obrigatórios. |
| Preview lê recurso real | Fixtures sem AppContainer, relógio, banco ou Bluetooth. |
| Dependência atualiza stack | Prova isolada e justificativa antes de Gradle. |
| Expanded diverge | Fonte única de estado e testes 360/600/840 dp. |
| Peso vira julgamento | Tokens neutros e texto explícito. |
| Fonte grande corta | Previews 1.5/2.0, UI Check, scroll e largura máxima. |
| Evidência revela dados | Fixtures/demo e mascaramento. |
| Efeitos reduzidos ignorados | Tokens consultam VisualEffects.REDUCED. |

## Telefone físico necessário

- Bluetooth, permissões, descoberta BLE, timeout e pesagem contra o visor;
- TalkBack, haptics, teclado e fonte ampliada real;
- Health Connect, notificações, Sharesheet, SAF e PDF;
- divergências de renderização entre AVD e hardware.

Dashboard, Histórico, Metas, Perfis, Formulários, Relatórios e estados visuais
de Medição podem ser revisados primeiro em preview/AVD com fixtures.

## Critérios objetivos de conclusão

1. Tokens de cor, tipografia, forma, espaçamento, elevação, tamanho e movimento
   estão documentados e usados.
2. Dashboard, Medição, Histórico, Navegação e Ajustes têm hierarquia revisada.
3. Demais fluxos seguem o mesmo sistema e estados consistentes.
4. Claro, escuro, alto contraste, efeitos reduzidos, compact/medium/expanded e
   fonte ampliada possuem evidência.
5. Previews/fixtures e screenshots ou alternativa determinística estão presentes.
6. BLE, persistência, privacidade e exportações preservam comportamento/testes.
7. Builds e testes obrigatórios passam sem suprimir verificações.

Nenhum commit, push ou publicação faz parte deste plano.
