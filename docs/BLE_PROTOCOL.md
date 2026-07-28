# Protocolo BLE

Este é o resumo normativo da implementação. A evidência completa, payloads e
plano GATT estão em
[BLE_PROTOCOL_AND_CONNECTION.md](BLE_PROTOCOL_AND_CONNECTION.md).

## Campo confirmado

A balança `Yoda1` observada transmite por Manufacturer Advertising, sem
pareamento ou conexão GATT:

```text
C0 sequência | peso bruto BE | valor secundário BE | desconhecido | propriedade | desconhecido
```

- marcador observado: `0xC0`;
- `0x24`: anúncio ocioso observado;
- `0x25`: anúncio de medição observado;
- peso bruto: 16 bits big-endian;
- `/100` reproduz o número mostrado pelo diagnóstico do OKOK;
- unidade: ainda não confirmada;
- estabilidade: não é inferida de `0x25`;
- valor secundário: sem semântica confirmada.

Versão exposta do parser: `OKOK-C0-advertising/1`.

## Estabilidade

O detector usa uma janela mínima de oito leituras, duração mínima de dois
segundos, amplitude anunciada máxima de 0,10 e diferença final máxima de 0,05.
Pacote ocioso, zero ou intervalo superior a dois segundos reinicia a janela.
Uma sessão emite no máximo um evento estável.

Esses limites são conservadores e precisam ser verificados de novo com o visor
físico. Eles não alteram o parser e não transformam `0x25` em flag de
estabilidade.

## UUIDs de referência

| Uso | UUID |
|---|---|
| serviço proprietário | `0000fff0-0000-1000-8000-00805f9b34fb` |
| notificação proprietária | `0000fff1-0000-1000-8000-00805f9b34fb` |
| escrita proprietária | `0000fff2-0000-1000-8000-00805f9b34fb` |
| Body Composition Service | `0000181b-0000-1000-8000-00805f9b34fb` |
| Body Composition Measurement | `00002a9c-0000-1000-8000-00805f9b34fb` |
| Battery Service | `0000180f-0000-1000-8000-00805f9b34fb` |
| Battery Level | `00002a19-0000-1000-8000-00805f9b34fb` |
| CCCD | `00002902-0000-1000-8000-00805f9b34fb` |

Esses UUIDs não foram observados na `Yoda1` e não autorizam uma tentativa de
escrita.

## Parser Chipsea conectado

`ChipseaParser` aceita defensivamente 16+ bytes e lê little-endian:

- 0–1 flags;
- 2–3 ano;
- 4–8 mês a segundo;
- 9–10 valor bruto de impedância 1;
- 11–12 peso bruto;
- 13–14 valor bruto de impedância 2;
- 15 propriedades.

Ele nunca inventa unidade ou escala decimal, não infere estabilidade, retorna
resultado parcial para data inválida e preserva o hexadecimal.

## Regras permanentes

- scan inicial sem filtro por nome, UUID ou fabricante;
- dispositivo não reconhecido permanece visível;
- arrays sempre copiados e exibidos por `BleHexFormatter`;
- payload bruto salvo somente quando existe;
- composição corporal não é calculada;
- GATT permanece fora do escopo até haver evidência física.
