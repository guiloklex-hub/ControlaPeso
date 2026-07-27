# Documentação do ControlaPeso

Este diretório concentra a documentação técnica e operacional do projeto.

## Estado atual

Em 27/07/2026, o aplicativo:

- detecta dispositivos Bluetooth Low Energy sem filtro de nome ou UUID;
- recebe medições da balança anunciada como `Yoda1` pelo próprio advertising
  BLE;
- reconhece o formato de Manufacturer Advertising observado no OKOK
  International;
- preserva os anúncios brutos e o histórico de mudanças;
- não pareia, não conecta por GATT e não escreve na balança;
- não calcula composição corporal;
- ainda não atribui uma unidade confirmada ao valor numérico recebido.

No teste físico com um Samsung `SM-S908E`, Android 16/API 36, o scan final
recebeu 258 resultados. A `Yoda1` foi identificada e transmitiu o valor bruto
`10380`, reproduzido como `103.8`, com unidade ainda não confirmada.

## Documentos

- [ARCHITECTURE.md](ARCHITECTURE.md): módulos, responsabilidades, estados,
  fluxo de dados, ciclo de vida e regras para evolução.
- [BLE_PROTOCOL_AND_CONNECTION.md](BLE_PROTOCOL_AND_CONNECTION.md): diferença
  entre advertising e GATT, estratégia de scan, permissões, protocolo
  observado, UUIDs conhecidos e desenho da conexão futura.
- [BLE_DIAGNOSTIC.md](BLE_DIAGNOSTIC.md): preparação do telefone, instalação,
  Logcat, roteiro de pesagem e informações que devem ser coletadas.

## Fonte de verdade

Quando houver divergência:

1. o código e os testes descrevem o comportamento executável atual;
2. `BLE_PROTOCOL_AND_CONNECTION.md` descreve fatos e hipóteses do protocolo;
3. `ARCHITECTURE.md` descreve os limites entre os componentes;
4. `BLE_DIAGNOSTIC.md` descreve o procedimento operacional.

Qualquer nova descoberta sobre o protocolo deve atualizar o código, os testes
e estes documentos no mesmo conjunto de alterações.

## Configuração preservada

| Item | Valor |
| --- | --- |
| Package e namespace | `br.com.paivalab.controlapeso` |
| Kotlin | `2.2.10` |
| Android Gradle Plugin | `9.3.1` |
| Gradle Wrapper | `9.5.0` |
| compileSdk | Android 36, minor API 1 |
| targetSdk | 36 |
| minSdk | 24 |
| Interface | Jetpack Compose e Material 3 |
| Compose BOM | `2026.02.01` |

`desugar_jdk_libs 2.0.3` está habilitado para permitir `java.time` no minSdk
24. Não existe framework de injeção de dependência, banco de dados, serviço em
segundo plano ou SDK proprietário.

## Regras permanentes

- Não colocar lógica Bluetooth em Composables.
- Nunca descartar o payload bruto ao adicionar uma interpretação.
- Não assumir que todo dispositivo OKOK usa os mesmos UUIDs ou o mesmo pacote.
- Não converter `weightValue` em `weightKg` antes de confirmar a unidade.
- Não chamar o valor secundário de impedância sem nova evidência.
- Não calcular gordura, água, músculo, metabolismo ou idade corporal.
- Manter scans limitados e liberar callbacks ao parar.
- Não iniciar conexão ou escrita automaticamente.
- Criar testes para cada novo parser ou variante.

