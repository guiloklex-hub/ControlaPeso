# Baseline de UI/UX — ControlaPeso

Data do registro: 28/07/2026.

## Escopo e limite desta etapa

Esta é uma leitura de base para o redesign. Nenhuma tela, regra de negócio,
ViewModel, repositório, parser, scanner BLE, dependência ou navegação foi
alterado para produzir este documento.

Foram lidos integralmente `AGENTS.md`, `UI_UX_AUDIT_ControlaPeso.md` e
`UI_UX_GOAL_ControlaPeso.md`. Também foram revisados `README.md`,
`docs/ARCHITECTURE.md`, `docs/PRIVACY.md`, `docs/TESTING.md`,
`IMPLEMENTATION_PLAN.md`, a configuração Gradle, o tema e os Composables
atuais.

## Produto e invariantes

O ControlaPeso é local-first, offline-first e não médico. O redesign deve
preservar integralmente a captura BLE por advertising, os parsers
OKOK/Chipsea, o detector de estabilidade, dados Room/DataStore, perfis, metas,
histórico, relatórios, backup, Health Connect, lembretes, privacidade e o modo
de demonstração apenas em debug.

Em particular, a UI nunca deve inferir uma unidade, estabilidade ou métrica
corporal que o domínio não tenha validado. Payload e endereço BLE seguem os
limites de privacidade atuais; detalhes técnicos continuam restritos ao
diagnóstico e a logs debug opt-in.

## Fotografia da implementação atual

| Área | Estado observado |
| --- | --- |
| Stack | Kotlin 2.2.10, Compose, Material 3, Navigation Compose, Room, DataStore e StateFlow/MVVM. |
| Tema | Claro, escuro, dinâmico, alto contraste e efeitos reduzidos já existem. A paleta própria usa Ocean/Aqua/Sand/Ink/Mist. |
| Tipografia | Apenas `bodyLarge` possui personalização explícita; pesos, variações, títulos e metadados ainda usam a escala Material padrão. |
| Formas e superfícies | Não há `Shapes` próprio nem tokens centrais para espaçamento, elevação, tamanho ou movimento. Há 34 usos diretos de `Card` na camada `ui` atual. |
| Responsividade | A casca usa `NavigationBar` abaixo de 600 dp e `NavigationRail` a partir de 600 dp. Dashboard e Histórico têm bifurcação manual em 840 dp com `BoxWithConstraints`. |
| Previews | Não há `@Preview` no código-fonte. |
| Movimento | Não foram encontrados `AnimatedContent`, `AnimatedVisibility`, `animate*AsState` ou transições equivalentes. |
| Testes visuais | Há testes Compose/instrumentados e smoke tests funcionais, mas não há screenshot tests ou referências visuais versionadas. |
| Componentes compartilhados | `WeightChart` e `BrazilianDateTextField` existem; os demais padrões de tela são em grande parte locais. |

## Diagnóstico visual inicial

O app já comunica confiança por meio de cor discreta, textos neutros e dados
locais, mas a superfície atual ainda parece uma composição de componentes
Material básicos. O principal ganho não virá de efeitos decorativos; virá de
hierarquia, repetição de padrões de produto e decisões visuais centralizadas.

1. Dashboard, Histórico e Ajustes concentram muitas informações em cartões de
   mesma ênfase. O último peso, o próximo passo e os dados de apoio competem.
2. A navegação se adapta na largura, mas o estado adaptável não é uma
   abstração reutilizável. Em medium, várias telas continuam uma coluna longa.
3. A tipografia não diferencia suficientemente peso ao vivo, último peso,
   variação, título de seção e metadados.
4. Estados de loading, vazio e erro existem, porém são implementados por tela
   e ainda não têm linguagem visual comum.
5. O gráfico é leve, acessível e baseado em Canvas, mas necessita melhor
   enquadramento, seleção e relação com o resumo textual.
6. A tela de medição possui os estados funcionais necessários, mas merece uma
   hierarquia visual centrada no valor e no estado da sessão, sem alterar o
   detector de estabilidade.

Pontos fortes a preservar: tema e alto contraste, fonte ampliada coberta por
testes, datas PT-BR, desenho local do gráfico, conteúdo técnico no diagnóstico,
ausência de telemetria e navegação primária que retorna à raiz de cada aba.

## Baseline técnico executado

O shell aberto em 28/07/2026 não tinha `java` no `PATH`. A primeira tentativa
terminou antes do Gradle com:

```bash
java -version
# /bin/bash: java: command not found
```

O bloqueio ambiental foi contornado sem alterar o projeto, usando o Temurin
21.0.12 já disponível em `/tmp/controlapeso-jdk21.YVQjvw`:

```bash
JAVA_HOME=/tmp/controlapeso-jdk21.YVQjvw \
PATH=/tmp/controlapeso-jdk21.YVQjvw/bin:$PATH \
./gradlew test lint assembleDebug --console=plain
```

Resultado atual: `BUILD SUCCESSFUL in 1s`, 77 tarefas acionáveis, uma
executada e 76 `UP-TO-DATE`. `test`, `lint` e `assembleDebug` concluíram sem
erro, com reutilização do configuration cache.

No levantamento imediatamente anterior, com o mesmo código de produção e o
mesmo JDK, também foram executados:

```bash
./gradlew test lint assembleDebug assembleRelease --continue
./gradlew assembleRelease --console=plain
```

O APK release não assinado foi gerado em
`app/build/outputs/apk/release/app-release-unsigned.apk`.

Não há bloqueio do projeto. A ausência de Java no `PATH` é um bloqueio do
ambiente de shell, reproduzível e já contornado com `JAVA_HOME` explícito.

Não foram executados nesta etapa:

- `connectedAndroidTest`, pois exige dispositivo/emulador desbloqueado e é
  desnecessário para uma auditoria documental; a variante `instrumented` deve
  continuar sendo usada para proteger os dados do APK debug;
- capturas de tela, porque ainda não existem previews, fixtures visuais nem
  roteiro de captura determinístico.

## Evidência visual de partida

Não existe baseline de screenshots versionado. Logo, não se deve afirmar
comparação visual antes/depois ainda. A primeira fase de execução criará
`docs/ui-ux/screenshots/before/` com um manifesto por captura: rota, estado,
largura, densidade, tema, font scale, build e data.

## Baseline do Figma

A conexão foi validada com a conta configurada e o Material 3 Design Kit está
disponível como biblioteca. Foi criado o arquivo vazio
[ControlaPeso UI Exploration](https://www.figma.com/design/yI28NOZ0CgbnQ6HLXCR4sV)
para a futura fase de direções visuais. Nenhum frame de tela foi produzido
nesta etapa documental.

O arquivo ainda não possui variáveis, estilos ou componentes locais. Também
não há Code Connect (`.figma.ts`/`.figma.js`) no repositório. Portanto, hoje
não existe divergência entre um sistema visual no Figma e o código; a lacuna é
justamente a ausência de uma biblioteca de produto em ambos os lados.

## Bloqueios e decisões pendentes

| Item | Situação | Tratamento planejado |
| --- | --- | --- |
| Sistema de design | Ausente como camada local | Criar tokens e componentes sobre Material 3, sem framework externo. |
| Material 3 Adaptive | Não está nas dependências atuais | Verificar compatibilidade sem atualização em cascata; manter abstração local se não for seguro. |
| Screenshot testing | Não configurado | Fazer prova de compatibilidade; usar captura instrumentada determinística como alternativa. |
| Figma MCP | Conexão confirmada; arquivo de exploração criado e ainda vazio | Criar variáveis, componentes e frames apenas na fase visual, nunca gerar código automaticamente. |
| Validação de BLE e TalkBack | Requer telefone físico | Planejar separadamente; não será simulada por fixtures. |

## Critério para encerrar o baseline

O baseline está concluído quando a equipe consegue comparar uma alteração
visual contra este registro, reproduzir os builds e identificar que nenhum dos
contratos BLE ou de privacidade foi colocado em risco. Esse critério foi
atendido para a fase documental.
