# Diagnóstico Bluetooth LE

Documentos relacionados:

- [Arquitetura do aplicativo](ARCHITECTURE.md)
- [Bluetooth, protocolo e conexão](BLE_PROTOCOL_AND_CONNECTION.md)
- [Índice da documentação](README.md)

## Objetivo

Esta fase serve para descobrir como a balança anuncia sua presença e quais
dados Bluetooth Low Energy ficam visíveis sem uma conexão GATT. O aplicativo
faz uma busca ampla de 15 segundos, sem filtro de nome ou UUID, e preserva
UUIDs, manufacturer data, service data e o `ScanRecord` bruto.

A busca começa em `LOW_POWER`, modo que encontrou a balança nos testes com o
OKOK International, e muda para `LOW_LATENCY` nos cinco segundos finais ou
assim que reconhece um anúncio OKOK. Nenhum dispositivo é pareado ou conectado.

O aplicativo interpreta apenas o formato de anúncio efetivamente observado na
balança anunciada como `Yoda1`. A interpretação continua separada do scanner,
preserva o payload original e não calcula métricas corporais.

## Permissões

- Android 12 ou superior: conceda **Dispositivos próximos**. O aplicativo
  solicita `BLUETOOTH_SCAN` e `BLUETOOTH_CONNECT`.
- Android 11 ou anterior: conceda **Localização durante o uso**. O scan BLE
  nessas versões depende de `ACCESS_FINE_LOCATION`; alguns aparelhos também
  exigem que a localização do sistema esteja ligada.
- Se a permissão for negada permanentemente, o botão **Solicitar permissões**
  abre a tela do aplicativo nas configurações do Android. Ative a permissão
  ali e retorne ao Controla Peso.

`BLUETOOTH_SCAN` usa `usesPermissionFlags="neverForLocation"`. O aplicativo não
deriva localização e, no Samsung/API 36 usado nos testes, o scan sem essa flag
era registrado normalmente pelo Android, mas entregava zero resultados. O APK
do OKOK International usa a mesma flag e recebeu os anúncios da `Yoda1`.

Essa declaração pode fazer o Android filtrar alguns tipos de beacon BLE. A
decisão favorece o funcionamento comprovado com a balança observada e mantém
`ACCESS_FINE_LOCATION` restrita ao Android 11 ou anterior, como exige a política
de permissões desta fase. Se outra variante continuar invisível, esse
compromisso deverá ser reavaliado com evidência no aparelho correspondente.

## Preparar um telefone físico

1. No telefone, abra **Configurações > Sobre o telefone**.
2. Toque sete vezes em **Número da versão** até ativar o modo de desenvolvedor.
3. Abra **Opções do desenvolvedor** e habilite **Depuração USB**.
4. Conecte o telefone ao computador por um cabo USB com dados.
5. Aceite no telefone a autorização da chave RSA do computador.
6. No computador, confirme a conexão:

   ```bash
   adb devices
   ```

O emulador normalmente não reproduz os anúncios da balança. Use um telefone
com Bluetooth LE.

## Compilar, instalar e abrir

Na raiz do projeto:

```bash
./gradlew assembleDebug
adb install -r app/build/outputs/apk/debug/app-debug.apk
adb shell am start -n br.com.paivalab.controlapeso/.MainActivity
```

Também é possível selecionar o telefone no Android Studio e executar a
configuração `app`.

## Acompanhar os logs

O scanner usa a tag `ControlaPesoBLE`. Estados e erros mínimos permanecem
disponíveis; endereço e payload completos só entram no Logcat em build debug
depois de ativar **Ajustes > Diagnóstico > Logs BLE detalhados**:

```bash
adb logcat -s ControlaPesoBLE:D
```

Os Cards permitem expandir e copiar payloads. A tela também copia ou
compartilha o diagnóstico inteiro e mascara os bytes centrais dos endereços
por padrão. A versão atual exibida é `OKOK-C0-advertising/1`.

O botão **Executar teste de comunicação** executa o mesmo scan amplo e finito
de 15 segundos. Ele valida somente recepção de advertising: não abre GATT, não
consulta serviços/características e não escreve na balança. O diagnóstico
explica essa diferença para evitar que um anúncio recebido seja interpretado
como conexão permanente.

## Procedimento de teste

Antes de começar, conceda as permissões e confirme que o Bluetooth está
ligado.

1. Feche completamente o aplicativo OKOK International para evitar que ele se
   mantenha um scan concorrente durante a captura.
2. Abra o Controla Peso.
3. Toque em **Iniciar busca**.
4. Suba na balança.
5. Espere o peso estabilizar.
6. Saia da balança.
7. Expanda os dados técnicos e salve os payloads exibidos.

Se a balança não aparecer, repita a busca e suba nela imediatamente após tocar
no botão.

## Evidência observada com o OKOK International

O log Bluetooth do Android confirmou que o OKOK recebeu anúncios BLE da
balança `Yoda1` sem abrir uma conexão GATT. Entre os registros capturados
estavam:

```text
10 FF C0 68 28 8C 13 88 00 00 25 00 00 00 00 00 00 06 09 59 6F 64 61 31
10 FF C0 69 00 00 00 00 00 00 24 00 00 00 00 00 00 06 09 59 6F 64 61 31
10 FF C0 69 28 8C 13 88 00 00 25 00 00 00 00 00 00 06 09 59 6F 64 61 31
```

No `ScanRecord`, `10 FF` é o tamanho e o tipo da estrutura de Manufacturer
Advertising. O Android entrega os bytes seguintes como um Manufacturer ID
little-endian: `C0 68` aparece como `0x68C0` e `C0 69` como `0x69C0`. Portanto,
esse ID varia com a sequência e não pode ser tratado como um identificador
fixo de fabricante.

Na variante observada:

- `C0` é o marcador do formato;
- o byte seguinte é o número de sequência;
- os dois primeiros bytes do payload são um peso bruto big-endian;
- dividir esse peso bruto por 100 reproduziu o valor mostrado no log interno
  do OKOK, mas a unidade ainda não está confirmada;
- os dois bytes seguintes formam outro valor big-endian; dividir por 10
  reproduziu o campo `r1` do OKOK, cuja unidade e semântica são desconhecidas;
- o byte de propriedade `24` foi observado no anúncio ocioso e `25` durante a
  medição.

Essas regras são aplicadas somente quando o marcador, o tamanho mínimo e a
propriedade correspondem à captura. Os valores bruto e transformado, as notas
da hipótese e o anúncio original ficam visíveis no Card do dispositivo.

## Validação física do ControlaPeso

Em 27/07/2026, depois de alinhar a declaração `neverForLocation` com o APK do
OKOK, o ControlaPeso recebeu 258 resultados em um scan no Samsung `SM-S908E`,
Android 16/API 36:

- 24 resultados em aproximadamente 3,7 segundos de `LOW_POWER`;
- detecção da `Yoda1` e mudança antecipada para amostragem;
- 234 resultados em aproximadamente 11,2 segundos de `LOW_LATENCY`;
- advertising ocioso com sequência `112`, peso bruto zero e propriedade
  `0x24`;
- advertising de medição com sequências `112` e `113`, peso bruto `10380`,
  valor reproduzido `103.8`, valor secundário bruto `5000` e propriedade
  `0x25`.

Essa execução validou o scanner e o parser do advertising. Ela não confirmou a
unidade do peso, estabilidade, impedância ou composição corporal. A análise
completa está em
[BLE_PROTOCOL_AND_CONNECTION.md](BLE_PROTOCOL_AND_CONNECTION.md).

## Informações que devem ser coletadas

- marca, modelo e fotografia da etiqueta da balança;
- versão do Android e modelo do telefone;
- nome anunciado e endereço mostrado pelo aplicativo;
- RSSI ao lado e sobre a balança;
- todos os UUIDs anunciados;
- todos os Manufacturer IDs;
- manufacturer data em hexadecimal;
- service data em hexadecimal;
- `ScanRecord` bruto em hexadecimal;
- sequência temporal de payloads: balança parada, durante a medição, peso
  estabilizado e logo após descer;
- linhas do Logcat com a tag `ControlaPesoBLE`;
- indicação de qualquer mudança no display da balança durante cada payload.

Ao compartilhar logs publicamente, o endereço Bluetooth pode ser mascarado,
desde que seja substituído por um identificador consistente para correlacionar
as leituras do mesmo aparelho.

## Limitações conhecidas

- O scanner observa apenas anúncios BLE e não descobre características GATT.
- A ausência de FFF0 ou 181B não exclui uma balança Chipsea/OKOK.
- O formato `C0 + sequência` foi confirmado apenas para a balança `Yoda1`
  observada; outras variantes OKOK podem anunciar dados diferentes.
- A transformação do peso bruto reproduz o número processado pelo OKOK, mas
  unidade, estabilidade e significado das demais flags ainda não foram
  confirmados no aparelho real.
- O detector atual exige uma janela de oito leituras e limites conservadores;
  esses limites ainda precisam ser comparados com a indicação de estabilidade
  do visor.
- O valor secundário não deve ser chamado de impedância até haver evidência
  adicional.
- `neverForLocation` pode fazer o Android filtrar certos tipos de beacon, apesar
  de ser a configuração usada pelo OKOK e necessária para obter resultados no
  telefone de teste.
- O aplicativo não calcula gordura, água, músculo, metabolismo ou idade
  corporal.
- Alguns fabricantes omitem campos do anúncio até a balança ser ativada pelo
  usuário.

## Próximos passos

Para consolidar esta variante:

1. capturar anúncios com valores conhecidos no display;
2. correlacionar a transição de `24` para `25` com o início e a estabilização;
3. descobrir o significado do valor secundário e das flags restantes;
4. confirmar a unidade configurada na balança;
5. confirmar a unidade no aplicativo somente depois dessa comparação; só
   então uma medição pode ser convertida para kg e persistida.

Uma conexão GATT só deve ser implementada para uma variante que apresente
evidência de serviços, características ou notificações conectadas. Nesse caso,
o usuário deverá selecionar o dispositivo explicitamente, e cada pacote deverá
continuar sendo preservado em formato bruto. O desenho arquitetural dessa
evolução está documentado em
[BLE_PROTOCOL_AND_CONNECTION.md](BLE_PROTOCOL_AND_CONNECTION.md#conexão-gatt-futura).
