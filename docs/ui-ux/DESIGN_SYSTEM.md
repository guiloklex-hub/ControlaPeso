# Design system UI — ControlaPeso

Atualizado em 29/07/2026. A direção escolhida é **Calm Health**, vencedora da
matriz registrada em `DESIGN_DIRECTIONS.md`.

## Princípios

1. Peso e estado da medição têm prioridade; detalhes vêm depois.
2. Uma ação preenchida principal por região. Ações secundárias são tonais,
   outlined ou textuais.
3. Aumento e redução são variações matemáticas, não julgamentos de saúde.
4. Estado BLE sempre possui texto; cor e movimento nunca são a única pista.
5. Payload, endereço e sinais técnicos ficam no diagnóstico.
6. A interface deve refluír com fonte 200% e manter alvos de pelo menos 48 dp.

## Tokens Compose

Todos os tokens ficam em
`app/src/main/java/br/com/paivalab/controlapeso/ui/designsystem/tokens/`.

| Grupo | Contrato |
| --- | --- |
| Cor | Cores Material ativas são traduzidas em `connected`, `disconnected`, `stable`, `measuring`, `informational`, tendências e cores do gráfico. Isso preserva temas estático, dinâmico e alto contraste. |
| Tipografia | Escala Material completa; peso ao vivo usa 56 sp e último peso 44 sp, com títulos e corpos coerentes. A fonte continua local ao sistema, sem download. |
| Forma | Raios coerentes de 8, 12, 20 e 28 dp, aplicados pelo `MaterialTheme`. |
| Espaçamento | Escala de 4 a 48 dp: `xxs`, `xs`, `sm`, `md`, `lg`, `xl`, `xxl`, `xxxl` e `huge`. |
| Elevação | Dois níveis tonais discretos; sombra não é usada para decorar listas. |
| Movimento | Durações curta, média e longa; a preferência `VisualEffects.REDUCED` continua chegando ao tema. |
| Tamanho | Alvo mínimo de 48 dp, paddings 16/24/32 dp e conteúdo máximo de 1200 dp. |

`ControlaPesoTheme` continua sendo a entrada única. Ele fornece
`MaterialTheme` e `ControlaPesoDesignSystem` sem framework adicional.

## Adaptação

`controlaPesoWindowSize` é a fonte comum:

- compact: menor que 600 dp;
- medium: 600 a 839 dp;
- expanded: 840 dp ou mais.

Compact usa barra inferior. Medium e expanded usam rail. O conteúdo recebe
largura máxima e paddings progressivos; expanded só usa mais de um painel
quando isso reduz navegação, como no Dashboard e Histórico.

Com fonte a partir de 150%, a navegação usa rótulos visuais curtos (`Hist.`,
`Relat.` e `Ajust.`). Ícones continuam com descrições completas, portanto a
abreviação não reduz o nome acessível.

## Componentes

| Componente | Uso |
| --- | --- |
| `AppBackground` | Fundo contínuo da aplicação. |
| `ResponsiveScreenList` | Lista de tela com padding e largura máxima adaptáveis. |
| `AdaptiveContentPane` | Conteúdo livre com classificação de janela. |
| `HeroMetricCard` | Último peso ou valor dominante. |
| `MetricTile` | Estatística secundária. |
| `StatusPill` / `TrendBadge` | Estado textual curto e tendência neutra. |
| `StatusCard` | Estado contextual com ícone, texto e uma ação acessível. |
| `DataOriginBadge` | Origem explícita de uma medição, sem alterar o dado persistido. |
| `MeasurementUnitSelector` | Seleção visual e semântica de kg/lb em formulários. |
| `GoalProgressCard` | Progresso de meta com resumo textual e ações refluíveis. |
| `PrimaryActionCard` / `SecondaryActionCard` | Hierarquia explícita de ação. |
| `EmptyState`, `LoadingState`, `ErrorState` | Estados humanos e consistentes. |
| `ProfileAvatar` | Identificação inicial de perfil sem imagem externa. |
| `MeasurementListItem` | Linha escaneável de medição. |
| `SettingsItem`, `SettingsSection` | Itens e agrupamentos tonais de ajustes. |
| `SectionHeader` | Título, texto de apoio e ação contextual. |

## Gráfico

`WeightChart` continua em Canvas e não recebeu dependência. As linhas principal,
média e grade usam tokens semânticos do tema. A descrição textual existente
continua disponível para tecnologia assistiva; a escala não força pequenas
variações a parecerem grandes.

## Dados fake e previews

Fixtures estão em `ui/preview/` e usam IDs, datas e medições fixas. Elas não
acessam Android I/O, Room, Bluetooth, Health Connect, rede ou relógio real.

Previews cobrem:

- Dashboard claro, escuro, medium com fonte 150%, expanded e vazio;
- medição aguardando, variando e estável;
- Histórico compacto, expanded e vazio;
- Ajustes claro e escuro;
- formulário com fonte 200%;
- Perfis, Metas, Balanças e Relatórios.

O plugin experimental Compose Preview Screenshot Testing não foi adicionado:
ele exigiria uma nova ferramenta alfa sem necessidade comprovada. A
alternativa adotada combina previews determinísticos, testes Compose
instrumentados e capturas do telefone versionadas.

## Figma

Arquivo:
[ControlaPeso UI Exploration](https://www.figma.com/design/yI28NOZ0CgbnQ6HLXCR4sV).

Foram criadas 82 variáveis nativas, nove estilos de texto, dois estilos de
elevação, Cover e Foundations. O plano Starter permite um modo por coleção;
por isso claro e escuro usam coleções semânticas separadas.

A sessão do conector passou a responder `INVALID_ARGUMENT` até em leituras
mínimas antes da criação dos componentes e dos frames comparativos. As três
direções e a matriz foram especificadas no repositório, mas não se afirma que
os frames finais existem no Figma. Esse é um bloqueio externo pendente, não
substituído por mockups inventados.

## Limites

- O design system não altera scanner, parser, detector de estabilidade,
  persistência, exportação, Health Connect ou regras de domínio.
- Não há animação nova contínua, blur, gradiente animado ou fonte remota.
- TalkBack precisa de uma rodada humana com leitor de tela; testes de
  semântica e inspeção visual não substituem essa validação.
