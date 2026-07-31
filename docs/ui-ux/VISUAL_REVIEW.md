# Revisão visual — ControlaPeso

Revisões executadas em 28/07/2026 no Samsung SM-S908E, Android 16/API 36,
1080 × 2316 px, densidade física 450 dpi com override 420 dpi. Os dados
visíveis (`Teste_Debug` e medições de teste) são artificiais.

## Método

- comparação do Dashboard instalado antes do redesign com o novo APK;
- previews determinísticos para estados que não dependem de hardware;
- capturas físicas em retrato e paisagem;
- temas claro e escuro;
- fonte 80% e 200%;
- contraste reforçado e efeitos reduzidos;
- inspeção de hierarquia, espaçamento, overflow, ações, estados e navegação;
- suíte Compose instrumentada no mesmo telefone.

O detalhe em forma de alça na borda direita das capturas em retrato pertence
ao Samsung Edge Panel, não à aplicação.

## Rodada 1 — estrutura e hierarquia

Evidências principais:

- `before/dashboard-phone.png`;
- `after/dashboard-phone-dark-expanded.png`;
- `after/dashboard-phone-light-compact.png`;
- `after/measure-menu-phone-dark-expanded.png`;
- `after/live-scanning-phone-dark-expanded.png`;
- `after/history-phone-dark-expanded.png`;
- `after/settings-phone-dark-expanded.png`;
- `after/dashboard-phone-light-font200.png`;
- `after/manual-form-phone-light-font200.png`.

### Achados e correções

| Achado | Severidade | Correção |
| --- | --- | --- |
| As cinco legendas da barra inferior se sobrepunham com fonte 200%. | Alta | Rótulos visuais curtos a partir de 150%, mantendo descrições completas nos ícones. Teste de bounds adicionado. |
| “Medir com a balança” e “Adicionar manualmente” tinham a mesma ênfase no menu Medir. | Média | A balança permanece em `PrimaryActionCard`; o registro manual usa `SecondaryActionCard`. |
| Estado vazio do Dashboard repetia a mesma frase como título e corpo. | Média | Títulos e explicações distintos para perfil ausente, medições ausentes e erro. |
| Paleta de alto contraste herdava containers roxos padrão do Material. | Média | Containers primário, secundário, terciário e variantes foram definidos explicitamente. |
| O texto “Aguardando alguém subir na balança” era dominante demais no layout anterior. | Média | Estado de espera usa `headlineSmall`; somente um peso recebido usa `displayLarge`. |
| Telas secundárias esticavam conteúdo em paisagem. | Média | `ResponsiveScreenList` com largura máxima e padding 16/24/32 dp. |

### Resultado

Dashboard ganhou uma ação primária, hero tonal e apoio secundário. Histórico,
Medição e Ajustes mostram hierarquia mais clara. Nenhum achado exigiu alteração
em BLE, banco ou domínio.

## Rodada 2 — refinamento, acessibilidade e consistência

Evidências principais:

- `after/dashboard-phone-light-font200-round2.png`;
- `after/manual-form-phone-light-font200-round2.png`;
- `after/settings-phone-light-expanded.png`;
- `after/settings-phone-light-high-contrast-reduced.png`;
- `after/live-waiting-phone-dark-expanded.png`;
- `after/history-phone-dark-expanded.png`.

### Verificações

| Critério | Resultado |
| --- | --- |
| Barra compacta em fonte 200% | Rótulos `Início`, `Hist.`, `Medir`, `Relat.` e `Ajust.` ficam em uma linha e não se sobrepõem. |
| Formulário em fonte 200% | Título e descrição refluem; campos continuam roláveis; máscara `DD/MM/AAAA` permanece legível. |
| Tema escuro | Hero, superfícies baixas, rail e status mantêm contraste e hierarquia. |
| Tema claro | Superfícies tonais e bordas continuam distintas sem depender de sombra. |
| Contraste reforçado | Texto preto/branco e containers teal explícitos substituem defaults inconsistentes. |
| Efeitos reduzidos | A preferência permanece ativa; o redesign não adicionou animação contínua ou informação exclusiva em movimento. |
| Compact | Barra inferior, padding de 16 dp, CTA e hero refluem em retrato. |
| Expanded | Rail, largura máxima e painéis evitam coluna esticada; Histórico mantém contexto. |
| Estado vazio/erro/loading | Componentes compartilhados têm título, explicação e ação quando existe recuperação. |
| Diagnóstico | Dados técnicos continuam expansíveis, selecionáveis e separados das telas comuns. |

### Teste associado

`largeFontUsesDistinctCompactNavigationLabels` renderiza o NavHost com
`fontScale = 2f`, exige a presença dos cinco rótulos curtos e compara os bounds
para impedir sobreposição. `connectedAndroidTest` executou 19 testes no
SM-S908E com sucesso após a correção.

## Pendências humanas

- Executar TalkBack em todos os fluxos e confirmar a ordem de foco por uma
  pessoa usando leitor de tela.
- Validar navegação por teclado físico/ChromeOS em hardware apropriado.
- Refazer a comparação screenshot versus frame quando o conector de escrita
  do Figma deixar de retornar `INVALID_ARGUMENT`; os frames finais não foram
  criados e não são alegados como evidência.
- Uma medição BLE física não foi necessária para validar o redesign; o
  scanner/parser foram protegidos por testes, mas uma sessão real continua
  recomendada antes de release.
