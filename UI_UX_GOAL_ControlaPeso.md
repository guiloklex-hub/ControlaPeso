# GOAL — Redesign completo de UI/UX do ControlaPeso

## Contexto do projeto

Você está trabalhando no aplicativo Android existente **ControlaPeso**.

- Package/namespace: `br.com.paivalab.controlapeso`
- Stack: Kotlin, Jetpack Compose, Material 3, Room, DataStore, Navigation Compose, WorkManager e Health Connect.
- O aplicativo já está funcional.
- A captura de peso por BLE e os parsers atuais funcionam e são invariantes críticos.
- Histórico, perfis, metas, relatórios, backup, compartilhamento, lembretes, diagnóstico e preferências já existem.

Este objetivo é exclusivamente uma evolução de **UI, UX, design system, responsividade visual, acessibilidade e validação visual**.

## Objetivo principal

Transformar a interface atual em uma experiência Android moderna, elegante, clara, adaptável e com qualidade de produto pronto para publicação, preservando integralmente as funcionalidades existentes.

Não interprete “moderno” como uso indiscriminado de gradientes, vidro, blur ou animações. Tome decisões de design com base em usabilidade, hierarquia, consistência, acessibilidade, adaptação e desempenho.

## Invariantes que não podem regredir

Não alterar o comportamento funcional de:

- scanner BLE;
- parser OKOK/Chipsea;
- detector de estabilidade;
- persistência Room;
- proteção contra duplicidade;
- perfis;
- metas;
- histórico;
- medições manuais;
- relatórios PDF/CSV/JSON;
- backup e restauração;
- FileProvider e compartilhamento;
- Health Connect;
- lembretes;
- privacidade;
- modo de demonstração debug;
- diagnóstico BLE.

Não reescrever ViewModels, repositories ou use cases apenas para facilitar mudanças visuais.

Mudanças de estado só são permitidas quando necessárias para representar corretamente um estado visual que já existe ou corrigir uma falha concreta de UX.

## Modo de execução autônoma

Você deve decidir a melhor UI/UX por meio de um processo verificável, e não por preferência arbitrária.

Execute nesta ordem:

1. Auditoria do código e da interface atual.
2. Inventário das telas, estados e componentes.
3. Pesquisa visual e técnica.
4. Definição de três direções visuais.
5. Avaliação objetiva das três direções.
6. Escolha autônoma da melhor direção.
7. Criação do design system.
8. Criação de previews e fixtures.
9. Implementação por prioridade.
10. Geração e revisão de screenshots.
11. Segunda rodada de refinamento.
12. Testes e entrega documentada.

Não comece alterando todas as telas imediatamente.

## Fase 0 — Baseline seguro

Antes de alterar código:

- leia `AGENTS.md`;
- leia `README.md`;
- leia `docs/ARCHITECTURE.md`;
- leia `docs/PRIVACY.md`;
- leia `docs/TESTING.md`;
- leia `IMPLEMENTATION_PLAN.md`;
- localize todas as telas e componentes Compose;
- execute os testes e builds disponíveis;
- registre o resultado inicial.

Crie:

- `docs/ui-ux/UI_UX_BASELINE.md`;
- `docs/ui-ux/UI_INVENTORY.md`;
- `docs/ui-ux/UI_UX_PLAN.md`;
- `docs/ui-ux/UI_UX_PROGRESS.md`.

`UI_INVENTORY.md` deve listar para cada tela:

- objetivo da tela;
- ação principal;
- ações secundárias;
- estados de loading, vazio, erro e sucesso;
- componentes atuais;
- problemas de hierarquia;
- problemas de acessibilidade;
- comportamento compact/medium/expanded;
- risco funcional da alteração.

Não altere lógica de negócio nesta fase.

## Fase 1 — Pesquisa orientada a evidências

Quando acesso à web estiver disponível, pesquise somente fontes confiáveis e atuais.

Priorize:

- documentação oficial do Android;
- Material Design 3 e Material 3 Expressive;
- Material 3 Adaptive;
- exemplos oficiais do Jetpack Compose;
- recomendações oficiais de acessibilidade Android;
- apps de saúde e balanças inteligentes apenas como referência secundária.

Analise referências de produtos como:

- Withings;
- Google Fit/Health Connect;
- Fitbit;
- RENPHO Health;
- Eufy Life;
- OKOK International.

Não copie identidade visual, ilustrações, textos, fluxos proprietários ou layouts inteiros.

Extraia apenas padrões gerais:

- como destacam o último peso;
- como mostram tendência;
- como representam metas;
- como organizam histórico;
- como apresentam medição ao vivo;
- como reduzem carga cognitiva;
- como tratam múltiplos perfis;
- como diferenciam medido de calculado;
- como usam estados vazios;
- como apresentam dados sensíveis.

Registre a pesquisa em `docs/ui-ux/UI_REFERENCE_NOTES.md`, incluindo fonte, padrão observado, benefício e risco.

Se Figma MCP estiver conectado, use-o como ferramenta de exploração e contexto visual. Se não estiver conectado, continue normalmente com Material 3, previews e screenshots locais.

## Fase 2 — Três direções visuais

Crie três propostas conceituais distintas. Não implemente as três por completo.

Cada direção deve especificar:

- nome;
- personalidade;
- princípios;
- paleta conceitual;
- tipografia;
- formas;
- densidade;
- tratamento de superfícies;
- navegação;
- dashboard;
- medição ao vivo;
- histórico;
- movimento;
- telas grandes;
- acessibilidade;
- riscos técnicos.

Use como ponto de partida:

### Direção A — Calm Health

- clara e serena;
- superfícies limpas;
- cores suaves;
- grande foco em legibilidade;
- pouco efeito decorativo.

### Direção B — Data Confidence

- orientada a dados;
- hierarquia forte;
- gráficos e estatísticas protagonistas;
- visual técnico, porém amigável.

### Direção C — Warm Progress

- mais humana e acolhedora;
- foco em progresso e metas;
- formas mais expressivas;
- uso moderado de cor e movimento.

Você pode renomear ou ajustar as direções após analisar o produto.

### Matriz obrigatória de decisão

Pontue cada direção de 0 a 10:

- Usabilidade e clareza — peso 25%;
- Hierarquia da informação — peso 20%;
- Acessibilidade — peso 20%;
- Consistência e escalabilidade — peso 15%;
- Adaptabilidade — peso 10%;
- Desempenho e simplicidade técnica — peso 10%.

Crie `docs/ui-ux/DESIGN_DIRECTIONS.md` com a matriz, justificativa e decisão final.

Escolha autonomamente a maior pontuação ponderada.

Em caso de empate, escolha a opção com:

1. melhor acessibilidade;
2. menor risco funcional;
3. menor complexidade;
4. melhor legibilidade de dados.

## Direção de produto obrigatória

Independentemente da proposta escolhida, o produto deve transmitir:

- calma;
- confiança;
- privacidade;
- clareza;
- progresso sem culpa;
- tecnologia discreta;
- precisão sobre o que é medido e o que é derivado.

Evite:

- estética clínica fria demais;
- aparência de jogo;
- gamificação agressiva;
- vermelho para simples aumento de peso;
- verde para simples perda de peso;
- mensagens moralistas;
- excesso de blur;
- glassmorphism em todas as superfícies;
- gradiente em todos os componentes;
- sombras grandes;
- cards dentro de cards;
- animações contínuas;
- ícones sem rótulo em ações importantes;
- densidade excessiva;
- layout “dashboard corporativo”.

## Fase 3 — Design system local

Crie um design system próprio em cima de Material 3.

Estrutura sugerida:

```text
ui/designsystem/
    ControlaPesoDesignSystem.kt
    tokens/
        ColorTokens.kt
        TypographyTokens.kt
        ShapeTokens.kt
        SpacingTokens.kt
        ElevationTokens.kt
        MotionTokens.kt
        SizeTokens.kt
    components/
        AppBackground.kt
        AppTopBar.kt
        SectionHeader.kt
        HeroMetricCard.kt
        MetricTile.kt
        TrendBadge.kt
        StatusPill.kt
        PrimaryActionCard.kt
        EmptyState.kt
        ErrorState.kt
        LoadingState.kt
        SettingsItem.kt
        SettingsSection.kt
        ProfileAvatar.kt
        MeasurementListItem.kt
        GoalProgressCard.kt
        AdaptiveContentPane.kt
```

Adapte nomes e quantidade quando necessário. Não crie wrappers inúteis.

### Cores

Defina papéis semânticos além dos papéis Material:

- connected;
- disconnected;
- stable;
- measuring;
- informational;
- trendUp;
- trendDown;
- trendNeutral;
- chartPrimary;
- chartAverage;
- chartGrid;
- glass/scrim quando realmente usado.

Tendência de peso não deve usar automaticamente semântica de “bom” ou “ruim”.

Mantenha:

- tema claro;
- tema escuro;
- seguir sistema;
- cores dinâmicas;
- alto contraste;
- efeitos reduzidos.

Quando cores dinâmicas estiverem ativas, tokens semânticos devem continuar legíveis e coerentes.

### Tipografia

O tema atual personaliza pouco a tipografia. Defina uma escala completa.

Inclua tratamento específico para:

- peso ao vivo;
- último peso;
- variação;
- títulos de tela;
- títulos de seção;
- estatísticas;
- listas;
- metadados;
- labels;
- botões.

Use números tabulares quando possível e apropriado para pesos e estatísticas.

Não adicione fonte externa por rede.

Caso adicione uma fonte empacotada:

- justifique licença e tamanho;
- mantenha fallback;
- não comprometa legibilidade;
- não use fonte decorativa para dados.

### Formas

Forneça `Shapes` ao `MaterialTheme`.

Use uma família coerente para:

- botões;
- chips;
- cards;
- sheets;
- campos;
- navegação;
- hero components.

Evite tornar todos os elementos excessivamente arredondados.

### Espaçamento

Crie escala de espaçamento, por exemplo:

- 4;
- 8;
- 12;
- 16;
- 20;
- 24;
- 32;
- 40;
- 48 dp.

Substitua valores espalhados somente quando isso melhorar consistência.

### Elevação e superfícies

Use elevação tonal do Material 3.

Reduza a dependência de `Card` para todo agrupamento.

Escolha entre:

- seção sem contêiner;
- superfície tonal;
- card elevado;
- card outlined;
- container destacado.

Cada nível deve ter função clara.

### Movimento

Crie tokens de duração e easing.

Use movimento apenas para:

- mudança de estado BLE;
- estabilização da medição;
- expansão/recolhimento;
- seleção de período;
- troca de conteúdo;
- navegação de painel;
- progresso de meta;
- entrada de estados vazios ou resultados.

Respeite `VisualEffects.REDUCED`.

Quando efeitos reduzidos estiverem ativos:

- evite transições decorativas;
- use mudanças imediatas ou curtas;
- não use pulsação;
- não use parallax;
- não dependa de movimento para comunicar estado.

## Fase 4 — Fixtures e previews

Crie dados de preview determinísticos e reutilizáveis.

Estrutura sugerida:

```text
ui/preview/
    PreviewProfiles.kt
    PreviewMeasurements.kt
    PreviewGoals.kt
    PreviewDevices.kt
    PreviewStates.kt
    ControlaPesoPreviews.kt
```

Todos os previews devem ser puros e não acessar:

- banco;
- Bluetooth;
- Health Connect;
- arquivos;
- relógio real;
- rede.

Crie previews para as telas prioritárias nos estados:

- loading;
- vazio;
- erro;
- conteúdo mínimo;
- conteúdo completo;
- texto longo;
- números extremos plausíveis;
- tema claro;
- tema escuro;
- fonte 1.0;
- fonte 1.5 ou superior;
- 360 dp;
- 600 dp;
- 840 dp.

Crie multi-preview para:

- temas;
- tamanhos;
- fonte;
- locale pt-BR.

Não precisa gerar todas as combinações cartesianas. Selecione combinações de maior risco.

## Fase 5 — Navegação adaptável

A navegação atual usa decisões manuais de largura. Centralize o estado adaptável.

Quando compatível com as versões atuais, considere Material 3 Adaptive:

- `currentWindowAdaptiveInfo()`;
- `NavigationSuiteScaffold`;
- `ListDetailPaneScaffold`;
- `SupportingPaneScaffold`.

Não adicione dependência se a versão compatível exigir atualização em cascata arriscada. Nesse caso, crie uma abstração local baseada em classes de tamanho.

Comportamento esperado:

### Compact

- barra inferior;
- ação de medição com destaque claro;
- uma coluna;
- conteúdo edge-to-edge com insets corretos.

### Medium

- NavigationRail ou solução equivalente;
- maior largura de conteúdo;
- duas colunas quando realmente úteis.

### Expanded/Large

- NavigationRail ou drawer;
- lista-detalhe no histórico;
- dashboard em painéis;
- medição com conteúdo e apoio lado a lado;
- configurações com categorias e detalhe quando adequado;
- largura máxima para textos e formulários.

A interface deve reagir à janela atual, não ao modelo físico do aparelho.

## Fase 6 — Redesign por prioridade

### 6.1 Dashboard

O dashboard deve ser a melhor tela do produto.

Objetivos:

- permitir entender a situação em poucos segundos;
- destacar o último peso;
- mostrar tendência sem julgamento;
- mostrar progresso da meta;
- oferecer ação de medição imediatamente;
- reduzir ruído.

Estrutura recomendada:

1. Top app bar com perfil e ações úteis.
2. Hero do último peso.
3. Ação primária “Medir agora”.
4. Tendência e variação.
5. Gráfico resumido.
6. Progresso da meta.
7. Estatísticas secundárias.
8. Ações rápidas discretas.

O hero deve conter:

- valor;
- unidade;
- horário;
- origem;
- variação;
- estado da balança;
- CTA principal.

Evite uma fileira longa de botões com a mesma ênfase.

### 6.2 Medição ao vivo

Esta tela deve ter foco absoluto na medição.

Crie uma hierarquia por estados:

- preparando Bluetooth;
- procurando;
- balança detectada;
- aguardando usuário;
- peso variando;
- estabilizando;
- estável;
- salvo;
- erro recuperável.

O valor do peso deve ser o principal elemento.

Use:

- indicador de conexão;
- superfície ou arco visual suave;
- transição de valor;
- texto de orientação curto;
- ação de cancelar;
- seleção de perfil acessível;
- feedback tátil existente quando aplicável.

Não use animação contínua pesada.

Não salve leituras intermediárias.

Não altere o algoritmo de estabilidade.

### 6.3 Histórico

Objetivos:

- facilitar leitura de tendência;
- permitir trocar período rapidamente;
- destacar medição selecionada;
- manter lista escaneável;
- funcionar em lista-detalhe em telas grandes.

Melhore o gráfico:

- margem e área de plotagem;
- labels mais claros;
- tooltip/seleção;
- datas legíveis;
- linha principal;
- média móvel diferenciada;
- acessibilidade textual;
- estado vazio visual;
- suporte a tema escuro.

Não exagere pequenas variações pela escala.

### 6.4 Navegação e app bars

Adote padrão coerente de top app bar.

- Telas raiz: título, perfil/contexto e ações principais.
- Telas secundárias: navegação para trás, título e ação contextual.
- Diagnóstico: manter caráter técnico, mas alinhado ao design system.

Use ícones Material adequados e rótulos acessíveis.

### 6.5 Configurações

A tela atual possui muitas seções em cards e chips.

Redesenhe como lista de configurações moderna:

- cabeçalhos de seção;
- itens com ícone inicial;
- título;
- texto de apoio;
- controle final;
- divisores ou agrupamento tonal;
- navegação para subtelas quando a seção for extensa.

Não coloque todas as opções na mesma tela quando isso prejudicar compreensão.

Preserve a busca fácil por:

- aparência;
- medição;
- histórico;
- lembretes;
- integrações;
- privacidade;
- diagnóstico;
- sobre.

### 6.6 Perfis

- avatar consistente;
- indicação clara de perfil ativo;
- ação de trocar perfil;
- cards ou linhas com hierarquia;
- estado vazio amigável;
- formulário com seções.

### 6.7 Metas

- progresso visual neutro;
- alvo, início, atual e restante;
- datas como apoio;
- não moralizar resultado;
- ações de editar/pausar/concluir com hierarquia correta.

### 6.8 Relatórios

- seleção de período e opções em etapas claras;
- resumo do que será exportado;
- CTA principal único;
- arquivos gerados em lista com tipo, data, tamanho e ações;
- estados de geração e erro.

### 6.9 Formulários

Padronize:

- título;
- descrição;
- campos;
- ajuda e erro;
- ação primária;
- ação secundária;
- teclado;
- scroll com fonte grande;
- sheets ou telas conforme complexidade.

### 6.10 Estados vazios, loading e erro

Crie componentes consistentes.

Cada estado vazio deve explicar:

- o que está vazio;
- por que isso importa;
- a próxima ação.

Use vetores simples ou ícones, sem adicionar imagens grandes.

Loading:

- prefira placeholders ou indicador contextual;
- evite spinner isolado em tela branca.

Erro:

- explique em linguagem humana;
- ofereça recuperação quando possível;
- preserve detalhe técnico apenas no diagnóstico.

## Fase 7 — Componentização consciente

Refatore arquivos Compose grandes apenas onde isso melhorar:

- previews;
- testes;
- legibilidade;
- reutilização;
- consistência.

Evite componentes genéricos demais.

Uma boa divisão deve representar conceitos de produto, por exemplo:

- `LatestWeightHero`;
- `MeasurementStatusHeader`;
- `HistoryPeriodSelector`;
- `GoalProgressOverview`;
- `SettingsNavigationItem`.

Não crie funções minúsculas apenas para reduzir linhas.

## Fase 8 — Validação visual obrigatória

O redesign não pode ser considerado concluído apenas porque compilou.

### Previews

Renderize e revise previews das telas prioritárias.

### Screenshot tests

Se compatível com o projeto sem atualização arriscada, configure Compose Preview Screenshot Testing.

Crie referências para:

- Dashboard claro compacto;
- Dashboard escuro compacto;
- Dashboard expanded;
- Medição aguardando;
- Medição variando;
- Medição estável;
- Histórico com dados;
- Histórico vazio;
- Histórico expanded;
- Configurações claro;
- Configurações escuro;
- formulário com fonte grande.

Se a ferramenta experimental não for apropriada, use testes instrumentados com captura de screenshot ou documente uma alternativa determinística.

### Emulador ou dispositivo

Quando disponível:

- instale debug;
- use dados demo;
- capture screenshots;
- salve em `docs/ui-ux/screenshots/after/`;
- registre resolução, densidade, tema e font scale.

Se houver baseline visual anterior, salve em `before/`.

### Revisão em duas rodadas

Rodada 1:

- identificar hierarquia fraca;
- inconsistência de espaçamento;
- contraste;
- overflow;
- ações sem prioridade;
- cards excessivos;
- problemas de telas grandes.

Corrija.

Rodada 2:

- revisar refinamento;
- verificar tema escuro;
- fonte ampliada;
- estados vazios;
- movimento reduzido;
- navegação;
- consistência.

Registre os achados em `docs/ui-ux/VISUAL_REVIEW.md`.

## Fase 9 — Acessibilidade

Verifique:

- TalkBack;
- ordem de foco;
- labels;
- descriptions somente onde úteis;
- botões com pelo menos 48 dp;
- contraste;
- fonte 200%;
- truncamento;
- navegação por teclado;
- gráficos com descrição textual;
- estados que não dependem de cor;
- movimento reduzido;
- feedback de conexão textual.

Use Android Studio Compose UI Check quando disponível.

Não marque acessibilidade como concluída sem evidência.

## Fase 10 — Desempenho visual

Evite:

- blur custoso em listas;
- sombras grandes repetidas;
- gradientes animados;
- recomposição do gráfico a cada frame;
- animações infinitas;
- criação repetida de objetos gráficos;
- listas sem chaves estáveis;
- efeitos que degradam aparelhos modestos.

Revise recomposições com ferramentas do Compose quando disponível.

Mantenha o app leve.

## Regras sobre dependências

Antes de adicionar biblioteca:

1. verifique se Compose/Material 3 já resolve;
2. verifique compatibilidade com o BOM atual;
3. avalie tamanho e manutenção;
4. documente justificativa;
5. não atualize stack em cascata apenas por aparência.

Não adicione biblioteca de gráfico sem demonstrar vantagem concreta sobre o Canvas atual.

Não adicione biblioteca de animação pesada.

Não adicione biblioteca de glassmorphism.

Não adicione sistema de design externo completo.

## Uso opcional do Figma MCP

Se o Figma MCP estiver disponível:

1. Crie um arquivo ou página chamada `ControlaPeso UI Exploration`.
2. Crie variáveis para cor, espaçamento, raio e tipografia.
3. Crie os componentes principais do design system.
4. Crie frames para:
   - Dashboard compacto;
   - Dashboard expanded;
   - Medição ao vivo;
   - Histórico;
   - Configurações.
5. Compare as três direções visuais.
6. Escolha a direção pela matriz obrigatória.
7. Use o frame vencedor como contexto, não como código final automático.
8. Implemente em Compose usando componentes reais do projeto.
9. Compare screenshot do app com o frame.

Não aceite automaticamente código genérico fornecido pelo MCP.

O código final é responsabilidade do projeto e deve seguir `AGENTS.md`.

## Uso de pesquisa e referências

Não use “tendências de Dribbble” como única referência.

Não copiar interfaces.

Toda escolha importante deve responder:

- melhora qual tarefa do usuário?
- reduz qual atrito?
- melhora qual informação?
- funciona em tema escuro?
- funciona em 320/360 dp?
- funciona em 600/840 dp?
- funciona com fonte ampliada?
- qual o custo técnico?

## Atualização de progresso

Mantenha `docs/ui-ux/UI_UX_PROGRESS.md` atualizado.

Formato mínimo:

```markdown
# Progresso UI/UX

## Fase atual

## Concluído

## Em andamento

## Evidências visuais

## Validações

## Problemas encontrados

## Próxima ação
```

Atualize após cada fase e antes de encerrar.

## Validações técnicas

Execute:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
./gradlew assembleRelease
```

Quando dispositivo/emulador estiver disponível:

```bash
./gradlew connectedAndroidTest
```

Quando screenshot tests forem configurados, execute as tasks correspondentes de atualização e validação.

Também execute:

```bash
git diff --check
git status --short
```

Não:

- desabilite lint;
- remova testes;
- esconda falhas;
- use suppression genérica;
- altere BLE para fazer UI funcionar;
- afirme validação visual sem screenshots;
- afirme teste físico sem aparelho.

## Critérios objetivos de conclusão

O objetivo só está concluído quando:

1. O BLE e a identificação de peso continuam funcionando.
2. O comportamento funcional existente não regrediu.
3. Existe um design system documentado.
4. Cor, tipografia, formas, espaçamento, elevação e movimento possuem tokens.
5. Dashboard foi redesenhado.
6. Medição ao vivo foi redesenhada.
7. Histórico e gráfico foram refinados.
8. Navegação é adaptável.
9. Configurações foram reorganizadas.
10. Perfis, metas, relatórios e formulários seguem o design system.
11. Estados loading/vazio/erro são consistentes.
12. Tema claro está validado.
13. Tema escuro está validado.
14. Alto contraste continua funcional.
15. Efeitos reduzidos continuam funcionais.
16. Compact, medium e expanded estão validados.
17. Fonte ampliada está validada.
18. Existem previews para telas e estados críticos.
19. Existem evidências de pelo menos duas rodadas de revisão visual.
20. Testes e builds obrigatórios passam.
21. Não há placeholders ou TODOs críticos.
22. Documentação UI/UX está atualizada.

## Entrega final

Apresente:

1. diagnóstico da UI anterior;
2. referências pesquisadas;
3. três direções propostas;
4. matriz de decisão;
5. direção escolhida e justificativa;
6. design system criado;
7. componentes reutilizáveis;
8. telas redesenhadas;
9. alterações adaptáveis;
10. melhorias de acessibilidade;
11. animações e comportamento reduzido;
12. previews criados;
13. screenshot tests ou alternativa;
14. screenshots antes/depois disponíveis;
15. dependências adicionadas e justificativas;
16. arquivos criados;
17. arquivos alterados;
18. testes executados;
19. resultados de lint e build;
20. limitações e itens que precisam de teste físico;
21. saída de `git status --short`.

Não faça commit.

Não faça push.

Não publique.

Comece pela auditoria e pelo baseline, depois siga o processo autônomo descrito neste arquivo.
