# Testes

## Automáticos

Na raiz, com JDK 21:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew assembleDebugAndroidTest
```

Com emulador ou telefone autorizado:

```bash
./gradlew connectedAndroidTest
```

## Último checkpoint local

Em 27/07/2026:

```bash
./gradlew clean test lint assembleDebug assembleRelease assembleDebugAndroidTest --continue
```

Resultado: sucesso em 139 tarefas; 64 testes JVM, zero falhas/erros/ignorados;
lint com zero erros e 21 avisos não bloqueantes; APKs debug, release não
assinado e instrumentado gerados. `git diff --check` também passou.

`adb devices -l` retornou a lista vazia e não havia AVD configurado. Portanto,
os cinco testes instrumentados foram compilados, mas não executados nesse
checkpoint; `connectedAndroidTest` continua pendente até um aparelho ou
emulador estar disponível.

Cobertura unitária inclui:

- hexadecimal, little-endian, parsers Chipsea e OKOK com capturas douradas;
- estabilidade, leitura BLE, duplicidade e seleção segura de perfil;
- kg/lb, validação manual, IMC, metas e estatísticas;
- intervalos de histórico, transição de horário de verão, offset persistido e
  redução de pontos;
- mapeadores/conversores;
- CSV, JSON, validação, limite de leitura, resumo, nomes e cache;
- mapper Health Connect;
- política de lembrete, mascaramento diagnóstico e ViewModel de perfis com
  relógio/dispatcher controlados.

Testes instrumentados cobrem Room/FKs/transações e todos os repositories,
persistência DataStore, contrato de permissões do manifest, FileProvider,
geração PDF multi-página e telas Compose essenciais. A UI inclui navegação,
fonte ampliada, tema escuro e o histórico lista-detalhe em 840 dp sem endereço
BLE. Um APK de testes compilado não substitui `connectedAndroidTest`.

## BLE físico

Use [BLE_PHYSICAL_VALIDATION.md](BLE_PHYSICAL_VALIDATION.md). Registre:

- modelo/Android;
- valor e unidade do visor;
- sequência ocioso → variação → estável → zero;
- payloads e timestamps;
- número de eventos salvos;
- comportamento com Bluetooth desligado/permissão revogada;
- duas ou mais balanças próximas.

## UI

Testar no Layout Inspector/emuladores e, quando possível, aparelhos:

- 320, 360, 600 e 840 dp;
- retrato e paisagem;
- tela dividida;
- tema claro, escuro e sistema;
- cores dinâmicas ligadas/desligadas;
- fonte 100%, 150% e 200%;
- TalkBack e teclado.

Verificar que ações continuam com alvo mínimo, rótulo textual, foco previsível
e mensagens não dependentes apenas de cor.

## Relatórios

1. Gere PDF curto e com centenas de medições.
2. Abra em leitor externo e verifique páginas, quebras e disclaimer.
3. Gere CSV com acentos, `;`, aspas, quebras e texto iniciando por `=`.
4. Exporte JSON, confira prévia, mescle e compare contagens.
5. Antes de substituir, confirme o arquivo de segurança.
6. Compartilhe por ao menos um destino real e salve cópia pelo SAF.

## Health Connect e lembretes

Em API 26+, testar provedor ausente, desatualizado, permissão concedida,
negada e revogada. Confirme que ressincronizar o mesmo UUID não duplica e que
payload/endereço/nota não aparecem.

Em Android 13+, negar e conceder notificações ao ativar lembretes. Confirme
texto neutro, dias, desativação e toque abrindo Medir sem iniciar scan.
