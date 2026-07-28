# Arquitetura

## Visão geral

O Controla Peso usa MVVM pragmático, composição manual de dependências e
armazenamento local. As fronteiras importantes são:

```text
MainActivity
  ├── Activity Result APIs: BLE, notificações, Health Connect e SAF
  └── ControlaPesoApp
        ├── onboarding
        └── ControlaPesoNavHost
              ├── Composables sem lógica BLE
              └── ViewModels + StateFlow
                    ├── casos de uso
                    ├── repositories
                    ├── Room / DataStore
                    ├── exportadores
                    └── BleMeasurementSource
                           └── BleScanner + parsers
```

Não há Hilt/Koin, backend, SDK proprietário, serviço BLE, scan contínuo,
conexão GATT ou permissão de Internet.

## Composição e processo

`ControlaPesoApplication` cria `AppContainer` sob demanda e agenda:

- limpeza diária dos relatórios temporários;
- lembrete periódico somente quando habilitado;
- preferência global que autoriza logs BLE detalhados em debug.

`AppContainer` mantém somente `applicationContext` e expõe banco,
repositories, preferências, fonte BLE, exportadores, Health Connect e
agendador. Dependências com API mínima maior, como Health Connect, são
carregadas tardiamente.

`MainActivity` hospeda Compose, solicita permissões com Activity Result API,
compartilha `content://` e encaminha o destino de notificações. Ela não
interpreta bytes e não controla o scan.

## Camadas

### Bluetooth

- `BleScanner`: aquisição ampla de advertising, timeout total de 15 segundos,
  deduplicação temporária por endereço e cópia defensiva dos bytes.
- `OkOkAdvertisementParser`: parser puro do formato C0 observado.
- `ChipseaParser`: parser defensivo e separado para o formato conectado
  hipotético de 16+ bytes.
- `OkOkBleMeasurementSource`: adaptador entre o scanner existente e o fluxo de
  medição.
- `StableMeasurementDetector`: janela de leituras; emite no máximo um evento
  estável por sessão.
- `BleDiagnosticFormatter`: exporta captura técnica e mascara endereço.

O scanner e o parser funcional não foram substituídos. A interpretação `/100`
continua chamada de valor anunciado até a unidade ser confirmada no aparelho.

### Domínio

Os modelos não dependem de Room ou Compose:

- `Profile`;
- `WeightMeasurement`;
- `WeightGoal`;
- `ScaleDevice`;
- `WeightUnit`;
- `MeasurementSource`.

Casos de uso validam entrada manual, salvamento BLE, duplicidade, perfil,
estatísticas, IMC e metas. Peso persistido é sempre canônico em kg.

### Dados

Room schema v1 contém:

```text
Profile 1 ── * WeightMeasurement * ── 0..1 ScaleDevice
    │                    (SET_NULL ao esquecer balança)
    └── 1 ── * WeightGoal
          (CASCADE ao excluir perfil)
```

DAOs expõem `Flow`, consultas por intervalo e transações para perfil e meta
ativos. Não existe `fallbackToDestructiveMigration`.

DataStore guarda onboarding, tema, unidade padrão, opções de medição,
histórico, acessibilidade, logs debug, confirmação de unidade e lembretes.
Datas de medição são exibidas com o `zoneOffsetSeconds` gravado junto ao
registro, inclusive após mudança do fuso atual do telefone. Apenas dados
legados com offset inválido usam o fuso do aparelho como fallback.

### UI e navegação

As cinco áreas principais são Início, Histórico, Medir, Relatórios e Ajustes.
Rotas secundárias cobrem perfis, metas, detalhe/edição, balanças, diagnóstico,
privacidade e sobre.

- abaixo de 600 dp: `NavigationBar`;
- a partir de 600 dp: `NavigationRail`;
- dashboard usa painéis lado a lado a partir de 840 dp;
- histórico usa lista e detalhe lado a lado a partir de 840 dp, sem expor
  endereço ou payload no painel resumido;
- formulários usam largura fluida, `FlowRow` e listas preguiçosas;
- gráfico limita pontos, possui seleção por toque e resumo semântico.

Todas as telas recebem estado imutável e callbacks. APIs BLE não são chamadas
por Composables.

## Fluxo de medição BLE

```text
ação do usuário
  → valida suporte, Bluetooth e permissão
  → scan sem filtro: LOW_POWER → LOW_LATENCY
  → parser C0 aceita somente pacote compatível
  → BleWeightReading mantém unidade desconhecida
  → StableMeasurementDetector observa janela
  → usuário confirma unidade e perfil
  → SaveBleMeasurement verifica duplicidade
  → Room salva kg + Instant + offset + origem + payload
  → opcionalmente grava WeightRecord no Health Connect
```

`property 0x25` significa pacote de medição observado, não estabilidade. O
evento estável exige múltiplas leituras próximas durante tempo mínimo. Ao
salvar, a sessão para e não há conexão persistente.

## Duplicidade

Duas proteções são complementares:

1. o detector emite apenas uma estabilidade por sessão;
2. o repository procura peso, horário, perfil e, quando disponível, endereço
   próximos antes de inserir.

O usuário pode revisar e confirmar uma medição legítima parecida. IDs UUID
preservam identidade em backup. No Health Connect, o UUID vira
`clientRecordId` e `updatedAt` vira `clientRecordVersion`.

## Relatórios e arquivos

- CSV e PDF usam apenas dados selecionados do perfil.
- JSON schema v1 representa o backup completo exportável.
- substituição valida tudo, cria backup de segurança e altera o banco em
  transação;
- a leitura de importação é limitada a 20 Mi caracteres para impedir consumo
  de memória sem limite antes da validação;
- DataStore é restaurado depois da transação Room, pois não existe transação
  atômica entre tecnologias distintas;
- arquivos compartilháveis ficam em `cache/shared-reports`;
- `FileProvider` expõe somente esse subdiretório;
- o SAF grava a cópia no destino escolhido pelo usuário;
- WorkManager remove temporários antigos.

## Health Connect

A integração é opt-in por perfil e só escreve `WeightRecord`. O mapper envia:

- peso em kg;
- `Instant` e `ZoneOffset`;
- método manual ou automático;
- dispositivo do tipo balança, sem modelo;
- UUID e versão idempotente.

Não envia payload, endereço, observação, nome do perfil ou métricas ausentes.
Dados de demonstração e registros originados do Health Connect não são
reenviados.

Health Connect requer API 26. O app mantém `minSdk 24` usando carregamento
tardio e verificação de versão; em API 24–25 a integração aparece como não
suportada, sem afetar o restante.

## Lembretes

WorkManager agenda trabalho periódico aproximado. O worker relê as
preferências antes de notificar, verifica dia selecionado e permissão do
Android 13+, e abre a tela de medição sem iniciar Bluetooth. Desativar o
recurso cancela o trabalho único.

## Privacidade e release

- `allowBackup=false` e regras de backup excluem banco, preferências e
  arquivos;
- release nunca habilita o corpo detalhado do Logcat, mesmo que uma
  preferência antiga exista;
- modo demo só tem implementação navegável no source set debug;
- dados demo usam origem `DEMO`, são avisados no dashboard/histórico/relatório
  e podem ser removidos;
- compartilhamento, SAF e Health Connect dependem de ação explícita.

## Evolução

Qualquer mudança de schema exige migration e teste. Nova variante BLE exige
captura real, parser separado, payload preservado e teste dourado. GATT só deve
ser adicionado quando uma balança real demonstrar serviços, características e
handshake; nunca escrever em UUIDs por suposição.
