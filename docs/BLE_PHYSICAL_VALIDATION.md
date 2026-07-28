# Validação física BLE

O parser de anúncios OKOK/Yoda1 reproduz o valor visto na captura real, mas a
unidade física e o comportamento de estabilidade ainda não foram comprovados
por amostras suficientes. Até essa confirmação, a aplicação chama o número de
“valor anunciado” e não o persiste automaticamente.

## Detector conservador inicial

- somente anúncios de medição com propriedade `0x25`;
- no mínimo 8 leituras;
- janela mínima de 2 segundos;
- amplitude máxima de 0,10 na unidade anunciada;
- diferença máxima de 0,05 entre as duas últimas leituras;
- um evento por sessão;
- nova sessão após `0x24`, valor zero ou lacuna superior a 2 segundos.

`0x25` não é tratado isoladamente como sinal de estabilidade.

## Roteiro pendente em telefone físico

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
