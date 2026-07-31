# Registro de implementação UI/UX

Atualizado em 31/07/2026. `docs/UI_UX_REVIEW.md` continua sendo a fonte de
intenção de produto; este arquivo registra o estado da implementação e não
substitui as decisões aprovadas.

## Meta 0 — auditoria do estado atual

| Decisão do contrato | Implementação atual | Situação |
| --- | --- | --- |
| Calm Health, tokens sem dependência nova | `ui/theme/Theme.kt`, `ui/designsystem/tokens/*`, `ui/designsystem/components/*` | `StatusCard`, `DataOriginBadge`, `MeasurementUnitSelector` e estados visuais adotados nas fatias revisadas |
| Navegação adaptável | `ui/navigation/ControlaPesoNavHost.kt`, `ControlaPesoWindowSize` | Implementada em compact/medium/expanded; validação física permanece pendente |
| Ações compactas com ícone e texto | `CompactAction`, `CompactActionGroup` e adoção em Dashboard, medição, perfis, metas, detalhe, relatórios, backup, Ajustes e estados com CTA | Ações principais adotadas; diálogos mantêm botões de confirmação localizados |
| Data visível `DD/MM/AAAA` e data-hora `DD/MM/AAAA · HH:mm` | `core/time/BrazilianDateFormatter.kt`, `BrazilianDateTimeFormatter.kt`, `MeasurementTimeFormatter.kt` e consumidores de UI | Implementada na camada de apresentação; validação física permanece pendente |
| Unidade global e confirmação BLE | `AppPreferences.defaultWeightUnit`, consumidores de Dashboard, Histórico, Relatórios, formulários e Perfis; `AppPreferences.confirmedBleUnit`, `BleMeasurementViewModel` | Unidade de apresentação centralizada; decisão física com visor continua pendente |
| Scanner/parser/payload bruto | `bluetooth/*`, `BleMeasurementViewModel` | Não alterado nesta etapa; regressões cobertas por testes existentes |
| Demonstração somente em debug | `src/debug`/`src/release` e `buildvariant/BleSourceFactory` | Implementação executável ausente do release; fábrica release indisponível |
| Backup local, GitHub opcional e confirmação do Android | `data/backup/*`, `worker/LocalBackup*`, `data/update/*`, `MainActivity`, `ui/update/*` | Implementação local; compartilhamento externo é manual pelo Sharesheet |
| Dados locais e migração não destrutiva | Room v2, `MIGRATION_1_2` e `data/backup/*` | Medições podem ficar sem perfil; payload bruto e histórico são preservados |

## Decisões realmente bloqueadoras

Estas são as únicas escolhas que bloqueiam as próximas alterações de domínio
ou a conclusão do produto. A fatia de apresentação de datas não depende delas.

1. **Release distribuível:** definir release estável, assinatura compatível e
   política de rollback do instalador antes de habilitar atualização externa.

Release assinado e política de rollback continuam pré-requisitos da distribuição,
mas não bloqueiam o backup local ou as fundações visuais.

## Estado de aceite por meta

| Meta | Estado atual | Evidência e limite |
| --- | --- | --- |
| 0 | Parcial | Contratos e aviso genérico identificado estão registrados; revisão jurídica e configuração de release continuam abertas. |
| 1 | Implementada localmente | Tokens, componentes, datas, estados e layouts adaptáveis têm cobertura JVM/Compose compilada; fonte 200%, contraste e TalkBack ainda precisam de aparelho. |
| 2 | Implementada localmente | `profileId` opcional, atribuição e migration v1→v2 têm regressões; onboarding e atribuição ainda precisam de validação física. |
| 3 | Implementada localmente | Fluxo BLE é finito, orientado por estado e mantém parser/payload; medição real e permissões dependem de aparelho autorizado. |
| 4 | Implementada localmente | Dashboard contextual e estado de tendência com uma medição têm cobertura instrumentada compilada; reprodução no aparelho e validação de acessibilidade permanecem pendentes. |
| 5 | Implementada localmente | Histórico, detalhe, edição, unidade e datas têm implementação/testes; fonte ampliada, TalkBack e fuso precisam de validação física. |
| 6 | Implementada localmente | Relatórios, backup local, compartilhamento e prévias têm estados e testes; SAF e destinos externos dependem do aparelho. |
| 7 | Implementada localmente | Landing e seções de Ajustes foram separadas; retorno, fonte ampliada e ordem de foco precisam de validação física. |
| 8 | Parcial | Privacidade, Sobre, GitHub e instalação Android estão implementados; revisão jurídica, assinatura e rollback ainda permanecem pendentes. |
| 9 | Não encerrada | `test`, `lint`, `assembleDebug`, release, compilação instrumentada e suíte conectada passaram no `SM-S908E`; BLE real, TalkBack, fonte 200% e instalação ainda exigem validação manual. |

## Primeira fatia segura concluída

- Centralizada a apresentação de datas em `BrazilianDateTimeFormatter`.
- `BrazilianDateFormatter` agora mascara com barras e mantém parsing tolerante
  ao separador antigo/ISO sem alterar o armazenamento técnico.
- Histórico, Dashboard, detalhe, scanner, dispositivos, compartilhamento, PDF,
  Drive e diálogo de atualização usam o formato global; agrupamento mensal não usa data
  localizada fora do contrato.
- Entrada manual continua mantendo dígitos no estado para preservar cursor e
  locale; a máscara visual é `DD/MM/AAAA`.
- Testes unitários cobrem ordem brasileira, data-hora com offset persistido,
  hora, compatibilidade de entrada e datas inválidas.
- Room v2 permite `profileId` nulo com migration não destrutiva; atribuição
  individual/em lote usa somente medições ainda não atribuídas.
- Entrada manual e BLE podem salvar sem perfil; o JSON continua carregando
  todas as colunas e o payload bruto.
- Relatórios não oferecem mais unidade própria: exibem a unidade global e
  levam a pessoa para Ajustes; ações de backup permanecem em Dados e backup.
- Ações importantes usam alvo mínimo de 48 dp, ícone com texto e semântica de
  botão; a landing de Ajustes refluí em uma ou duas colunas conforme espaço e
  escala de fonte.
- A autorização Drive só grava conta/pasta depois de uma operação remota
  confirmada; upload confirmado registra o último envio, e falhas de listagem
  após o upload não bloqueiam a confirmação do envio.
- A confirmação de upload Drive, conta, pasta e horário é persistida em uma
  única transação do DataStore depois da resposta remota; cancelamentos em
  Drive, GitHub, Health Connect e restauração JSON são propagados sem virar
  sucesso ou alterar o estado local.
- A medição BLE só persiste a balança depois da verificação de duplicidade;
  `SaveBleMeasurementInstrumentedTest` cobre a confirmação pendente sem criar
  dispositivo ou novo registro.
- O `BleMeasurementViewModel` fecha a fonte BLE ao ser destruído, liberando
  callbacks e recursos do scanner mesmo quando a tela sai durante uma busca.
- As ações pós-salvamento da pesagem ao vivo usam o componente compacto com
  ícone, texto e alvo mínimo acessível para perfil, nota, compartilhamento e
  exclusão.
- O contexto de perfil e unidade aparece no Dashboard, menu de Medir, medição
  BLE, entrada manual, Metas e detalhe; medições sem perfil usam rótulo
  explícito e cada tela oferece atalho para trocar de perfil.
- Metas mantêm editar e excluir como ações visíveis e movem pausar, concluir e
  reativar para um menu secundário compacto; o detalhe de medição exibe avatar
  e nome do perfil ou o estado sem perfil.
- A sincronização opcional com Health Connect mantém o último erro visível até
  uma nova tentativa bem-sucedida, distinguindo permissão ausente, serviço
  indisponível e falha de gravação sem alterar dados locais.
- Cancelamentos de onboarding, perfis, metas, atribuição de histórico, detalhe
  de medição, exclusão e integrações externas são propagados; erros ou
  cancelamentos não são convertidos em sucesso nem alteram dados locais.
- A tela Sobre mantém a última atualização validada em memória e oferece
  `Ver novidades` depois que o diálogo global for dispensado, sem repetir a
  consulta pública automaticamente.
- `StatusCard` agora concentra no Dashboard os estados textuais de Bluetooth,
  backup local, Health Connect, atualização pendente e demonstração de debug,
  sempre com uma ação contextual e sem iniciar integração automaticamente; o
  status Bluetooth leva a Ajustes, sem expor o diagnóstico técnico na tela
  principal.
- O seletor visual de kg/lb é compartilhado pela primeira medição, onboarding e
  registro manual e edição de perfil; a unidade global continua sendo a fonte
  exibida em Ajustes, Dashboard, histórico e relatórios, enquanto o perfil
  mantém sua unidade preferida no próprio formulário.
- A troca de unidade em onboarding e no formulário manual converte um valor já
  digitado para preservar a massa física, sem reinterpretar silenciosamente o
  mesmo número; entradas vazias ou inválidas permanecem inalteradas.
- A edição de metas e medições existentes também formata o valor armazenado em
  quilogramas na unidade atualmente selecionada, evitando reinterpretar um
  valor salvo ao alternar para lb.
- O teste instrumentado de restauração verifica que JSON truncado retorna erro
  antes de apagar ou alterar medições existentes, incluindo o payload bruto.
- Formato e período de relatório usam seletores compactos; o conteúdo PDF/CSV
  abre um modal com checkboxes e explicação, sem alterar dados locais.
- Payload, endereço e RSSI foram mantidos no diagnóstico; a tela de medição ao
  vivo não expõe mais o sinal técnico aproximado.
- Respostas inválidas do GitHub não substituem o último cache validado; o
  cancelamento de ações Drive é propagado sem limpar conexão local no meio da
  operação.
- `Configurar depois` no onboarding mantém um cartão persistente de
  configuração incompleta no Dashboard, com acesso direto a Ajustes e sem
  duplicar perfil ou medições.
- `FilterChip`s de perfil, unidade, período, fonte e preferências usam o token
  global de alvo mínimo de 48 dp, preservando acessibilidade em fontes
  ampliadas sem criar um tamanho local concorrente.
- Estados de carregamento do Dashboard, Histórico e Balanças oferecem `Tentar
  novamente` e recriam os fluxos de leitura depois de uma falha; a atribuição
  no Histórico oferece `Dispensar` para o erro sem duplicar a ação principal.
- A foto do perfil é propagada ao detalhe da medição; rótulos e avisos de
  demonstração em Histórico e relatórios ficam condicionados ao debug, sem
  alterar a origem bruta exportada e sem incluir a fonte executável no release.
- A foto local privada também é propagada aos contextos de Dashboard, medição
  ao vivo, registro manual e Metas; o backup continua sem copiar arquivos até
  a política de restauração de fotos ser aprovada.
- O `avatarKey` persistido agora é renderizado como avatar vetorial nos
  contextos de Dashboard, Histórico, detalhe, medição, Metas, Perfis e menu
  Registrar peso; a foto privada continua tendo precedência quando existe.
- Estados de erro e vazio revisados oferecem retry, dispensar ou voltar quando
  existe uma próxima ação segura; a ação usa o componente compacto com ícone,
  texto e semântica de acessibilidade, sem duplicar a ação principal da tela.
  Os títulos desses estados são marcados como cabeçalhos para leitores de tela,
  e os títulos de `SectionHeader` também expõem a semântica de heading nas
  seções compartilhadas, com regressão Compose em
  `SectionHeaderInstrumentedTest`,
  o erro de carregamento do Histórico tem título separado da explicação e a
  falha da medição ao vivo oferece `Dispensar` sem esconder as ações de retry.
  Diagnóstico, Drive, backup e relatório também mantêm títulos distintos das
  mensagens detalhadas.
- O schema Room v1 é empacotado como asset dos testes instrumentados para que a
  regressão da migration v1→v2 valide preservação de medição, payload bruto e
  desassociação após exclusão do perfil.
- O restore JSON verifica o cancelamento antes do commit e conclui o trecho
  pós-validação como uma unidade não cancelável, evitando estado parcial entre
  Room e DataStore.
- A sincronização manual do histórico no Health Connect agora exibe uma prévia
  com quantidade e intervalo `DD/MM/AAAA · HH:mm` antes do envio; medições já
  originadas no Health Connect e dados de demonstração ficam fora da prévia e
  do envio.
- O diagnóstico Bluetooth deixou de aparecer no Dashboard, no menu Registrar
  peso e em Balanças conhecidas; permanece em Ajustes > Ajuda e diagnóstico,
  conforme a decisão de não competir com as ações principais.
- A tela de Ajuda e diagnóstico agora começa por um estado amigável — pronto,
  permissão pendente, Bluetooth desligado, busca em andamento, falha ou
  nenhuma balança encontrada — e oferece a CTA contextual `Verificar conexão`
  sem iniciar busca ao abrir. O estado de busca concluída é mantido no
  `ScannerViewModel`; os detalhes de advertising, parser e payload bruto
  continuam em seção técnica expansível; a ação de busca não é repetida em
  botões gerais e os títulos de diagnóstico e balanças expõem heading para
  leitores de tela.
- Privacidade agora separa `Direitos e contato` e exibe o aviso genérico
  identificado com controlador/responsável, contato, versão `1.0` e data
  `31/07/2026`; a revisão jurídica continua indicada e nenhum valor clínico ou
  jurídico absoluto é prometido. O título da tela e os cards de privacidade
  também expõem headings.
- A exclusão local também remove o cache explícito de APKs em
  `cache/release-updates`, além das fotos privadas, relatórios temporários,
  Room, DataStore e lembretes; cópias remotas do Drive e registros do Health
  Connect permanecem fora do alcance dessa ação.
- A troca ou remoção de foto durante a edição de perfil agora usa uma operação
  transacional no armazenamento privado: falhas ou cancelamento preservam o
  arquivo anterior junto com o perfil existente.
- O Dashboard evita rótulos duplicados nas ações de status; Relatórios mantém
  a unidade global visível também para JSON; e a prévia do Health Connect
  separa contagem, intervalo e aviso de confirmação em nós acessíveis.

## Pendências desta etapa

- Validar a máscara e a leitura com fonte 200%, TalkBack e aparelho físico.
- Validar a escolha da unidade do perfil com fonte 200% e TalkBack.
- Confirmar a ordem de foco dos novos cartões, seletores e modal com TalkBack.
- Validar atribuição, foto privada e TalkBack com aparelho.
- Validar manualmente BLE, Health Connect, Drive, TalkBack, fonte 200% e ordem
  de foco no aparelho; a suíte instrumentada automatizada passou.

## Validação técnica desta etapa

Executado com JDK 17 temporário fora do repositório:

- Execução consolidada `./gradlew test lint assembleDebug
  :app:compileInstrumentedAndroidTestKotlin assembleRelease --no-daemon` —
  passou em 2m09s;
- `./gradlew test --no-daemon` — passou;
- `./gradlew lint --no-daemon` — passou;
- `./gradlew assembleDebug --no-daemon` — passou;
- `./gradlew assembleRelease --no-daemon` — passou;
- `./gradlew :app:compileInstrumentedAndroidTestKotlin --no-daemon` — passou;
- `git diff --check` — passou.

`connectedInstrumentedAndroidTest` foi executado no aparelho `SM-S908E - 16`:
41 testes passaram, 0 falharam e 0 foram ignorados em 1m16s. O Gradle exibiu
apenas o aviso de ambiente `androidx.test.services` sem UID ao tentar aplicar
`MANAGE_EXTERNAL_STORAGE`; isso não impediu a execução nem alterou o resultado.

O APK release universal gerado foi inspecionado em
`app/build/outputs/apk/release/app-universal-release-unsigned.apk`; a
implementação `DemoBleMeasurementSource`, a antiga `DemoBleSourceFactory` e o
metadata do arquivo demo não estão nos dex release. A fábrica genérica da
variante release permanece indisponível (`isAvailable = false`). O
identificador `source_demo` permanece apenas para compatibilidade de dados e
formatação de registros antigos; ele não habilita rota nem fonte simulada no
release. A instalação ainda depende de um APK assinado e de um
aparelho/emulador disponível.

O aparelho está autorizado para testes automatizados. A validação manual de
BLE real, permissões, fontes ampliadas, TalkBack, Health Connect, Drive e
instalação continua pendente; esses cenários não são provados pelos testes
Compose automatizados.

## Validação da continuação — 31/07/2026

- `./gradlew test --no-daemon` — passou após a regressão que garante o
  Health Connect desligado em perfis novos.
- `./gradlew lint assembleDebug --no-daemon` — passou.
- `./gradlew assembleRelease --no-daemon` — passou.
- `git diff --check` — passou.
- `./gradlew :app:connectedInstrumentedAndroidTest --no-daemon` — 43 testes
  passaram, 0 falharam e 0 foram ignorados no `SM-S908E - 16`.
- A mesma suíte passou com 43 testes em `font_scale=2.0` e novamente com 43
  testes em `font_scale=0.8`; a escala original do aparelho foi restaurada.
- A inspeção visual em `font_scale=2.0` confirmou que o cabeçalho de perfil
  refluí verticalmente sem quebrar nome, unidade ou `Trocar perfil`.
- A landing de Ajustes foi inspecionada em `font_scale=2.0`: os cards
  refluem para uma coluna e permanecem legíveis; a escala original `0.8` foi
  restaurada ao final.
- A inspeção física do APK atualizado confirmou `Abrir Ajustes` na ação
  contextual do cartão Bluetooth e manteve `Ajustes` apenas na navegação.

O ambiente exibiu o aviso `androidx.test.services` sem UID ao tentar aplicar
`MANAGE_EXTERNAL_STORAGE`; a suíte continuou e o resultado não foi afetado.
TalkBack, fonte 200%, BLE real, Health Connect, Drive e instalação continuam
dependentes de validação manual específica.

## Validação da continuação — largura compacta — 31/07/2026

- `ProfileContextHeader` usa reflow vertical abaixo de `360.dp`, além do
  reflow já aplicado para fonte ampliada.
- `./gradlew test --no-daemon` — passou.
- `./gradlew lint assembleDebug --no-daemon` — passou.
- `./gradlew assembleRelease --no-daemon` — passou.
- `./gradlew :app:connectedInstrumentedAndroidTest --no-daemon` — 44 testes
  passaram em `font_scale=0.8` e novamente em `font_scale=2.0` no
  `SM-S908E - 16`.
- O aparelho foi restaurado para `font_scale=0.8`; o aviso de
  `androidx.test.services` sem UID para `MANAGE_EXTERNAL_STORAGE` permaneceu
  apenas ambiental e não afetou os testes.

## Validação de semântica — 31/07/2026

- Cards clicáveis da landing de Ajustes agora mesclam título, descrição e
  chevron em um único alvo `Role.Button`.
- A árvore UI do aparelho expõe ações focáveis com rótulos textuais; o serviço
  TalkBack permanece desativado (`accessibility_enabled=0`) e não foi ativado
  para não alterar uma configuração global do aparelho.
- `./gradlew test lint assembleDebug --no-daemon` — passou.
- `./gradlew assembleRelease --no-daemon` — passou.
- `./gradlew :app:connectedInstrumentedAndroidTest --no-daemon` — 44 testes
  passaram em `font_scale=0.8` e novamente em `font_scale=2.0`.

## Validação de componentes clicáveis — 31/07/2026

- `MeasurementListItem` e cartões de ação mesclam conteúdo e ação em um único
  alvo `Role.Button`, evitando leitura fragmentada por tecnologias assistivas.
- `ProductComponentsInstrumentedTest` cobre clique e semântica dos dois
  componentes reutilizáveis.
- `./gradlew test lint assembleDebug --no-daemon` — passou.
- `./gradlew assembleRelease --no-daemon` — passou.
- `./gradlew :app:connectedInstrumentedAndroidTest --no-daemon` — 46 testes
  passaram em `font_scale=0.8` e novamente em `font_scale=2.0`.
- O aparelho foi restaurado para `font_scale=0.8`.

## Release e BLE físico — 31/07/2026

- `signingReport` mostrou `Config: none` para a variante release local; o APK
  release local não é candidato a distribuição sem o keystore oficial.
- O workflow exige os quatro secrets de assinatura, valida a tag e agora
  recusa sobrescrever uma release existente.
- A política de rollback foi definida como nova release assinada com
  `versionCode` maior; não há downgrade nem substituição de assets publicados.
- A busca BLE real terminou normalmente, encontrou 6 anúncios e exibiu 3
  dispositivos sem nome. Como nenhum foi identificado de forma suficiente,
  nenhuma unidade foi confirmada, peso foi lido ou medição foi salva.

## Mudança de arquitetura — backup local — 31/07/2026

- OAuth, Google Drive API, cliente Play Services Auth e telas de conexão/lista
  remota foram removidos do aplicativo.
- `LocalBackupService` cria `files/local-backups/controla-peso-backup-latest.json`;
  `LocalBackupWorker` agenda `OFF`, diário, semanal ou mensal de 30 dias.
- O compartilhamento do último JSON usa somente FileProvider/Sharesheet; um
  destino Google Drive pode aparecer pelo Android, mas não é autenticado pelo
  ControlaPeso.
- Importação, prévia, mesclar/substituir e payload bruto permanecem locais.
- A validação física criou o arquivo local e abriu o Sharesheet com destinos
  disponíveis; nenhum upload externo foi iniciado.

## Validação física Health Connect — 31/07/2026

- Ajustes > Integrações exibiu `Health Connect disponível` e
  `Autorizado · somente Peso`.
- A preferência `Gravar novas pesagens no Health Connect` apareceu como opt-in
  do perfil de teste; o histórico informou que ainda não foi enviado por este
  aplicativo.
- `Gerenciar permissões` abriu o gerenciador nativo do Android sem alterar
  nenhuma autorização; ao voltar, o app preservou o estado da integração.

## Validação física Dados e backup — 31/07/2026

- Ajustes > Dados e backup exibiu `Nenhum envio manual confirmado neste
  aparelho` e nenhuma timestamp de backup foi criada.
- O backup local permaneceu sem conexão externa e com compartilhamento explícito
  pelo Sharesheet.
- Importação informa que o JSON inteiro será validado antes de qualquer
  alteração e que fotos privadas ficam fora da restauração.
- Não há OAuth ou Drive API no fluxo atual; um destino Google Drive, se
  instalado, é escolhido pelo Sharesheet Android.

## Validação física Diagnóstico BLE — 31/07/2026

- Ajustes > Diagnóstico > Ajuda abriu em `Pronto para verificar`, com
  `Busca parada` e zero dispositivos, sem iniciar scan automaticamente.
- `Verificar conexão` permaneceu como ação explícita para iniciar a busca de
  15 segundos.
- `Exibir informações para suporte` expandiu a versão do parser
  `OKOK-C0-advertising/1` e a descrição técnica sem alterar o payload ou iniciar
  GATT.

## Atualização do aviso de privacidade — 31/07/2026

- O placeholder de identificação foi substituído por um aviso genérico com
  controlador/responsável informado, contato para direitos e encarregado,
  versão `1.0` e data de publicação `31/07/2026`.
- A tela mantém a ressalva de revisão jurídica; nenhum texto promete
  conformidade automática.
- `./gradlew test lint assembleDebug --no-daemon` — passou.
- `./gradlew assembleRelease --no-daemon` — passou.
- `./gradlew :app:connectedInstrumentedAndroidTest --no-daemon` — 47 testes
  passaram no `SM-S908E - 16`.
- A mesma suíte passou em `font_scale=2.0`, confirmando que o aviso longo de
  privacidade permanece rolável e acessível; a escala foi restaurada para
  `0.8`.

## Auditoria release estável e BLE — 31/07/2026

- O GitHub possui os quatro secrets de assinatura configurados e o workflow
  `Publish Android release` da tag `v1.0.0` concluiu com sucesso.
- A release pública `v1.0.0` possui assinatura v2 RSA 4096; o certificado
  público SHA-256 é o já publicado no checklist. O keystore privado não foi
  baixado nem exposto.
- O build release local continua `Config: none`; a assinatura de distribuição
  deve ocorrer somente no workflow com os secrets protegidos.
- A política de rollback exige nova versão, `versionCode` maior, mesma chave e
  assets imutáveis; o workflow local recusa sobrescrever uma tag existente.
- Nova busca BLE física encontrou 10 anúncios, mas nenhum dispositivo
  identificável como `Yoda1` apareceu nesta rodada. Nenhuma unidade, peso ou
  medição foi confirmada/salva; a medição correta do Yoda1 relatada antes
  permanece evidência anterior, não foi repetida agora.

## Tentativa de validação TalkBack — 31/07/2026

- O serviço Samsung TalkBack foi ativado temporariamente e conectou com
  `requestTouchExplorationMode=true`.
- O Android abriu o tutorial inicial `Conheça o TalkBack` antes de permitir a
  inspeção falada do Controla Peso; nenhuma conclusão sobre a leitura por voz
  foi inferida a partir dessa tentativa.
- A configuração original foi restaurada: `accessibility_enabled=0` e chave
  de serviços removida; a Activity principal voltou a estar em primeiro plano.

## Validação de foco em perfis — 31/07/2026

- O cartão de perfil deixou de ser um botão pai concorrente com `Editar`,
  `Excluir`, `Tornar ativo` e atribuição; as ações explícitas permanecem os
  únicos alvos clicáveis.
- `ProfilesScreenInstrumentedTest` cobre a ação `Editar` como botão acessível.
- `./gradlew test lint assembleDebug --no-daemon` — passou.
- `./gradlew assembleRelease --no-daemon` — passou.
- `./gradlew :app:connectedInstrumentedAndroidTest --no-daemon` — 47 testes
  passaram em `font_scale=0.8` e novamente em `font_scale=2.0`.
- O aparelho foi restaurado para `font_scale=0.8`.
