# Revisão de UI/UX — ControlaPeso

Documento de decisão para a próxima etapa de evolução da experiência do
ControlaPeso. Ele consolida a inspeção tela a tela realizada no aparelho,
as decisões aprovadas durante a conversa e o backlog necessário para
implementar as melhorias com segurança.

Este documento é a fonte de intenção de produto e UX. O inventário de rotas,
componentes e riscos técnicos continua em
[ui-ux/UI_INVENTORY.md](ui-ux/UI_INVENTORY.md); a direção visual escolhida
continua em [ui-ux/DESIGN_DIRECTIONS.md](ui-ux/DESIGN_DIRECTIONS.md); e os
resultados da rodada visual automatizada anterior continuam em
[ui-ux/VISUAL_REVIEW.md](ui-ux/VISUAL_REVIEW.md).

## 1. Como usar este documento em /goal

Ao iniciar o trabalho em modo /goal, cada meta deve apontar para uma seção
ou requisito deste documento. A implementação deve ser incremental e
verificável:

1. Ler as decisões da seção correspondente antes de alterar código.
2. Preservar os invariantes funcionais da seção 5.
3. Implementar uma fatia vertical pequena, incluindo estado vazio, estado
   preenchido, erro e acessibilidade quando aplicável.
4. Adicionar ou atualizar testes antes de passar para a próxima fatia.
5. Validar no aparelho com o estado de dados indicado na seção 4.
6. Marcar a meta como concluída somente quando os critérios de aceite e os
   testes estiverem atendidos.

Prioridades usadas no backlog:

- P0 — bloqueia compreensão, segurança, privacidade ou fluxo principal.
- P1 — melhoria estrutural necessária para a navegação e consistência.
- P2 — refinamento visual, conteúdo ou conveniência.
- P3 — melhoria opcional posterior.

Status usados neste documento:

- Aprovado — decisão de produto já tomada durante a revisão.
- Implementar — trabalho necessário para atingir a decisão.
- Validar — depende de teste físico, jurídico ou escolha explícita.
- Não alterar — comportamento técnico protegido.

## 2. Contexto e escopo da revisão

### 2.1 Rodadas executadas

- Rodada inicial: onboarding, Dashboard sem perfil, menu Registrar peso,
  medição Bluetooth bloqueada, formulário manual sem perfil, Histórico vazio,
  Relatórios, Ajustes, Perfis, Metas, Balanças conhecidas, Diagnóstico,
  Privacidade e Sobre.
- Rodada com dados: perfil de teste, uma medição simulada estável, uma meta,
  Health Connect autorizado somente para Peso e tema claro.
- O aparelho usado foi um Samsung SM-S908E conectado por ADB, Android 16,
  resolução 1080 × 2316 em retrato.
- A medição simulada foi marcada pela própria aplicação como Demonstração.
  Nenhuma medição física foi inventada ou tratada como evidência de protocolo.

### 2.2 Escopo

Esta revisão trata de:

- hierarquia, textos, estados, ações, componentes e navegação;
- primeira medição e confirmação da unidade kg/lb;
- perfil, meta e associação de pesagens;
- visualização de histórico, detalhe, edição e relatórios;
- centralização de Ajustes;
- transparência sobre Bluetooth, GitHub, compartilhamento externo e Health Connect;
- acessibilidade, temas, fontes ampliadas e telas estreitas;
- preparação de testes e implementação em metas posteriores.

Não faz parte desta revisão:

- redefinir o protocolo BLE sem evidência;
- alterar o payload bruto ou o parser funcional;
- criar analytics, backend, anúncios ou telemetria;
- decidir aconselhamento médico ou diagnóstico;
- aprovar juridicamente uma política de privacidade;
- inventar dados de balança para uma versão release.

## 3. Decisões executivas

### 3.1 Direção visual

Adotar a direção Calm Health, já registrada em
[DESIGN_DIRECTIONS.md](ui-ux/DESIGN_DIRECTIONS.md), incorporando:

- perfil ativo legível e humano;
- uma ação principal clara por viewport;
- peso ao vivo e última pesagem como heróis visuais;
- tendência neutra, sem linguagem de julgamento;
- unidades e rótulos persistentes;
- superfícies com função, sem cartão dentro de cartão sem necessidade;
- reflow em fonte ampliada, tema claro/escuro e contraste reforçado.

### 3.2 Regra global de ações

Botões não devem ocupar a tela inteira por padrão. Usar largura baseada no
conteúdo, ícone acompanhado de texto, alvo mínimo de toque de 48 dp e quebra
responsiva em telas estreitas ou fontes ampliadas.

Largura total fica reservada a uma decisão crítica única, quando a hierarquia
e a acessibilidade justificarem. Não repetir a mesma CTA no topo e dentro do
estado vazio.

### 3.3 Datas, horas e unidades

- Toda data visível ao usuário deve usar DD/MM/AAAA.
- Data e hora juntas devem usar DD/MM/AAAA · HH:mm.
- Hora permanece em HH:mm.
- O armazenamento interno, migração e JSON podem continuar usando o formato
  técnico estável, desde que a camada de apresentação seja uniforme.
- A unidade global escolhida em Ajustes deve ser reutilizada em Dashboard,
  Histórico, Relatórios e formulários.
- A unidade física da balança deve ser confirmada na primeira medição e só
  ser perguntada novamente por ação explícita de alteração.
- A entrada manual deve aceitar vírgula e ponto conforme o locale e
  normalizar internamente.

### 3.4 Perfil e pesagens sem perfil

O fluxo aprovado permite salvar uma pesagem sem perfil, identificada como
Sem perfil, e atribuí-la depois individualmente ou em lote. Hoje o modelo
exige profileId não nulo; a mudança exige migração Room não destrutiva e
revisão de histórico, estatísticas, relatórios, backup e Health Connect. A
migração foi implementada na versão 2 do Room, mantendo compatibilidade com
o schema anterior.

Nenhuma pesagem existente pode ser perdida ou atribuída silenciosamente a
uma pessoa diferente.

**Decisão registrada nesta etapa:** `profileId` é opcional. A tela de medição
permite salvar sem perfil, o Histórico oferece atribuição individual ou em
lote e a exclusão de um perfil desassocia suas pesagens sem apagá-las.

### 3.5 Avatar e foto

O perfil deve ter avatares ilustrados e a opção Usar foto. A foto fica no
armazenamento privado da aplicação, com troca e remoção explícitas. A
foto privada não entra no JSON de backup nem é enviada pelo aplicativo nesta versão.
O `avatarKey` do avatar ilustrado continua sendo um dado de perfil; restaurar
um backup não deve recriar ou baixar uma foto privada.

### 3.6 Integrações

- GitHub: consulta pública de release ao abrir, silenciosa em falha; não
  enviar perfil, peso, payload BLE ou conta.
- Compartilhamento externo: somente pelo Sharesheet Android, após ação manual;
  Google Drive pode aparecer como destino, mas não há OAuth nem Drive API no
  aplicativo.
- Health Connect: opcional, somente os campos autorizados no Android; a
  autorização não ativa gravações novas. A preferência por perfil começa
  desligada e só é ativada por ação explícita; o envio do histórico também
  exige ação e confirmação. A pessoa administra os registros também no Health
  Connect.
- Nenhuma integração deve parecer automática quando não for automática.

### 3.8 Métricas derivadas e tendência

O aplicativo não calcula nem exibe IMC ou outra métrica corporal derivada nesta
versão. Campos corporais que eventualmente já estejam no modelo, no payload ou
no backup permanecem preservados como dados recebidos; qualquer interpretação
futura exige protocolo validado e decisão de domínio separada.

O gráfico de tendência só aparece quando houver pelo menos duas medições
válidas no contexto selecionado. Uma única medição mostra o peso registrado e
orienta a pessoa a registrar novas medições, sem estatística artificial.

### 3.7 Demonstração

Dados simulados e o caminho de demonstração permanecem somente em builds de
desenvolvimento. O selo Demonstração deve ser inequívoco em debug e não pode
aparecer em builds release.

## 4. Estado de validação no aparelho

### 4.1 Dados locais criados para a revisão

| Item | Estado usado |
| --- | --- |
| Perfil | Teste_Debug |
| Unidade | kg |
| Altura | 170 cm |
| Pesagem | 72,1 kg, origem Demonstração, estável |
| Meta | 70,0 kg |
| Health Connect | Peso autorizado; histórico não sincronizado |
| Compartilhamento externo | Não usado; nenhum arquivo enviado |
| GitHub/APK | Nenhum download ou instalação iniciado |
| Tema | Tema claro testado e depois restaurado para Sistema |

O perfil, a pesagem e a meta são dados de teste locais e devem ser removidos
ao terminar a validação física. A autorização do Health Connect pode ser
revogada nas configurações do sistema se o aparelho voltar a um estado limpo.

### 4.2 Evidências

As capturas transitórias da rodada ficaram em /tmp durante a sessão. As
capturas versionadas da rodada visual anterior estão em
[ui-ux/screenshots](ui-ux/screenshots). Nomes úteis da rodada física:

- dashboard-demo-one e dashboard-demo-bottom;
- reports-demo-top e reports-demo-bottom;
- settings-demo-top, settings-demo-middle e settings-demo-final;
- profiles-demo3, goals-demo e goals-created2;
- devices-demo;
- privacy-demo;
- about-demo;
- live-profile-start;
- manual-profile-demo;
- health-consent2 e settings-health-after;
- dashboard-light-fake.

As capturas são evidência de UX, não contrato de pixels. O comportamento
deve ser validado com fontes, temas e tamanhos de tela diferentes.

## 5. Invariantes funcionais e de segurança

Estas regras não devem ser quebradas por uma melhoria visual:

- preservar Kotlin, Jetpack Compose e Material 3;
- não colocar lógica BLE em Composables;
- manter scanner, callbacks, timeout, liberação de recursos e parsers;
- preservar o payload bruto em qualquer fluxo de protocolo;
- não inventar unidade, estabilidade, campo ou detalhe de protocolo;
- tratar permissões Bluetooth conforme a versão do Android;
- não iniciar scan infinito nem scan Bluetooth em segundo plano;
- não calcular ou destacar métricas corporais derivadas sem protocolo validado
  e decisão de domínio; nesta versão IMC não é exibido;
- não usar fallback destrutivo em migração Room;
- não guardar tokens de serviços externos;
- não alterar dados locais em cancelamento, rede indisponível, JSON inválido,
  token revogado ou falha externa;
- não incluir demonstração em release;
- não tratar autorização de Health Connect como autorização de Drive;
- o Android sempre deve confirmar a instalação de APK;
- nenhuma cópia externa deve ser apagada ao excluir somente dados locais.

## 6. Matriz geral de estados

| Situação | Primeira informação | Ação principal | Ações secundárias | Regra |
| --- | --- | --- | --- | --- |
| Sem perfil e Bluetooth recusado | Crie ou selecione um perfil | Criar perfil | Permitir Bluetooth, adicionar manualmente | Não sugerir que a balança está pronta |
| Perfil sem pesagens | Perfil e unidade | Iniciar primeira medição | Adicionar manualmente | Orientar sem gráfico |
| Perfil com uma pesagem | Último peso | Medir novamente | Ver histórico, ver meta | Sem tendência estatística falsa |
| Perfil com histórico suficiente | Tendência e última pesagem | Medir novamente | Meta, histórico, relatório | Mostrar resumo e gráfico |
| Permissão Bluetooth negada | Permissão necessária | Permitir acesso | Abrir configurações, manual | Nunca dizer Bluetooth pronto |
| Bluetooth desligado | Ative o Bluetooth | Ativar Bluetooth | Manual | Atualizar ao voltar do sistema |
| Unidade não confirmada | Confira o visor físico | Escolher kg/lb | Alterar depois | Não iniciar busca |
| Busca não iniciada | Pronto para começar | Iniciar medição | Cancelar | Não mostrar “suba na balança” |
| Busca em andamento | Procurando balança | Cancelar busca | Ajuda contextual | Liberar callbacks ao parar |
| Falha de busca | Não encontramos a balança | Tentar novamente | Manual, Verificar conexão | Diagnóstico somente contextual |
| Pesagem sem perfil | Sem perfil | Atribuir depois | Criar/selecionar perfil | Nunca atribuir silenciosamente |
| Drive não configurado | Backup manual não configurado | Gerenciar backup | Privacidade | Nunca sugerir sincronização |
| Drive conectado | Conta/pasta conectada | Enviar backup manual | Listar/restaurar/excluir | Timestamp só após upload confirmado |
| Health Connect não autorizado | Integração opcional | Autorizar peso | Gerenciar permissões | Não pedir automaticamente |
| Health Connect autorizado | Peso autorizado | Gravar novas pesagens, se opt-in | Sincronizar histórico manual | Mostrar última sincronização |
| Nova versão | Atualização disponível | Abrir diálogo | Depois, Sobre | Instalação depende do Android |
| Dados de demonstração | Aviso de dados fake | — | Limpar demonstração | Somente debug |

## 7. Revisão tela a tela

### 7.1 Onboarding

**Observado**

- “Pular por enquanto” encerra todas as etapas e não há reabertura simples.
- O app não deve presumir que a pessoa possui OKOK.
- A escolha de kg/lb era pouco destacada na primeira pesagem.
- A autorização Bluetooth e a criação de perfil têm consequências diferentes.
- Health Connect, atualização GitHub e backup local ainda precisam de
  explicação curta e opcional.

**Decisões aprovadas**

- Renomear para “Configurar depois”.
- Em Ajustes, oferecer “Rever introdução” e “Refazer configuração”.
- Refazer a configuração deve preencher dados já existentes, sem duplicar
  perfil nem apagar pesagens.
- Mostrar cartão persistente no Dashboard quando a configuração estiver
  incompleta.
- Colocar título e opções grandes para a unidade física.
- Explicar que “Continuar” termina a orientação; não inicia medição.
- Explicar que o Drive é backup manual e que a atualização GitHub é opcional,
  com confirmação final do Android.
- Se Bluetooth for recusado, mostrar estado textual, nova tentativa e
  configurações; não esconder o modo manual.

**Critérios de aceite**

- Configurar depois deixa o app utilizável sem bloquear dados locais.
- Refazer introdução mantém perfil/pesagens/metas.
- Cada etapa tem uma ação principal e uma explicação do próximo passo.
- Permissão recusada, perfil criado e primeira pesagem têm estados distintos.
- A unidade é uma decisão visualmente evidente, não uma nota pequena.

### 7.2 Dashboard

**Observado sem dados**

- A medição Bluetooth aparecia como ação principal mesmo quando perfil ou
  permissão impediam a operação.
- O estado vazio repetia mensagens e não orientava a próxima decisão.

**Observado com Teste_Debug**

- Saudação “Boa tarde, Teste_Debug”.
- CTA “Medir com a balança”, ação manual e Histórico.
- Aviso de demonstração.
- Card azul Último peso com 72,1 kg, data longa e IMC 24,9.
- Gráfico muito alto mesmo com uma única medição.
- Média, variação e mínimo/máximo repetem informação para uma leitura.
- Meta fica abaixo do gráfico e fora do primeiro viewport.
- Avatar é apenas a inicial T.

**Decisão de composição**

1. Cabeçalho de perfil com avatar/foto, nome, unidade e Trocar perfil.
2. Próxima ação contextual: criar perfil, permitir Bluetooth, confirmar
   unidade ou iniciar medição.
3. Resumo compacto da última pesagem e da meta.
4. Área Status com somente pendências relevantes: Bluetooth, backup local,
   Health Connect e atualização.
5. Ações rápidas com ícone e texto: balança, manual, histórico e relatórios.
6. Gráfico abaixo do resumo; com menos de duas medições, usar estado “Ainda
   não há tendência” em vez de um gráfico vazio gigante.

**Critérios de aceite**

- Sem perfil, Criar perfil é a ação principal; Medir com a balança e o registro
  manual continuam disponíveis como ações secundárias para salvar sem perfil.
- Perfil sem pesagens não mostra gráfico nem estatística artificial.
- Uma pesagem mostra último peso e orientação para novas medições.
- Meta ativa aparece no primeiro viewport em formato compacto.
- Data é DD/MM/AAAA · HH:mm.
- O aviso de demonstração não existe em release.
- Status de Drive nunca usa “automático” ou “sincronizado” sem confirmação.
- IMC e outras métricas corporais derivadas não são exibidos nesta versão.

### 7.3 Menu Registrar peso

**Observado**

- Dois cartões funcionais: balança e manual.
- Diagnóstico Bluetooth aparece como botão técnico grande no fluxo principal.
- Com perfil ativo, o texto informa que o perfil será usado no salvamento.

**Decisão**

- Manter balança como PrimaryActionCard.
- Manter manual como SecondaryActionCard.
- Mostrar avatar/nome do perfil ativo com Trocar perfil.
- Mover diagnóstico para Ajustes > Ajuda e diagnóstico.
- Após falha, oferecer “Não encontrou sua balança? Verificar conexão”.

**Critérios de aceite**

- Usuário comum entende as duas formas de registrar sem conhecer OKOK.
- Diagnóstico não compete com medir/manual.
- A rota manual continua disponível quando Bluetooth está negado.

### 7.4 Medição com a balança

**Problema principal**

Antes de tocar em Iniciar medição, a tela já mostra “Aguardando alguém subir
na balança”, progresso 0 de 8 e aparência de leitura ativa. Isso contradiz
o botão e pode fazer a pessoa subir antes da hora.

**Fluxo proposto**

1. Perfil/destino: avatar, nome, Trocar perfil; ou opção explícita de medir
   sem perfil, se a migração de dados sem perfil estiver concluída.
2. Unidade física: título claro, visor físico e cards grandes kg/lb.
3. Permissão/Bluetooth: estado e CTA específicos.
4. Estado “Pronto para começar”: a busca ainda não começou.
5. Ao tocar em Iniciar medição: Procurando balança.
6. Depois da conexão: Agora suba na balança.
7. Depois da leitura: Mantenha-se parado / Peso estabilizado.
8. Salvar somente quando leitura, unidade e destino forem válidos.

**Estados obrigatórios**

- sem perfil;
- perfil selecionado;
- permissão negada ou permanentemente negada;
- Bluetooth desligado;
- Bluetooth pronto;
- unidade não confirmada;
- unidade confirmada;
- pronto ocioso;
- procurando;
- conectando;
- pronto para subir;
- leitura instável;
- leitura estável;
- timeout/sem dispositivo;
- leitura inválida;
- duplicidade;
- salvar sem perfil;
- salvo;
- cancelado.

**Critérios de aceite**

- Nenhum scan começa ao abrir a tela.
- “Aguardando alguém subir” só aparece depois de busca/conexão iniciadas.
- Um bloqueio sempre tem título, explicação e ação; cor não é o único sinal.
- Manual permanece acessível quando a balança falha.
- Cancelar e timeout liberam scanner/callbacks.
- A leitura inválida não modifica dados locais.
- O payload bruto continua preservado conforme o contrato BLE.

### 7.5 Adicionar peso manualmente e editar

**Observado**

- Com perfil, o formulário mostra Teste_Debug, Peso (kg), kg/lb, data
  29-07-2026, hora, observação e Salvar medição esticado.
- O subtítulo de edição dizia que o registro era fora da balança mesmo para
  uma origem de demonstração/BLE.

**Decisão**

- Cabeçalho contextual com avatar/nome e Trocar perfil.
- Subtítulo de edição: “Revise os dados desta medição”.
- Unidade com título e seleção destacada.
- Data localizada DD/MM/AAAA e seletor nativo.
- Hora HH:mm.
- Entrada decimal aceita vírgula/ponto.
- Salvar compacto, com ícone e texto, próximo ao formulário.
- Se sem perfil for permitido, usar “Salvar sem perfil” e atribuição posterior.

**Critérios de aceite**

- Data e ajuda exibem DD/MM/AAAA.
- Formulário não perde cursor, locale ou valor ao trocar unidade.
- Origem BLE, manual e demonstração não são confundidas.
- Ações Cancelar/Salvar refluem em fonte 200%.

### 7.6 Histórico

**Observado com uma medição**

- Muitos chips de filtros no topo.
- Gráfico alto com um único ponto.
- Aviso de demonstração.
- Data “29 de jul. de 2026 13:16”.
- Precisão 72,10 na leitura e 72,1 em alguns cards.
- Linha não mostra avatar/perfil.

**Decisão**

- Filtros compactos Período e Origem, mais ícone de filtros e contador.
- Resumo textual: “Últimos 30 dias · Todas as origens”.
- Filtros avançados em bottom sheet.
- Personalizado abre intervalo com DD/MM/AAAA.
- Uma medição mostra “1 medição registrada — tendência disponível após
  novas pesagens”, sem gráfico dominante.
- Lista mostra avatar/nome, peso, data/hora e origem como badge.
- Precisão de exibição uniforme.

**Critérios de aceite**

- Limpar filtros só aparece quando algo não padrão está selecionado.
- Filtro não é apenas cor; há rótulo e estado acessível.
- Histórico sem perfil oferece atribuição individual/lote.
- Gráfico e lista mantêm resumo textual equivalente.

### 7.7 Detalhe de medição

**Observado**

- Card com 72,1 kg, data longa, perfil, origem Demonstração, balança
  simulada e estabilidade.
- Ações Editar, Compartilhar e Excluir pequenas e sem ícones.

**Decisão aprovada**

- Editar: ícone de lápis, ação preenchida/primária.
- Compartilhar: ícone de share, outlined/tonal.
- Excluir: ícone de lixeira, tratamento de risco, confirmação e possível
  desfazer.
- Linha horizontal responsiva; em telas estreitas, quebra em duas linhas,
  sem esticar cada botão.
- Data DD/MM/AAAA · HH:mm.
- Avatar/nome e origem como elementos visuais.
- Medição sem perfil tem ação Atribuir perfil.

**Critérios de aceite**

- Texto permanece visível junto do ícone.
- Alvo mínimo e TalkBack funcionam em fonte ampliada.
- Excluir nunca é confundido com Editar/Compartilhar.
- Após ação, detalhe e histórico atualizam sem perder contexto.

### 7.8 Relatórios

**Observado com dados**

- Formato, perfil, período em muitos botões, unidade kg/lb e quatro switches.
- Gerar arquivo esticado.
- Backup, conexão Drive, importação e restauração ocupam a mesma tela.

**Decisão**

- Relatórios cuida de período, perfil, prévia e geração local.
- Drive e restauração vão para Ajustes > Dados e backup.
- Unidade vira preferência global em Ajustes; Relatórios mostra “Exibindo em
  kg · Alterar em Ajustes”.
- Formato e período viram seletores compactos.
- Conteúdo abre modal/bottom sheet com checkboxes e descrição:
  gráfico, tabela, observações e métricas disponíveis.
- Prévia informa quando uma leitura não permite tendência.
- O Dashboard exibe o estado contextual do backup manual; Relatórios exibe o
  mesmo estado no fim da prévia, somente leitura: não configurado, conectado
  ou último envio confirmado.

**Critérios de aceite**

- Nenhum controle Drive aparece na tela de relatórios além de um status/atalho
  secundário.
- Não existe timestamp de backup antes do upload confirmado.
- A geração PDF/CSV/JSON continua local e usa SAF/share existentes.
- Relatório não inclui métrica não validada só porque um switch foi marcado.

### 7.9 Ajustes

**Problema observado**

A página contínua mistura aparência, medição, histórico, lembretes,
integrações, diagnóstico, modo debug e links soltos. Switches altos e
seletores pequenos dificultam escaneamento.

**Decisão de arquitetura**

Landing com cards responsivos, duas colunas quando houver espaço e uma coluna
em telas estreitas/fonte ampliada:

1. Perfis e metas
2. Medição e balança
3. Aparência e acessibilidade
4. Histórico e relatórios
5. Lembretes
6. Integrações
7. Dados e backup
8. Privacidade e dados
9. Ajuda e diagnóstico
10. Sobre e atualizações

Cada card exibe ícone, título, resumo de estado e chevron. A tela de detalhe
tem AppBar com Voltar, descrição curta e controles agrupados.

**Controles**

- Escolhas mutuamente exclusivas: cards/segmented controls com ícone e texto.
- Booleanos: linha com ícone, título, descrição, “Ativado/Desativado” e
  switch; a linha inteira é alvo.
- Conteúdo de relatório: checkboxes em modal.
- Horário: TimePicker nativo/localizado.
- Ações destrutivas: no final, zona visual separada e confirmação.

**Critérios de aceite**

- A landing não contém todos os switches.
- O resumo do card muda após voltar da tela de detalhe.
- Fonte ampliada troca duas colunas por uma sem cortar texto.
- Debug não aparece em release.

### 7.10 Perfis

**Observado**

- Com Teste_Debug, cartão mostra inicial T, Perfil ativo, kg, altura e
  Editar/Excluir em texto.
- Criar perfil aparece como botão de largura total.
- Não há contagem de pesagens nem atribuição.

**Decisão**

- Avatar ilustrado ou foto com prévia.
- Nome, unidade, altura e última pesagem no cartão.
- Exibir quantidade de pesagens e ação Atribuir pesagens sem perfil quando
  houver registros.
- Criar perfil compacto com ícone.
- Editar com lápis; excluir separado com confirmação.
- Seleção de perfil ativo explícita quando houver mais de um.

**Critérios de aceite**

- Excluir perfil não exclui pesagens sem confirmação clara.
- Pesagens existentes nunca trocam de perfil silenciosamente.
- Foto é privada e pode ser removida.

### 7.11 Metas

**Observado vazio**

- Dois botões Criar meta, um no topo e outro no cartão.

**Observado preenchido**

- Meta 70,0 kg mostra barra, distância 2,1 kg, início/atual e ações Editar,
  Pausar, Concluir e Excluir, todas textuais e na mesma linha.
- O perfil não aparece claramente.

**Decisão**

- Uma única CTA quando não houver meta.
- Cabeçalho com avatar/nome e perfil da meta.
- Meta ativa: Editar como principal; Pausar/Concluir em menu secundário;
  Excluir com risco e confirmação.
- Mostrar percentual ou etapa acessível além da distância absoluta.
- Linguagem neutra, sem promessa ou recomendação clínica.

### 7.12 Balanças conhecidas

**Observado**

- Estado vazio tem explicação, Medir com a balança e Abrir diagnóstico
  Bluetooth em largura total.

**Decisão**

- Ilustração e passo a passo curto: permitir Bluetooth, iniciar, salvar.
- Diagnóstico vai para Ajuda e diagnóstico.
- Dispositivo salvo mostra nome/identificador permitido, última utilização e
  Remover com confirmação.
- Manter a informação de que não há busca em segundo plano como apoio.

### 7.13 Ajuda e diagnóstico Bluetooth

**Decisão**

Apresentar primeiro um estado amigável:

- Tudo certo;
- Permissão pendente;
- Bluetooth desligado;
- Nenhuma balança encontrada;
- Última verificação com falha.

A CTA contextual é Verificar conexão. Informações de parser, advertising,
GATT, UUID, RSSI e payload ficam em seção expansível Informações para suporte.
Detalhes técnicos continuam disponíveis para suporte e não são removidos.

### 7.14 Privacidade e dados

**Observado**

- Cartões de armazenamento local, permissões e Health Connect.
- Exportar antes, Limpar temporários e Excluir todos os dados locais em
  largura total.
- A exclusão parece uma ação positiva por usar o mesmo tratamento visual de
  outros botões.

**Decisão**

Separar a tela em:

1. Seus dados ficam no aparelho.
2. Compartilhamentos opcionais: GitHub, Drive e Health Connect com estado.
3. Seus controles: exportar, editar e excluir local.
4. Segurança e retenção.
5. Direitos e contato.
6. Zona de exclusão.

Explicar o alcance da exclusão local: perfis, pesagens, metas, preferências,
arquivos temporários e onboarding. Informar explicitamente que cópias no
Drive e registros no Health Connect não são apagados por essa ação.

O aviso atualmente exibe um modelo genérico identificado com controlador/
responsável, contato, versão `1.0` e data `31/07/2026`. A revisão jurídica
continua recomendada antes do release; o texto não promete conformidade
automática nem substitui a validação legal.

Referências oficiais para a etapa jurídica:

- [LGPD — Planalto](https://www.planalto.gov.br/ccivil_03/_ato2015-2018/2018/lei/l13709compilado.htm)
- [Direitos dos titulares — ANPD](https://www.gov.br/anpd/pt-br/assuntos/titular-de-dados-1/direito-dos-titulares)
- [Aviso de privacidade — ANPD](https://www.gov.br/anpd/pt-br/acesso-a-informacao/aviso-de-privacidade)

### 7.15 Sobre e atualizações

**Observado**

- Versão 1.0.0 (1) build debug, privado/local, protocolo, aviso não médico
  e licenças.
- Não há última consulta, status, changelog ou ação de atualização.

**Decisão**

Adicionar cartão Atualizações com:

- versão instalada;
- último status/horário da consulta;
- Verificar agora;
- Ver novidades;
- explicação de download opcional e confirmação final do Android.

Separar dados locais de integrações externas. Manter protocolo em detalhe
expansível e links compactos para changelog e licenças.

### 7.16 Health Connect

**Teste realizado**

O consentimento nativo permitiu selecionar somente Peso. Depois da concessão,
a tela passou a mostrar “Sincronizar o perfil ativo” ligado e “Sincronizar
histórico agora”.

**Ajustes necessários**

- Não interpretar autorização como envio automático.
- Renomear a preferência para algo explícito, por exemplo “Gravar novas
  pesagens no Health Connect”.
- Mostrar Autorizado · Peso, Revogado ou Não autorizado.
- Exibir última sincronização e erro.
- Antes de histórico, informar quantidade e período e pedir confirmação.
- Oferecer Gerenciar permissões abrindo Health Connect.
- Falhas não alteram dados locais.

### 7.17 Backup local e compartilhamento

O backup permanece em Ajustes > Dados e backup. O fluxo deve:

- oferecer frequência desativada, diária, semanal ou mensal de 30 dias;
- criar apenas uma cópia local mais recente no armazenamento privado;
- permitir criar agora e compartilhar o último backup pelo Sharesheet;
- manter importação, prévia, mesclar/substituir e exclusão confirmada;
- tratar cancelamento e JSON inválido sem alterar dados locais;
- deixar claro que Google Drive, se aparecer, é escolhido pelo Android e não é
  autenticado pelo aplicativo.

### 7.18 Atualizações GitHub e instalação

O diálogo global deve mostrar versão, data e corpo do release estável. “Depois”
fecha a sessão atual. “Baixar e instalar”:

- localiza apenas o asset universal;
- valida SHA-256, applicationId, assinatura compatível e versão superior;
- baixa para cache;
- orienta fontes desconhecidas quando necessário;
- abre PackageInstaller;
- deixa a confirmação final para o Android.

Falha de rede na consulta é silenciosa e não impede uso local.

## 8. Componentes transversais a criar ou padronizar

### 8.1 ProfileContextHeader

Avatar/foto, nome, unidade, estado ativo e Trocar perfil. Deve suportar Sem
perfil e não assumir que o primeiro perfil é o destino.

### 8.2 MeasurementUnitSelector

Título, descrição contextual, cards kg/lb, seleção semântica e acessível. Deve
ser reutilizado na primeira medição, formulário manual e perfil; em
Relatórios, a unidade é somente leitura e vem de Ajustes.

### 8.3 StatusCard

Ícone, título, descrição, estado textual e uma CTA. Não depender somente de
vermelho, verde ou azul. Estados incluem permissão, Bluetooth, Drive,
Health Connect, atualização e demonstração.

### 8.4 CompactActionGroup

Ícone + texto, min 48 dp, largura de conteúdo, quebra responsiva e separação
visual para risco. Usar para Editar/Compartilhar/Excluir, metas, perfis,
relatórios e backup.

### 8.5 DateDisplay e DatePicker

Uma única camada de apresentação para DD/MM/AAAA e DD/MM/AAAA · HH:mm.
Persistência, JSON e parser de data não devem ser alterados sem teste de
regressão.

### 8.6 EmptyState e ErrorState

Cada estado deve ter título, explicação, próxima ação, ícone e semântica de
acessibilidade. Evitar cartão vazio gigante e CTA duplicada.

### 8.7 DataOriginBadge

Origem BLE, manual e Demonstração. Demonstração é explícita somente em debug;
não esconder nem alterar a origem real de um registro.

### 8.8 ResponsiveScreen

Uma coluna em compact e fonte ampliada; duas colunas somente quando houver
espaço; largura máxima em expanded; navigation bar/rail conforme o inventário.
Validar fonte 200%, contraste reforçado, efeitos reduzidos e TalkBack.

## 9. Backlog recomendado para /goal

### Meta 0 — congelar decisões e contratos

**Prioridade:** P0.

- Registrar este documento como fonte de intenção.
- Registrar a decisão de não exibir métricas corporais derivadas sem protocolo
  validado.
- Registrar a aprovação das medições sem perfil e desenhar migração Room não
  destrutiva.
- Registrar que fotos privadas não entram no backup desta versão.
- Registrar que a gravação futura no Health Connect começa desligada e exige
  opt-in por perfil.
- Preencher controlador, contato/encarregado e versão/data da política.

**Aceite:** nenhuma decisão bloqueadora fica implícita no código.

### Meta 1 — fundações visuais e apresentação

**Prioridade:** P0/P1.

- Implementar tokens de ação compacta, status, origem, avatar e data.
- Centralizar DD/MM/AAAA e DD/MM/AAAA · HH:mm.
- Padronizar seleção kg/lb.
- Criar estados vazio/erro/loading acessíveis.
- Garantir fonte ampliada, tema claro/escuro e contraste.

**Testes:** previews e instrumentados para bounds, semântica e tema.

### Meta 2 — onboarding, perfil e destino da pesagem

**Prioridade:** P0.

- Configurar depois sem encerrar a possibilidade de revisão.
- Rever/refazer configuração em Ajustes.
- Perfil ativo com avatar/foto.
- Seleção de perfil na medição.
- Medição sem perfil e atribuição posterior, conforme a decisão registrada na
  Meta 0.

**Testes:** migração, criação/edição/exclusão, atribuição individual/lote e
onboarding preservando dados.

### Meta 3 — fluxo de medição orientado por estado

**Prioridade:** P0.

- Separar pronto, busca, conexão, subir na balança, estabilidade, erro e
  salvamento.
- Confirmar unidade somente quando necessário.
- Remover diagnóstico do menu principal.
- Permissão/Bluetooth com CTAs diretas.
- Manter manual como fallback.

**Testes:** JVM de transição; instrumentado para bloqueios e cancelamento;
físico para uma medição real assinada/protocolada.

### Meta 4 — Dashboard contextual

**Prioridade:** P1.

- Perfil no topo.
- Próxima ação por estado.
- Última pesagem e meta acima do gráfico.
- Estado de uma medição sem gráfico dominante.
- Status compactos de permissões, Drive e atualização.
- Manter o estado sem IMC e sem outras métricas derivadas.

**Aceite:** as matrizes das seções 6 e 7.2 são reproduzíveis no aparelho.

### Meta 5 — Histórico, detalhe e edição

**Prioridade:** P1.

- Filtros dropdown/bottom sheet.
- Data global.
- Histórico sem tendência para uma leitura.
- Avatar/origem na lista.
- Ações de detalhe com ícones e hierarquia.
- Formulário de edição localizado.

**Testes:** filtros, agrupamentos, data/fuso, compartilhamento, confirmação e
exclusão.

### Meta 6 — Relatórios e Dados e backup

**Prioridade:** P1.

- Separar geração local de Drive.
- Conteúdo em modal com checkboxes.
- Unidade global.
- Status de último backup confirmado.
- Fluxos Drive com cancelamento, revogação, rede e JSON inválido.

**Testes:** JVM de estados Drive/JSON; instrumentado de prévia e SAF; físico
com projeto Google Cloud configurado, sem versionar segredo.

### Meta 7 — Ajustes por sessões

**Prioridade:** P1.

- Landing de cards e telas de detalhe.
- Aparência, medição, histórico, lembretes, integrações, dados, privacidade,
  diagnóstico e atualizações separados.
- Switches contextualizados e TimePicker.
- Debug isolado.

**Aceite:** nenhum fluxo depende de lista longa para encontrar função
essencial.

### Meta 8 — Privacidade, Sobre e atualizações

**Prioridade:** P0/P1.

- Status de integrações.
- Política completa, direitos, contato e retenção.
- Cartão GitHub/changelog.
- Diálogo de release e instalação Android.
- Zona de exclusão clara.

**Aceite:** textos não prometem proteção absoluta, sincronização automática
ou exclusão externa que o app não executa.

### Meta 9 — acessibilidade e validação final

**Prioridade:** P0 antes do release.

- TalkBack e ordem de foco.
- Fonte 200%, compact/medium/expanded, tema claro/escuro, contraste,
  efeitos reduzidos.
- Navegação por teclado quando disponível.
- Teste físico BLE, Health Connect, Drive e atualização.

## 10. Plano de testes

### 10.1 JVM

- comparação e formatação de datas;
- locale de decimal e unidade;
- transições da medição ao vivo;
- permissão/Bluetooth/unidade/perfil;
- medição sem perfil e atribuição;
- seleção de gráfico com uma medição;
- estados de backup local e compartilhamento;
- parsing/validação de JSON;
- origem Demonstração e bloqueio em release;
- regressão de parsers BLE e payload bruto.

### 10.2 Instrumentado Compose

- onboarding Configurar depois/Rever introdução;
- Dashboard sem perfil, sem pesagens, uma pesagem, meta e erro;
- primeira unidade kg/lb;
- LiveMeasurement antes/depois de Iniciar;
- permissão negada/concedida e Bluetooth desligado;
- manual e edição em DD/MM/AAAA;
- filtros e detalhe;
- ações Editar/Compartilhar/Excluir;
- cards de Ajustes e retorno preservando estado;
- Health Connect cancelado/concedido/revogado;
- Drive cancelado e erro;
- diálogo de atualização claro/escuro;
- fontes ampliadas e bounds da navegação;
- caminho de instalação bloqueado por fontes desconhecidas.

### 10.3 Físico

1. Instalar debug limpo.
2. Completar onboarding com Bluetooth recusado.
3. Rever introdução e criar perfil.
4. Testar unidade física kg/lb.
5. Executar busca curta e medição BLE real.
6. Salvar, editar, compartilhar e excluir com confirmação.
7. Criar meta e verificar Dashboard.
8. Autorizar Health Connect somente para Peso, enviar histórico com prévia e
   revogar.
9. Configurar Drive, enviar/listar/restaurar/excluir backup.
10. Simular release futuro assinado, verificar diálogo, hash e confirmação
    Android.
11. Limpar dados de demonstração e verificar que release não contém a rota.

### 10.4 Validação técnica final

Executar, na raiz do projeto:

- ./gradlew test
- ./gradlew lint
- ./gradlew assembleDebug
- ./gradlew assembleRelease
- git diff --check

O resultado de cada comando deve ser registrado no handoff da meta.

## 11. Critérios gerais de pronto

Uma tela só pode ser considerada concluída quando:

- estados vazio, preenchido, carregando e erro estão cobertos;
- há uma ação principal clara;
- ações secundárias têm ícone/texto e alvo acessível;
- datas e unidades seguem as regras globais;
- tema claro/escuro, fonte ampliada e contraste foram verificados;
- TalkBack não depende de cor ou movimento;
- nenhuma integração externa ocorre sem consentimento/ação exigidos;
- não há regressão no scanner BLE, parser, payload, persistência ou
  permissões;
- testes relevantes passam;
- a tela foi conferida no aparelho com o estado da matriz correspondente.

## 12. Pendências que exigem decisão antes de implementação completa

1. Definir release estável, assinatura compatível e política de rollback do
   instalador.

## 13. Resultado da consolidação

Nenhum código foi alterado durante a rodada de revisão que originou este
documento. Foram alterados apenas o estado local de teste do aparelho e as
observações de produto registradas aqui:

- perfil Teste_Debug;
- pesagem Demonstração de 72,1 kg;
- meta de 70,0 kg;
- autorização Health Connect para Peso;
- teste temporário de tema claro, depois restaurado para Sistema.

O próximo trabalho autorizado deve usar este arquivo como contrato de UX,
começando pela Meta 0 e seguindo as metas na ordem de risco. A implementação
não deve apagar, reinterpretar ou mascarar os dados de teste até que a
validação física seja encerrada.
