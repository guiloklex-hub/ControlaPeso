# Referências de UI/UX — ControlaPeso

Pesquisa registrada em 28/07/2026. As referências orientam princípios e
critérios; nenhum layout, texto, ilustração ou identidade de outro produto
será copiado.

## Fontes primárias

| Fonte | Padrão observado | Benefício para o ControlaPeso | Risco/limite |
| --- | --- | --- | --- |
| [Android — Build adaptive apps](https://developer.android.com/develop/ui/compose/build-adaptive-apps) | A interface responde à janela em runtime; navegação e quantidade de painéis mudam conforme espaço, orientação e multi-window. | Centralizar compact/medium/expanded e evitar bifurcações diferentes por tela. | Adicionar Material 3 Adaptive sem prova pode ampliar dependências; uma abstração local continua válida. |
| [Android — Build adaptive navigation](https://developer.android.com/develop/adaptive-apps/guides/build-adaptive-navigation) | Barra inferior em compact e rail em janelas maiores; a decisão vem do tamanho disponível, não do modelo do aparelho. | Preserva as cinco raízes atuais e reduz alcance/ocupa melhor tablets. | Rail não significa que todo conteúdo deva virar duas colunas. |
| [Android — Accessibility API defaults](https://developer.android.com/develop/ui/compose/accessibility/api-defaults) | Componentes Material trazem semântica de base, mas alvos interativos precisam de pelo menos 48 dp e ícones precisam de descrição contextual. | Favorece componentes Material reais e tokens de tamanho; reduz customização inacessível. | Semântica automática não substitui TalkBack e ordem de foco manuais. |
| [Android — Accessibility testing](https://developer.android.com/develop/ui/compose/accessibility/testing) | Testes Compose podem detectar contraste, alvo pequeno e ordem de travessia, combinados a testes manuais. | Cria um gate verificável para componentes novos. | A API/dependência precisa de prova compatível; alertas não substituem teste humano. |
| [Android — Support user-scalable content](https://developer.android.com/develop/ui/compose/accessibility/scalable-content) | Conteúdo ampliado deve refluír, evitando pan horizontal bidimensional; o font scaling do sistema é não linear. | Previews 1.5/2.0 e layouts que abraçam altura, em vez de cartões fixos. | Não criar zoom próprio para telas comuns; respeitar a configuração do sistema. |
| [Android — Compose Preview Screenshot Testing](https://developer.android.com/studio/preview/compose-screenshot-testing) | `@PreviewTest` produz referências e diffs host-side e aceita tema, largura e font scale. A ferramenta segue experimental. | Evidência determinística para as telas prioritárias sem depender de BLE/banco. | Exige plugin alfa; adotar só após prova sem atualizar AGP/Kotlin/Compose. |
| [Health Connect UI guidelines](https://developer.android.com/health-and-fitness/health-connect/ui/guidelines) | Consistência, transparência e clareza devem liderar a integração de dados sensíveis. | Ajustes e racional de permissão explicam benefício, dados e controle do usuário. | Não transformar Health Connect em CTA permanente no Dashboard. |
| [Health Connect — Permissions and data access](https://developer.android.com/health-and-fitness/health-connect/ui/permissions) | Ajustes devem expor sincronização e acesso; estados de acesso insuficiente precisam de orientação consistente. | Separar estado, ação e explicação na seção de integração. | Não sugerir sincronização concluída sem confirmação do estado funcional. |
| [Health Connect — Data display and attribution](https://developer.android.com/health-and-fitness/health-connect/ui/data) | Dados agregados precisam de origem clara e status de sincronização quando houver latência. | Preservar origem manual/BLE e diferenciar dado recebido de derivado. | O app hoje escreve peso; não inventar leitura/atribuição que o domínio não fornece. |

## Referências secundárias de produto

| Produto/fonte | Padrão geral extraído | Aplicação possível | Risco/limite |
| --- | --- | --- | --- |
| [Google Fit — Track your weight](https://support.google.com/fit/answer/9366735) | O peso abre em visão temporal; pontos representam medições e uma linha separada representa tendência/média. | Manter medição individual e resumo textual distintos da tendência; filtros ficam próximos do gráfico. | Não copiar navegação/identidade do Fit nem apresentar média como peso medido. |
| [RENPHO Health FAQ](https://renpho.com/pages/faq-for-renpho-health-app) | Medição começa com o app aberto; histórico, comparação e múltiplos usuários são tarefas explícitas. | Perfil ativo deve estar claro antes de medir; a sessão precisa dizer o que fazer e quando terminou. | A RENPHO calcula métricas BIA; o ControlaPeso não deve exibi-las sem protocolo validado. |
| [RENPHO scale manual](https://renpho.com/pages/001) | A instrução ao vivo conduz posição/tempo e separa conclusão na balança da transferência para o app. | Um estado textual por vez, valor em foco e próxima ação visível. | Tem pareamento e contas diferentes do produto atual; não importar esses fluxos. |
| [Withings Body guide](https://support.withings.com/hc/article_attachments/360012313517/Withings_Body_User_Guide_EN.pdf) | Peso aparece como widget de Dashboard; tendência e histórico detalhado ficam em contexto próprio. | Dashboard resume o último peso e abre histórico, sem tentar mostrar todas as métricas. | Não copiar widgets nem linguagem proprietária. |

Não foi encontrada documentação pública oficial suficientemente precisa para
derivar UI de Eufy Life ou OKOK International nesta pesquisa. A experiência
funcional observada no telefone e os contratos já implementados são mais
confiáveis do que imagens promocionais. Nenhuma hipótese de protocolo BLE foi
extraída dessas referências.

## Decisões resultantes

1. Último peso e medição ao vivo usam escalas tipográficas próprias, com
   números tabulares quando o suporte da fonte permitir.
2. Tendência usa linguagem e cor neutras; aumento ou redução não recebe
   julgamento automático.
3. Dashboard mostra um resumo e uma ação principal. Histórico assume filtros,
   gráfico e comparação.
4. Perfil ativo aparece antes da ação de medir, evitando salvar no contexto
   errado.
5. Estados BLE nunca dependem apenas de cor ou movimento.
6. Expanded usa lista-detalhe ou painel de apoio somente quando reduz
   navegação; não estica uma coluna para preencher a tela.
7. Componentes devem refluír com fonte 200%, manter 48 dp e preservar
   alternativas textuais para gráfico/status.
8. Integrações e dados sensíveis permanecem transparentes, locais por padrão e
   sem telemetria visual ou técnica.

