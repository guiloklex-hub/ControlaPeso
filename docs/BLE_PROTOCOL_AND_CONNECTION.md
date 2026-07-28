# Bluetooth, protocolo e conexão

## Escopo

Este documento registra o que está implementado, o que foi observado no
telefone físico e como uma conexão GATT poderá ser adicionada quando existir
evidência. Fatos e hipóteses são mantidos separados.

## Advertising não é conexão

A variante `Yoda1` observada envia o peso em pacotes de advertising BLE. O
telefone apenas escuta transmissões periódicas:

```text
balança ── advertising BLE ──► BluetoothLeScanner ──► aplicativo
```

Não há, nesse fluxo:

- pareamento;
- seleção no menu Bluetooth do Android;
- `BluetoothGatt.connect`;
- descoberta de serviços;
- escrita de característica;
- assinatura de notificação;
- sessão persistente entre telefone e balança.

Por isso os dados aparecem “automaticamente” no OKOK International. O
aplicativo inicia um scan, a pessoa ativa a balança ao subir e o advertising é
recebido enquanto ela transmite.

## Implementação atual do scan

`BleScanner` usa `BluetoothLeScanner` com:

- lista de filtros `null`;
- `CALLBACK_TYPE_ALL_MATCHES`;
- `reportDelay` igual a zero;
- duração total de 15 segundos;
- proteção contra scans simultâneos.

As fases são:

| Fase | Modo | Início | Término |
| --- | --- | --- | --- |
| Descoberta | `SCAN_MODE_LOW_POWER` | Ao tocar em iniciar | Ao detectar um anúncio OKOK ou completar dez segundos |
| Amostragem | `SCAN_MODE_LOW_LATENCY` | Após a descoberta | No prazo total de 15 segundos ou em uma parada antecipada |

A mudança de modo para e reinicia o callback BLE, mas não reinicia o relógio
total. Nenhum filtro por nome, endereço, UUID ou Manufacturer ID é usado.
Na tela de diagnóstico, **Executar teste de comunicação** é apenas um atalho
explícito para esse mesmo scan; não adiciona uma etapa GATT especulativa.

O scan é interrompido por:

- timeout;
- ação do usuário;
- Bluetooth desligado;
- perda de permissão;
- erro crítico;
- destruição do `ScannerViewModel`.

## Permissões e recursos

O Manifest declara BLE, Bluetooth e localização como recursos opcionais para o
aplicativo poder informar a ausência de suporte em vez de depender de filtro
da loja.

| Plataforma | Declaração/solicitação |
| --- | --- |
| Android 11 ou anterior | `BLUETOOTH`, `BLUETOOTH_ADMIN` e `ACCESS_FINE_LOCATION`, todos limitados a API 30 no Manifest |
| Android 12 ou superior | `BLUETOOTH_SCAN` e `BLUETOOTH_CONNECT` |

Não são declarados `BLUETOOTH_ADVERTISE`, internet ou localização em Android
12+.

### Decisão sobre `neverForLocation`

`BLUETOOTH_SCAN` usa:

```xml
android:usesPermissionFlags="neverForLocation"
```

Evidência no Samsung `SM-S908E`, Android 16/API 36:

1. sem a flag, o Android registrou cinco scans do ControlaPeso, todos com zero
   resultados;
2. o OKOK International tinha `BLUETOOTH_SCAN` concedida, localização negada e
   a flag `neverForLocation` no APK;
3. o OKOK recebeu 53 resultados durante a captura comparativa;
4. depois de adicionar a mesma flag ao ControlaPeso, a execução seguinte
   recebeu 258 resultados e detectou a `Yoda1`.

Na execução bem-sucedida:

- `LOW_POWER`: 24 resultados em aproximadamente 3,7 segundos;
- o anúncio OKOK antecipou a troca de fase;
- `LOW_LATENCY`: 234 resultados em aproximadamente 11,2 segundos.

Essa flag pode fazer o Android filtrar alguns tipos de beacon. Ela foi mantida
porque:

- o aplicativo não deriva localização;
- localização deve permanecer restrita a Android 11 ou anterior;
- a configuração foi necessária no aparelho testado;
- o advertising da `Yoda1` continuou visível.

Se outra variante for invisível, o compromisso deve ser reavaliado com logs e
um aparelho real, nunca por suposição.

## Dados capturados para cada dispositivo

Cada `BleDeviceResult` contém:

- nome anunciado;
- endereço entregue pelo Android;
- RSSI;
- timestamp da última detecção;
- UUIDs de serviço;
- Tx Power, quando presente;
- todos os Manufacturer IDs;
- todos os manufacturer data;
- todos os service data;
- bytes completos disponíveis em `ScanRecord`;
- sinais que motivaram a classificação Chipsea/OKOK;
- interpretação OKOK, quando o parser específico aceitar o pacote.

Arrays são copiados e apresentados em hexadecimal. Dispositivos não
reconhecidos continuam na lista.

## Identificação conservadora

Um dispositivo pode ser destacado como possível Chipsea/OKOK se houver ao
menos um destes sinais:

- nome contendo `Chipsea`;
- serviço FFF0 anunciado;
- Body Composition Service 181B anunciado;
- manufacturer ou service data com estrutura temporal plausível do pacote
  conectado de 16+ bytes;
- Manufacturer Advertising aceito pelo parser comprovado da `Yoda1`.

Esses sinais afetam apenas destaque e ordenação. Eles nunca funcionam como
filtro de exclusão.

## UUIDs conhecidos

Os UUIDs abaixo são referências conhecidas de variantes Chipsea e padrões
Bluetooth. Eles não foram observados no advertising da `Yoda1` e não comprovam
que ela ofereça uma conexão GATT equivalente.

| Uso conhecido | UUID |
| --- | --- |
| Serviço proprietário | `0000fff0-0000-1000-8000-00805f9b34fb` |
| Notificação proprietária | `0000fff1-0000-1000-8000-00805f9b34fb` |
| Escrita proprietária | `0000fff2-0000-1000-8000-00805f9b34fb` |
| Body Composition Service | `0000181b-0000-1000-8000-00805f9b34fb` |
| Body Composition Measurement | `00002a9c-0000-1000-8000-00805f9b34fb` |
| Battery Service | `0000180f-0000-1000-8000-00805f9b34fb` |
| Battery Level | `00002a19-0000-1000-8000-00805f9b34fb` |
| Client Characteristic Configuration | `00002902-0000-1000-8000-00805f9b34fb` |

## Evidência da variante `Yoda1`

### Captura comparativa do OKOK

Foram observados estes `ScanRecord`:

```text
10 FF C0 68 28 8C 13 88 00 00 25 00 00 00 00 00 00 06 09 59 6F 64 61 31
10 FF C0 69 00 00 00 00 00 00 24 00 00 00 00 00 00 06 09 59 6F 64 61 31
10 FF C0 69 28 8C 13 88 00 00 25 00 00 00 00 00 00 06 09 59 6F 64 61 31
```

O próprio diagnóstico do OKOK registrou, para `28 8C 13 88 ... 25`:

- protocolo OKOK;
- peso bruto `0x288C`;
- valor processado `103.8`;
- valor secundário bruto `0x1388`;
- campo secundário processado `500.0`;
- propriedade `0x25`;
- comando `1`.

Não foi observada abertura de conexão GATT pelo OKOK durante essa pesagem.

### Validação no ControlaPeso

Depois da correção de permissão, o ControlaPeso recebeu:

```text
10 FF C0 70 00 00 00 00 00 00 24 00 00 00 00 00 00 06 09 59 6F 64 61 31
10 FF C0 70 28 8C 13 88 00 00 25 00 00 00 00 00 00 06 09 59 6F 64 61 31
10 FF C0 71 28 8C 13 88 00 00 25 00 00 00 00 00 00 06 09 59 6F 64 61 31
```

Isso confirmou:

- nome anunciado `Yoda1`;
- transição de ocioso para medição;
- sequência avançando de `112` para `113`;
- repetição estável do valor bruto durante o scan;
- equivalência do parser local com o valor processado pelo OKOK.

O endereço observado deve ser mascarado em documentação pública, por exemplo
`80:F4:…:6E:5B`.

## Estrutura do advertising

O trecho principal do `ScanRecord` tem esta forma:

```text
10 FF | C0 70 | 28 8C 13 88 00 00 25 00 00 00 00 00 00
```

| Bytes | Interpretação |
| --- | --- |
| `10` | Tamanho da AD structure |
| `FF` | Tipo Manufacturer Specific Data |
| `C0` | Marcador observado do protocolo |
| `70` | Número de sequência no exemplo |
| `28 8C` | Peso bruto big-endian: `0x288C = 10380` |
| `13 88` | Valor secundário bruto big-endian: `0x1388 = 5000` |
| `00 00` | Desconhecido |
| `25` | Propriedade de medição observada |
| seis bytes finais | Desconhecidos na captura |

O Android interpreta os dois bytes depois de `FF` como Manufacturer ID
little-endian. Assim:

- `C0 70` vira `0x70C0`;
- `C0 71` vira `0x71C0`.

O Manufacturer ID muda porque o byte alto contém a sequência. Ele não é um ID
fixo de fabricante nessa variante.

Depois de remover o Manufacturer ID, o Android entrega 13 bytes de payload ao
parser:

| Offset no payload | Tamanho | Leitura atual | Confiança |
| --- | --- | --- | --- |
| 0–1 | 16 bits big-endian | `rawWeight`; divisão por 100 reproduz o número do OKOK | Confirmado para os exemplos |
| 2–3 | 16 bits big-endian | valor secundário; divisão por 10 reproduz `r1` do OKOK | Transformação confirmada, semântica desconhecida |
| 4–5 | 2 bytes | não interpretado | Desconhecido |
| 6 | 1 byte | `0x24` ocioso/comando 0; `0x25` medição/comando 1 | Observado |
| 7–12 | 6 bytes | não interpretado | Desconhecido |

### O que não está confirmado

- se `103.8` está em quilogramas, libras ou outra unidade;
- se `0x25` significa apenas medição ou também estabilidade;
- se `500.0` representa impedância;
- o significado dos bytes restantes;
- se outras balanças OKOK usam o marcador `C0`;
- se a escala decimal muda conforme a unidade configurada.

Por isso o modelo se chama `weightValue`, não `weightKg`.

## Parser de pacote conectado

`ChipseaParser` é separado do parser de advertising. Ele prepara uma leitura
defensiva para um formato conectado de pelo menos 16 bytes descrito em fontes
anteriores, mas ainda não observado na `Yoda1`:

| Bytes | Hipótese |
| --- | --- |
| 0–1 | flags, 16 bits little-endian |
| 2–3 | ano, 16 bits little-endian |
| 4 | mês |
| 5 | dia |
| 6 | hora |
| 7 | minuto |
| 8 | segundo |
| 9–10 | primeiro valor bruto de impedância |
| 11–12 | peso bruto |
| 13–14 | segundo valor bruto de impedância |
| 15 | propriedades |
| 16+ | bytes preservados sem interpretação |

O parser:

- valida o tamanho antes de acessar offsets;
- nunca converte `rawWeight` para uma unidade;
- valida data/hora com `LocalDateTime`;
- retorna campos nulos e notas quando algo é insuficiente;
- não infere estabilidade;
- preserva sempre o payload hexadecimal.

Ele não deve ser aplicado ao `ScanRecord` inteiro, pois offsets de AD structures
não equivalem aos offsets de um pacote recebido por característica GATT.

## Logcat

A tag única é:

```text
ControlaPesoBLE
```

Comando:

```bash
adb logcat -s ControlaPesoBLE:D
```

O log sempre pode registrar estados e erros mínimos. Campos detalhados,
endereço e payload são opt-in e somente em build debug; ative a opção em
**Ajustes > Diagnóstico**. Release bloqueia esse corpo por `BuildConfig.DEBUG`.
Antes de publicar um log, endereços Bluetooth devem ser mascarados de forma
consistente. Não devem ser incluídos tokens, credenciais ou logs amplos de
outros aplicativos.

A tela técnica oferece cópia do payload e exportação com endereço mascarado
por padrão. A versão atual do parser é `OKOK-C0-advertising/1`.

## Conexão GATT futura

### Estado atual

GATT não está implementado porque:

- a `Yoda1` já entrega a medição por advertising;
- a captura não mostrou `connectGatt` no OKOK;
- não há serviços ou características descobertos nessa balança;
- escrever em UUIDs supostos poderia alterar o comportamento do dispositivo.

### Condições para implementar

Uma variante conectada deve fornecer pelo menos uma destas evidências:

- serviços visíveis após conexão autorizada;
- UUIDs de características capturados;
- propriedades de leitura, escrita ou notificação;
- bytes de notificação correlacionados com o display;
- handshake ou escrita comprovados por captura.

### Componentes propostos

GATT deve ser adicionado fora de `BleScanner`:

```text
ScannerViewModel
    │ seleção explícita
    ▼
ScaleConnectionViewModel
    ▼
BleGattClient
    ├── GattConnectionState
    ├── fila serial de operações
    ├── descoberta de serviços
    ├── assinatura de notificações
    └── entrega de payload bruto ao parser adequado
```

Uma máquina de estados mínima deve distinguir:

```text
Disconnected
Connecting
DiscoveringServices
Ready
Subscribing
Listening
Disconnecting
Failed
```

### Fluxo seguro

1. O usuário seleciona um dispositivo encontrado.
2. O aplicativo para o scan.
3. Valida `BLUETOOTH_CONNECT` e Bluetooth ligado.
4. Abre uma única conexão usando `applicationContext`.
5. Aguarda o callback de conexão antes de descobrir serviços.
6. Registra todos os serviços, características, propriedades e descritores.
7. Só assina ou escreve em uma característica confirmada.
8. Serializa operações; não dispara leituras e escritas concorrentes.
9. Aplica timeout por etapa.
10. Copia e registra cada notificação em hexadecimal antes do parser.
11. Em erro, ação do usuário ou destruição do componente, chama `disconnect()`
    e `close()`.

Não deve haver conexão, reconexão, pareamento ou escrita automática.

### Assinatura de notificações

Se FFF1, 2A9C ou outra característica real tiver propriedade de notificação:

1. habilitar notificação local;
2. localizar o descritor CCCD real;
3. escrever o valor adequado apenas quando suportado;
4. aguardar confirmação da escrita;
5. iniciar interpretação somente após receber bytes;
6. preservar notificações inclusive quando o parser falhar.

FFF0/FFF1/FFF2 e 181B/2A9C são candidatos conhecidos, não uma exigência.

### Escrita e handshake

Nenhuma escrita deve ser criada por analogia com outro modelo. Para adicionar
um comando, a documentação precisa registrar:

- característica;
- propriedades;
- bytes exatos;
- endianess;
- momento da escrita;
- resposta ou mudança observada;
- captura que sustenta a decisão;
- teste unitário do encoding.

### Política de falhas

- Um erro GATT não deve apagar o último payload bruto.
- `SecurityException` deve virar estado explícito.
- Bluetooth desligado deve encerrar e fechar a conexão.
- Falha de descoberta não deve iniciar tentativa de escrita.
- Timeout deve fechar a instância antes de permitir nova tentativa.
- Não deve existir loop infinito de reconexão.

## Dados necessários para avançar

Para consolidar o advertising atual:

- unidade configurada no display;
- valor exato exibido em pelo menos três medições diferentes;
- histórico antes, durante, estabilizado e depois de sair;
- indicação visual do momento em que a balança considera o peso estável;
- medições com objeto de peso conhecido;
- capturas de outras variantes, se disponíveis.

Para iniciar GATT em outra variante:

- nome e endereço mascarado;
- serviços e características descobertos;
- propriedades de cada característica;
- descritores;
- notificações e leituras brutas;
- escritas reais observadas, se existirem;
- correlação temporal com display e ações do usuário.
