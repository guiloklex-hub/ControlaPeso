# Arquitetura do aplicativo

## Objetivo arquitetural

O ControlaPeso é, nesta etapa, uma ferramenta Android nativa de diagnóstico
BLE. A arquitetura mantém aquisição Bluetooth, interpretação de protocolo,
estado de tela e apresentação em componentes separados para que novas
variantes de balança possam ser investigadas sem inserir hipóteses na UI ou no
scanner.

O projeto usa uma organização MVVM simples:

```text
MainActivity
    │ ações e resultado de permissões
    ▼
ScannerViewModel ───────────────► ScannerUiState (StateFlow)
    │                                  │
    │ controla                          ▼
    ▼                             ScannerScreen
BleScanner                            Compose
    │
    ├── captura ScanResult
    ├── ChipseaUuids: sinais conservadores
    └── OkOkAdvertisementParser: variante Yoda1 comprovada
```

Não há camada de persistência, injeção de dependência ou serviço em segundo
plano. Os resultados vivem em memória enquanto o `ScannerViewModel` existir.

## Estrutura

```text
br.com.paivalab.controlapeso/
├── MainActivity.kt
├── bluetooth/
│   ├── BleScanner.kt
│   ├── BleScanState.kt
│   ├── BleDeviceResult.kt
│   ├── BleHexFormatter.kt
│   ├── BlePermissionHelper.kt
│   ├── ChipseaUuids.kt
│   ├── ChipseaParser.kt
│   ├── OkOkAdvertisement.kt
│   └── OkOkAdvertisementParser.kt
├── domain/
│   └── ScaleMeasurement.kt
└── ui/scanner/
    ├── ScannerScreen.kt
    ├── ScannerUiState.kt
    └── ScannerViewModel.kt
```

## Responsabilidades

| Componente | Responsabilidade | Não deve fazer |
| --- | --- | --- |
| `MainActivity` | Hospedar Compose, solicitar permissões pela Activity Result API e abrir configurações quando a negação for permanente | Executar scan ou interpretar bytes |
| `ScannerViewModel` | Orquestrar scanner, ambiente Bluetooth, histórico, ordenação e `ScannerUiState` | Acessar widgets ou interpretar o protocolo |
| `BleScanner` | Validar pré-condições, controlar o tempo, receber `ScanResult`, copiar dados e liberar o callback | Guardar `Activity`, renderizar UI ou conectar por GATT |
| `BlePermissionHelper` | Determinar permissões exigidas por versão e reconhecer negação permanente após uma solicitação | Solicitar permissões diretamente |
| `ChipseaUuids` | Manter UUIDs conhecidos e produzir sinais conservadores de identificação | Excluir dispositivos que não tenham esses sinais |
| `ChipseaParser` | Ler de forma defensiva o formato conectado de 16+ bytes descrito como hipótese | Ser chamado automaticamente para qualquer advertising |
| `OkOkAdvertisementParser` | Interpretar somente o Manufacturer Advertising comprovado na `Yoda1` | Inferir unidade, estabilidade ou composição corporal |
| `BleDeviceResult` | Transportar um snapshot completo e independente do anúncio | Manter referências mutáveis do Android |
| `ScannerScreen` | Renderizar estado e emitir ações do usuário | Chamar APIs Bluetooth |
| `ScaleMeasurement` | Modelo de domínio conservador para futura medição conectada | Forçar campos desconhecidos a valores inventados |

## Fluxo de inicialização

1. `MainActivity` obtém `ScannerViewModel` com `viewModels()`.
2. O `ViewModel` cria `BleScanner` usando `Application`, nunca uma `Activity`.
3. O `ViewModel` registra um `BroadcastReceiver` não exportado para
   `BluetoothAdapter.ACTION_STATE_CHANGED`.
4. `refreshEnvironment()` publica suporte BLE, energia do Bluetooth e
   permissões.
5. Compose coleta `ScannerUiState` e habilita ações conforme esse estado.
6. Em `onResume`, o ambiente é consultado novamente, permitindo refletir
   mudanças feitas nas configurações do Android.

## Fluxo do scan

1. O usuário toca em **Iniciar busca**.
2. O `ViewModel` limpa o erro atual, atualiza o ambiente e chama
   `BleScanner.start()`.
3. O scanner impede uma segunda busca simultânea e valida suporte, permissões,
   Bluetooth ligado e disponibilidade de `BluetoothLeScanner`.
4. A busca começa sem filtros em `LOW_POWER`.
5. Cada `ScanResult` é convertido em `BleDeviceResult`, com cópias dos arrays
   de bytes.
6. O parser do anúncio OKOK e as heurísticas Chipsea são executados fora da
   UI.
7. Ao reconhecer um anúncio OKOK, ou após dez segundos, o scanner reinicia o
   mesmo callback em `LOW_LATENCY`, preservando o prazo total de 15 segundos.
8. O `ViewModel` substitui o snapshot mais recente do endereço e guarda um
   novo item no histórico somente se o payload mudou.
9. `ScannerUiState` é publicado e a tela é recomposta.
10. Ao atingir o prazo, o callback é removido e o estado deixa de indicar scan.

## Modelos de estado

### Estado de baixo nível

`BleScanState` representa o scanner:

- `Idle`;
- `Scanning(secondsRemaining, phase)`;
- `Stopped(reason)`;
- `Failed(error)`.

As fases são `DISCOVERY` e `SAMPLING`. Os motivos de parada atuais são:

- ação manual;
- timeout;
- Bluetooth desligado;
- perda de permissão;
- destruição do `ViewModel`.

Os erros distinguem permissão ausente, Bluetooth inexistente, BLE não
suportado, Bluetooth desligado, scanner indisponível, `SecurityException`,
falha reportada pelo Android e falha inesperada por operação.

### Estado da tela

`ScannerUiState` contém:

- suporte a BLE;
- estado ligado/desligado;
- situação das permissões;
- scan e fase atuais;
- segundos restantes;
- lista do último snapshot de cada endereço;
- histórico de anúncios por endereço;
- erro atual.

O `ViewModel` é o único produtor desse estado. Composables recebem valores e
callbacks, sem dependência de classes Bluetooth.

## Identidade, duplicação e histórico

- A identidade temporária de um dispositivo é o endereço entregue pelo
  Android.
- `devicesByAddress` mantém apenas o resultado mais recente de cada endereço.
- RSSI, timestamp e conteúdo são atualizados quando o dispositivo reaparece.
- `historyByAddress` mantém no máximo 50 payloads distintos por endereço.
- O histórico compara primeiro `rawScanRecord`; se ele não existir, compara
  `manufacturerData` e `serviceData` pelo conteúdo dos arrays.
- A tela exibe no máximo os 12 registros mais recentes, mas o estado preserva
  até 50.
- Possíveis Chipsea/OKOK aparecem antes; os demais são ordenados por RSSI.

Endereços BLE podem ser aleatórios ou mudar entre sessões. Eles não devem ser
promovidos a identificador permanente de uma balança sem validação adicional.

## Propriedade dos dados

`ScanRecord`, manufacturer data e service data são copiados antes de entrar no
estado. Essa decisão evita que buffers pertencentes ao framework Android sejam
reutilizados ou alterados depois do callback.

Todos os bytes são formatados por `BleHexFormatter` no padrão:

```text
FF F0 02 00 34 12
```

Parsers devem sempre manter o hexadecimal original junto com suas notas de
interpretação.

## Permissões

`MainActivity` usa `RequestMultiplePermissions`. A decisão por versão pertence
a `BlePermissionHelper`:

- Android 12+: `BLUETOOTH_SCAN` e `BLUETOOTH_CONNECT`;
- Android 11 ou anterior: `ACCESS_FINE_LOCATION`.

Uma negação somente é classificada como permanente depois do retorno de uma
solicitação. Nesse estado, o botão abre a página do aplicativo nas
configurações, pois `shouldShowRequestPermissionRationale()` sozinho não
distingue a primeira execução de uma negação permanente.

A justificativa de `neverForLocation` está detalhada em
[BLE_PROTOCOL_AND_CONNECTION.md](BLE_PROTOCOL_AND_CONNECTION.md).

## Ciclo de vida e liberação

- O scanner guarda apenas `applicationContext`.
- Timers usam `Handler` no `Looper` principal.
- `stopScan()` é chamado no timeout, ação manual, erro crítico, Bluetooth
  desligado e perda de permissão.
- `onCleared()` remove o receiver e chama `BleScanner.close()`.
- `close()` interrompe um scan ativo, remove callbacks pendentes e libera a
  referência ao `BluetoothLeScanner`.
- Uma exceção ao parar é transformada em estado de erro; ela não é ocultada.

Não existe scan infinito, reconexão automática ou componente em background.

## Interface

`ScannerScreen` usa Compose e Material 3. Ela exibe:

- ambiente e permissões;
- progresso, fase e tempo restante;
- ações de solicitar, iniciar, parar e limpar;
- contador e Cards por dispositivo;
- identificação provável Chipsea/OKOK;
- campos técnicos e bytes selecionáveis;
- histórico expandido de mudanças;
- resultado interpretado da variante OKOK com aviso de unidade desconhecida.

Todos os textos de interface reutilizáveis ficam em `strings.xml`. O tema
existente oferece suporte aos modos claro e escuro.

## Testabilidade

Os parsers e o formatador são objetos puros, sem dependência de Android:

- `BleHexFormatterTest`: formatação hexadecimal;
- `ChipseaParserTest`: little-endian, tamanho, data inválida, pacotes de 16/20
  bytes e garantia contra exceções;
- `OkOkAdvertisementParserTest`: capturas reais, big-endian, sequência,
  propriedade, marcador e payloads inválidos.

O scanner recebe callbacks no construtor, reduzindo o acoplamento com o
`ViewModel`. Uma evolução futura pode extrair uma interface de scanner para
testes de orquestração, sem introduzir um framework de DI.

## Regras para evolução

Ao adicionar uma variante:

1. criar um parser separado e puro;
2. definir condições de entrada conservadoras;
3. preservar o payload original;
4. registrar hipóteses em `parserNotes`;
5. adicionar testes com capturas reais e casos inválidos;
6. só então integrar a detecção ao scanner;
7. atualizar a documentação do protocolo.

Ao adicionar GATT:

1. criar um componente próprio; não ampliar `BleScanner` para conexão;
2. expor uma máquina de estados explícita;
3. exigir seleção e ação do usuário;
4. parar o scan antes de conectar;
5. serializar operações GATT;
6. fechar `BluetoothGatt` em toda saída terminal;
7. preservar notificações e leituras brutas;
8. não implementar handshake, escrita ou UUID sem evidência.

O desenho proposto está em
[BLE_PROTOCOL_AND_CONNECTION.md](BLE_PROTOCOL_AND_CONNECTION.md).

