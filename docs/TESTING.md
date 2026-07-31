# Testes

## Automáticos

Na raiz, com JDK 21:

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
./gradlew assembleRelease
./gradlew assembleInstrumentedAndroidTest
```

Com emulador ou telefone autorizado:

```bash
./gradlew connectedAndroidTest
```

`connectedAndroidTest` usa a variante descartável `instrumented`, com package
`br.com.paivalab.controlapeso.instrumented`. Ela existe para que a limpeza do
runner não altere o APK `debug` usado manualmente nem seus dados locais.
Não execute `connectedDebugAndroidTest` sobre dados que precisem ser
preservados: em 28/07/2026 o runner removeu o pacote-alvo `debug` ao encerrar
uma execução direta. Exporte um JSON antes de qualquer teste que não use a
variante isolada.

## Último checkpoint local e no telefone

Em 28/07/2026, com JDK 21:

```bash
./gradlew test lint assembleDebug assembleRelease assembleInstrumentedAndroidTest --continue
./gradlew connectedAndroidTest
```

Resultado local: sucesso em 161 tarefas; testes JVM, lint, APK debug, release
não assinado e APK instrumentado gerados sem falha. O lint não introduziu erros
bloqueantes.

No checkpoint final no Samsung `SM-S908E`, Android 16/API 36,
`connectedAndroidTest` concluiu 18/18 testes, sem falhas ou ignorados. Ao final, o package normal
`br.com.paivalab.controlapeso` continuava instalado e em primeiro plano. O
runner exibiu uma mensagem não bloqueante de `appops` para
`androidx.test.services`, mas a execução Gradle e todos os testes concluíram
com sucesso.

Uma repetição posterior foi interrompida porque o telefone estava bloqueado:
os quatro smoke tests Compose falharam sem hierarquia de UI enquanto
`isKeyguardShowing=true`. Isso não aponta uma falha do app, mas a repetição
final deve ocorrer com a tela desbloqueada e acesa.

## Checkpoint de distribuição v1.0.0

Em 28/07/2026, com JDK 21 e a configuração de assinatura de release:

```bash
./gradlew clean test lint assembleDebug assembleRelease bundleRelease \
  assembleInstrumentedAndroidTest
./gradlew connectedAndroidTest
```

O primeiro comando concluiu com sucesso em 172 tarefas; gerou debug, APKs
release universal/por ABI, AAB e APK de testes. O lint continuou sem erros e
com 21 avisos não bloqueantes já documentados. Os cinco APKs release foram
validados com `apksigner` e usam o certificado SHA-256 publicado no checklist.

No Samsung `SM-S908E`, Android 16/API 36, o APK universal release instalou,
iniciou a frio em 703 ms e permaneceu em execução sem `FATAL EXCEPTION`.
Depois, `connectedAndroidTest` executou 19/19 testes pela variante isolada
`instrumented`, preservando o package release principal.

Cobertura unitária inclui:

- hexadecimal, little-endian, parsers Chipsea e OKOK com capturas douradas;
- formatação, máscara e validação estrita de datas `DD/MM/AAAA`;
- estabilidade, leitura BLE, duplicidade e seleção segura de perfil;
- kg/lb, validação manual, metas e estatísticas; métricas derivadas permanecem
  desativadas até validação do protocolo e decisão de produto;
- intervalos de histórico, transição de horário de verão, offset persistido e
  redução de pontos;
- mapeadores/conversores;
- CSV, JSON, validação, limite de leitura, resumo, nomes e cache;
- SemVer, parsing de release GitHub, ETag/offline, seleção de asset/checksum e
  política de package, assinatura e versão do APK;
- mapper Health Connect;
- política de lembrete, mascaramento diagnóstico e ViewModel de perfis com
  relógio/dispatcher controlados.

Testes instrumentados cobrem Room/FKs/transações e todos os repositories,
persistência DataStore, contrato de permissões do manifest, FileProvider,
geração PDF multi-página e telas Compose essenciais. A UI inclui navegação
primária que volta à raiz, máscara de data com digitação sequencial, fonte
ampliada, tema escuro e o histórico lista-detalhe em 840 dp sem endereço BLE.
Um APK de testes compilado não substitui `connectedAndroidTest`.

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

## Atualização e backup local

Em aparelho físico, confirme no tema claro e escuro que o diálogo de update
mostra changelog, que **Depois** vale só para a sessão e que uma fonte
desconhecida bloqueada leva às configurações antes da confirmação Android.
Use um release futuro assinado para o teste de instalação.

Para o backup local, teste frequência desativada/diária/semanal/mensal, criação
manual, compartilhamento cancelado e concluído pelo Sharesheet, importação,
prévia, mesclar, substituir e exclusão. Nenhum desses cenários pode alterar
dados locais antes da prévia válida de JSON.
