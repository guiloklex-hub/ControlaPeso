# Inventário de UI — ControlaPeso

O inventário registra a implementação existente. Risco funcional classifica a
mudança visual: **alto** pede regressão funcional/instrumentada, não indica que
a tela atual esteja errada.

## Convenções atuais de janela

- **Compact:** abaixo de 600 dp, `NavigationBar` e uma coluna.
- **Medium:** de 600 a 839 dp, `NavigationRail`; a maioria das telas segue
  como coluna única.
- **Expanded:** 840 dp ou mais; Dashboard usa painéis e Histórico lista/detalhe.

## Mapa de implementação

| Superfície | Composable/rota atual | Estado e integração | Dependências visuais compartilhadas |
| --- | --- | --- | --- |
| Inicialização e casca | `ui/app/ControlaPesoApp.kt`, `ui/navigation/ControlaPesoNavHost.kt` | `AppUiState.kt`, `AppViewModel.kt`, `AppDestination.kt` | `ui/theme/*` |
| Onboarding | `ui/onboarding/OnboardingScreen.kt` | `OnboardingViewModel.kt` | `BrazilianDateTextField.kt` |
| Dashboard | `ui/dashboard/DashboardScreen.kt` | `DashboardViewModel.kt` | `WeightChart.kt` |
| Histórico | `ui/history/HistoryScreen.kt` | `HistoryViewModel.kt`, `HistoryDateRangeCalculator.kt` | `WeightChart.kt`, `BrazilianDateTextField.kt` |
| Menu Medir | função `MeasureMenu` em `ControlaPesoNavHost.kt` | rotas do `AppDestination.kt` | Material 3 direto |
| Medição ao vivo | `ui/measurement/live/LiveMeasurementScreen.kt` | `BleMeasurementViewModel.kt` | scanner, parser e detector somente por estado/callback |
| Manual/edição | `ui/measurement/edit/ManualMeasurementScreen.kt` | `ManualMeasurementViewModel.kt` | `BrazilianDateTextField.kt` |
| Detalhe | `ui/measurement/detail/MeasurementDetailScreen.kt` | `MeasurementDetailViewModel.kt` | Material 3 direto |
| Relatórios | `ui/reports/ReportsScreen.kt` | `ReportsViewModel.kt` | SAF/share por callbacks |
| Ajustes | `ui/settings/SettingsScreen.kt` | `SettingsViewModel.kt` | preferências por estado/callback |
| Perfis | `ui/profiles/ProfilesScreen.kt` | `ProfilesViewModel.kt` | Material 3 direto |
| Metas | `ui/goals/GoalsScreen.kt` | `GoalsViewModel.kt` | Material 3 direto |
| Dispositivos | `ui/devices/DevicesScreen.kt` | `DevicesViewModel.kt` | Material 3 direto |
| Diagnóstico BLE | `ui/scanner/ScannerScreen.kt` | `ScannerUiState.kt`, `ScannerViewModel.kt` | formatadores/resultado BLE por estado |
| Privacidade | `ui/privacy/PrivacyScreen.kt`, `HealthPermissionsRationaleActivity.kt` | `PrivacyViewModel.kt` | confirmação e intents por callbacks |
| Sobre | `ui/about/AboutScreen.kt` | conteúdo estático | Material 3 direto |

Os caminhos da tabela são relativos a
`app/src/main/java/br/com/paivalab/controlapeso/`. Eles identificam os pontos
de alteração visual; ViewModels e integrações aparecem para explicitar a
fronteira que o redesign deve preservar, não como alvos de reescrita.

## Telas e estados

| Tela | Objetivo e ações | Estados e componentes atuais | Hierarquia e acessibilidade | Compact / medium / expanded | Risco |
| --- | --- | --- | --- | --- | --- |
| Inicialização | Escolher loading, onboarding ou aplicativo; sem ação durante carga. | `ControlaPesoTheme`, `Box` e spinner central; loading/onboarding/conteúdo. | Spinner não explica a etapa de carga. | Ocupa a janela inteira. | Médio: manter `AppUiState`. |
| Onboarding | Explicar local-first, solicitar permissões por ação, criar perfil e concluir. Secundárias: voltar/pular/unidade/meta/tema. | Oito etapas, erro e salvamento; progresso linear, campos outlined, chips e botões. | Preservar scroll, IME, erros textuais e permissão explícita; melhorar contexto de cada etapa. | Coluna rolável em todos os tamanhos. | Alto: perfil e preferências. |
| Início (Dashboard) | Entender situação e iniciar medição. Secundárias: manual, histórico, perfis, metas. | Loading/erro/sem perfil/sem medições/conteúdo/demo; saudação, CTAs, `LatestWeightCard`, `WeightChart`, estatísticas e meta. | Peso compete com cartões e CTAs; tendência é neutra e gráfico mantém resumo textual. | Coluna até 839; em 840, peso/meta ao lado do gráfico. | Médio: não tocar cálculos. |
| Histórico | Explorar tendência e abrir detalhe. Secundárias: período, datas, origem e seleção. | Loading/erro/sem perfil/vazio/dados; chips, campos PT-BR, gráfico, estatísticas, agrupamentos e cartões. | Filtros e dados são densos; labels/seleção do gráfico precisam ser claros e textuais. | Coluna em compact/medium; lista/detalhe em 840. | Médio: preservar fuso, filtros e agrupamento. |
| Medir (menu) | Escolher balança ou registro manual. Secundária: diagnóstico. | Menu estático e botões de rota. | Dar clareza ao próximo passo e ao caráter técnico do diagnóstico. | Coluna em todas as larguras. | Alto para rotas; baixo visual. |
| Medição ao vivo | Conduzir BLE até leitura estável e confirmação/salvamento. Secundárias: buscar/parar, perfil, cancelar, unidade e duplicidade. | Sem suporte/permissão/Bluetooth, procurando, detectada, aguardando, variando, estabilizando, estável, salvo e erro; cartões de status/peso/ações. | Valor é o foco; estado e próxima ação devem ser textuais e não depender de cor/movimento. | Coluna central; sem painel de apoio estruturado. | Muito alto: scanner, estabilidade, timeout e salvamento. |
| Medição manual / edição | Criar ou editar peso com data PT-BR. Secundárias: voltar, cancelar e excluir. | Campos vazios, validação, erro, salvamento e confirmação; formulário outlined e máscara. | Padronizar ajuda/erro/teclado sem quebrar máscara ou cursor. | Coluna rolável fluida. | Alto: data, fuso e persistência. |
| Detalhe de medição | Revisar valores e editar/excluir. | Loading, não encontrada, conteúdo e métricas adicionais só quando existentes; app bar e cartões. | Distinguir medido, derivado e ausente; não sugerir dados corporais não recebidos. | Rota de detalhe; pode ser aberta pelo painel expanded. | Alto: fidelidade e privacidade. |
| Relatórios | Escolher período/formato e gerar PDF/CSV/JSON. Secundárias: SAF, share, importação/restauração. | Loading, sem perfil/dados, gerando, sucesso e erro; seções, escolhas e lista de arquivo. | Muitas opções têm ênfase semelhante; CTA e resumo devem liderar. | Predominantemente coluna única. | Alto: SAF, MIME, FileProvider e backup. |
| Ajustes | Configurar aparência, medição, histórico, lembretes, integrações e abrir subtelas. | Health Connect/permite/sincroniza/mensagem/debug; cartões longos, chips, switches, steppers e linhas navegáveis. | Maior oportunidade de reduzir cartões; preservar rótulo, ajuda e estados técnicos separados. | Lista longa em todos os tamanhos; casca vira rail. | Alto: DataStore, WorkManager, Health Connect e logs. |
| Perfis | Listar, criar, editar e ativar perfil. | Loading, vazio, conteúdo, validação/erro; `ProfileCard` e diálogo. | Avatar, perfil ativo e estado vazio podem ganhar clareza. | Lista/diálogo em coluna. | Alto: transação de perfil ativo. |
| Metas | Criar/editar e ler progresso neutro. | Sem perfil/meta, ativa, erro e validação; formulário, resumo e progresso linear. | Mostrar início/atual/alvo/restante sem moralizar. | Coluna única. | Médio: cálculo e estado da meta. |
| Dispositivos | Ver/remover balanças conhecidas. | Vazio, lista, remoção/erro; `DeviceCard` e metadados. | Tratar como lista de ajustes e não expor dados técnicos fora do contexto. | Coluna única. | Alto: associação histórica e privacidade. |
| Diagnóstico Bluetooth | Buscar anúncios e revisar dados técnicos. Secundárias: permissão, parar/limpar, expandir, copiar/compartilhar. | Sem BLE/Bluetooth/permissão, scan, timeout, `SecurityException`, vazio/erro/resultados; status, botões, contador e `DeviceCard` expansível. | Organizar status, ação e detalhes sem ocultar hex/UUID/manufacturer data. | Lista com detalhes expansíveis. | Muito alto: sem filtros, conexão ou protocolo inventado. |
| Privacidade e dados | Explicar armazenamento e executar exportação/exclusão explícita. | Conteúdo, confirmação digitando `EXCLUIR`, exclusão/erro; cartões e diálogo. | Destacar consequência e recuperação sem reduzir clareza destrutiva. | Coluna única. | Muito alto: dados locais e Health Connect. |
| Sobre | Mostrar versão, escopo e avisos. | Conteúdo estático em `AboutCard`. | Pode ser uma lista informativa menos pesada. | Coluna única. | Baixo. |
| Demo (debug) | Demonstrar fluxo de medição em desenvolvimento. | Fonte fake e estados isolados de release. | Deve ser identificada como `DEMO`. | Mesma estrutura de medição. | Alto: não aparecer em release. |
| Racional Health Connect | Explicar e encaminhar permissão de escrita. | `Activity` auxiliar; disponibilidade e retorno de permissão. | Ação e dados sensíveis devem permanecer explícitos. | Conforme janela da Activity. | Alto. |

## Componentes transversais

| Componente | Uso atual | Evolução visual permitida | Invariante |
| --- | --- | --- | --- |
| `WeightChart` | Dashboard e Histórico | Melhorar plotagem, seleção e resumo semântico. | Manter Canvas, pontos limitados e alternativa textual. |
| `BrazilianDateTextField` | Onboarding e formulários | Padronizar como campo do design system. | Preservar máscara e cursor `DD/MM/AAAA`. |
| Cartões locais | Praticamente todas as telas | Trocar por níveis claros de superfície. | Não criar cards dentro de cards. |
| Botões e chips | CTAs, filtros e escolhas | Definir prioridade e estado semântico. | Rótulo e área mínima de toque. |
| App bars | Diagnóstico e secundárias; ausentes em raízes | Unificar título, retorno e ação contextual. | Não quebrar rotas ou back stack. |

## Estados compartilhados a criar no redesign

Os seguintes estados devem receber componentes visuais consistentes, sem mudar
o estado de domínio: loading contextual, vazio com próxima ação, erro humano
com recuperação, sucesso/mensagem, status BLE textual, item de configuração e
linha de medição. Cada um precisa ter tema claro/escuro, alto contraste, fonte
ampliada e comportamento sem animação quando `VisualEffects.REDUCED` estiver
ativo.
