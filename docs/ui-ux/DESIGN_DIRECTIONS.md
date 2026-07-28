# Direções visuais — ControlaPeso

Decisão registrada em 28/07/2026. As propostas partem do mesmo conteúdo e dos
mesmos estados funcionais para que a comparação seja visual, não uma disputa
entre escopos diferentes.

## A — Calm Health

- **Personalidade:** serena, precisa, privada e humana sem ser infantil.
- **Princípios:** uma informação principal por região; texto antes de
  decoração; cor como organização; progresso sem julgamento.
- **Paleta:** Ocean/Aqua existentes como ação e estados; Mist/Ink em
  superfícies; Sand apenas para apoio/tendência, nunca para “bom” ou “ruim”.
- **Tipografia:** Roboto; peso ao vivo 56/64, último peso 44/52, títulos
  32/40 e corpo 16/24. Poucos pesos e bastante espaço.
- **Formas:** raios 8/12/20/28; hero mais suave, listas e campos mais contidos.
- **Densidade:** confortável; seção pode existir sem cartão.
- **Superfícies:** fundo Mist, superfície elevada branca/Ink, destaque tonal;
  sombra apenas para elemento flutuante.
- **Navegação:** barra compacta e rail medium/expanded; Medir é prioritário
  pela hierarquia, não por um FAB desconectado.
- **Dashboard:** último peso como hero; tendência neutra; um CTA “Medir
  agora”; gráfico e meta como apoio.
- **Medição ao vivo:** valor central, status textual curto, instrução seguinte
  e ações periféricas. Sem pulsação obrigatória.
- **Histórico:** filtros compactos, resumo acima do gráfico e lista legível.
- **Movimento:** 150–250 ms em estado/expansão; imediato com efeitos reduzidos.
- **Telas grandes:** largura máxima e painéis de apoio; histórico lista-detalhe.
- **Acessibilidade:** maior contraste estrutural, 48 dp, estados textuais,
  reflow com fonte 200%.
- **Risco técnico:** baixo; Material 3 atual e Canvas existente resolvem a
  maior parte.

## B — Data Confidence

- **Personalidade:** analítica, organizada e técnica, porém acessível.
- **Princípios:** comparação visível; rótulos e unidades persistentes; gráfico
  e estatísticas lideram.
- **Paleta:** Aqua/Data mais dominante, neutros frios, Ocean para ação.
- **Tipografia:** escala mais compacta; números e labels próximos; peso 48/56.
- **Formas:** 8/12/16; superfícies mais retas e densas.
- **Densidade:** média/alta, com tiles de estatística.
- **Superfícies:** painéis outlined e divisores; pouca elevação.
- **Navegação:** raízes iguais; histórico e relatórios ganham presença visual.
- **Dashboard:** gráfico principal, último peso e estatísticas no mesmo bloco.
- **Medição ao vivo:** valor, intensidade RSSI/status e cronologia da sessão.
- **Histórico:** gráfico protagonista com filtros e comparação.
- **Movimento:** mínimo, focado em seleção e atualização de dados.
- **Telas grandes:** grade analítica e lista-detalhe.
- **Acessibilidade:** bom contraste, mas maior densidade aumenta risco com
  fonte 200% e carga cognitiva.
- **Risco técnico:** médio; exige mais layouts alternativos e pode aproximar o
  produto de um dashboard corporativo.

## C — Warm Progress

- **Personalidade:** acolhedora, otimista e doméstica.
- **Princípios:** contexto pessoal; progresso em linguagem suave; metas e
  perfis visíveis.
- **Paleta:** Sand/Warm como destaque, Ocean como ação e contraste.
- **Tipografia:** títulos expressivos, hero 52/60, texto conversacional.
- **Formas:** 16/24/28 e pílulas moderadas.
- **Densidade:** confortável, com mais respiro e blocos editoriais.
- **Superfícies:** tons quentes e containers destacados; sombra muito leve.
- **Navegação:** mesma estrutura, perfil ativo mais proeminente.
- **Dashboard:** saudação, progresso/meta e último peso em composição humana.
- **Medição ao vivo:** instruções acolhedoras e transições suaves.
- **Histórico:** resumo narrativo e gráfico secundário.
- **Movimento:** entradas curtas e progresso animado quando permitido.
- **Telas grandes:** conteúdo editorial + painel de dados.
- **Acessibilidade:** linguagem clara e alvos grandes; tons quentes exigem
  controle rigoroso de contraste.
- **Risco técnico:** médio; maior variedade de containers/movimento aumenta
  consistência e manutenção.

## Matriz de decisão

Escala 0–10. A contribuição é `nota × peso`; o total continua na escala 0–10.

| Critério | Peso | Calm Health | Data Confidence | Warm Progress |
| --- | ---: | ---: | ---: | ---: |
| Usabilidade e clareza | 25% | 9,4 | 8,7 | 9,0 |
| Hierarquia da informação | 20% | 9,2 | 9,5 | 8,8 |
| Acessibilidade | 20% | 9,5 | 8,4 | 9,0 |
| Consistência e escalabilidade | 15% | 9,3 | 8,8 | 8,6 |
| Adaptabilidade | 10% | 9,1 | 9,2 | 8,7 |
| Desempenho e simplicidade técnica | 10% | 9,6 | 8,6 | 8,5 |
| **Total ponderado** | **100%** | **9,36** | **8,88** | **8,80** |

## Decisão

**Calm Health vence com 9,36.**

Ela favorece a tarefa principal — medir e entender a evolução — com menor
carga cognitiva, melhor comportamento com fonte ampliada e menor risco sobre
BLE/persistência. “Data Confidence” informa muito bem, mas aproxima a tela
compacta de um painel denso. “Warm Progress” é acolhedora, porém pede mais
variação de superfície e movimento para sustentar a identidade.

A direção vencedora incorpora dois elementos das demais sem perder coerência:

- de Data Confidence: unidade/rótulo persistente e distinção explícita entre
  medição, tendência e média;
- de Warm Progress: perfil ativo legível e linguagem de meta neutra e humana.

## Contrato visual vencedor

1. Fundo base contínuo; nem toda seção recebe cartão.
2. Hero é reservado a último peso ou peso ao vivo.
3. Uma ação preenchida principal por viewport; ações secundárias são tonais,
   outlined ou textuais.
4. Cartões têm função: hero tonal, item outlined, alerta/status tonal ou
   superfície elevada transitória.
5. Tendência usa `trendUp`, `trendDown` e `trendNeutral` como direção
   matemática, sem semântica moral.
6. Compact tem 16 dp laterais; medium 24–32; expanded tem largura máxima de
   1200 dp e painéis.
7. Animações nunca carregam informação exclusiva.
8. Payload, endereço e detalhes técnicos permanecem somente em diagnóstico.

## Evidência Figma

Arquivo:
[ControlaPeso UI Exploration](https://www.figma.com/design/yI28NOZ0CgbnQ6HLXCR4sV).

Variáveis, estilos, Cover e Foundations estão presentes. O plano Starter
impõe um modo por coleção; `CP / Color Light` e `CP / Color Dark` preservam a
separação semântica.

Antes da criação dos componentes e frames comparativos, o conector passou a
retornar `INVALID_ARGUMENT` inclusive para scripts somente de leitura e
`get_metadata`. Assim, as três direções acima são especificações e matriz de
decisão versionadas, mas não se afirma que seus frames finais existem no
Figma. O escopo realmente criado está documentado em `DESIGN_SYSTEM.md`.
