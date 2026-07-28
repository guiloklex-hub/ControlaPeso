# Validação física BLE

O parser de anúncios OKOK/Yoda1 reproduz o valor visto na captura real. Uma
execução no telefone confirmou a descoberta, a mudança de modo do scan e a
detecção visual de estabilidade. A unidade física e a correlação com o visor
da balança ainda não foram comprovadas por amostras suficientes. Até essa
confirmação, a aplicação chama o número de “valor anunciado” e não o persiste
automaticamente.

## Evidência parcial — 28/07/2026

No Samsung `SM-S908E`, Android 16/API 36, com a `Yoda1` próxima:

- o scan iniciou sem filtros em `LOW_POWER`;
- ao receber anúncio OKOK, mudou para `LOW_LATENCY`;
- a interface apresentou **Peso estabilizado**, `102,85 kg`, e a origem
  `Yoda1`;
- o scan foi interrompido manualmente após a observação;
- nenhuma ação de salvar foi acionada, portanto essa sessão não comprova a
  persistência nem a garantia de um único registro.

O Logcat preservado para essa execução contém apenas os eventos operacionais:

```text
Scan BLE iniciado sem filtros por 15 segundos; fase=DISCOVERY, modo=LOW_POWER
Scan BLE mudou para fase=SAMPLING, modo=LOW_LATENCY; motivo=anúncio OKOK detectado
Scan BLE finalizado: motivo=MANUAL
```

O valor exibido pelo visor da balança não foi anotado nessa observação. Logo,
ela não confirma a unidade nem a escala do valor; essas hipóteses continuam
dependentes do roteiro abaixo.

## Detector conservador inicial

- somente anúncios de medição com propriedade `0x25`;
- no mínimo 8 leituras;
- janela mínima de 2 segundos;
- amplitude máxima de 0,10 na unidade anunciada;
- diferença máxima de 0,05 entre as duas últimas leituras;
- um evento por sessão;
- nova sessão após `0x24`, valor zero ou lacuna superior a 2 segundos.

`0x25` não é tratado isoladamente como sinal de estabilidade.

## Roteiro ainda pendente em telefone físico

1. Fechar completamente o OKOK International.
2. Instalar o APK debug.
3. Executar `adb logcat -s ControlaPesoBLE:D`.
4. Fazer ao menos três pesagens com o valor do display anotado.
5. Registrar valor anunciado, valor bruto, sequência, propriedade e duração.
6. Oscilar o peso antes de estabilizar e verificar que não há evento precoce.
7. Confirmar explicitamente se o valor anunciado representa kg ou lb.
8. Repetir com Bluetooth desligado e permissões negada/revogadas.

O scanner continua usando anúncios sem filtro e prazo total de 15 segundos.
Nenhuma conexão GATT será adicionada sem evidência.
