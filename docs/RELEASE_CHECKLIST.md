# Checklist de release

## Checkpoint de distribuição v1.0.0 — 28/07/2026

- [x] `clean test lint assembleDebug assembleRelease bundleRelease`
- [x] APK universal e APKs `arm64-v8a`, `armeabi-v7a`, `x86_64` e `x86`
  compilados e assinados
- [x] AAB assinado compilado
- [x] assinatura v2 e certificado SHA-256 conferidos com `apksigner`
- [x] `connectedAndroidTest`: 19/19 no Samsung SM-S908E/API 36
- [x] APK universal release instalado e iniciado a frio no Samsung SM-S908E/API 36
- [x] `git diff --check`
- [ ] roteiro BLE completo com comparação de visor, salvamento e repetição
- [ ] PDF/CSV/JSON, Sharesheet, SAF, Health Connect, lembretes e TalkBack no
  artefato release

O release não declara concluídos os testes físicos pendentes. A chave de
assinatura fica fora do Git; o certificado publicado tem SHA-256
`CC:07:DD:7F:E3:2E:BE:8E:CB:35:30:BD:84:FD:E3:12:5E:B6:A9:D3:34:5A:AF:75:13:98:0D:32:B9:48:83:73`.

## Checkpoint local de 27/07/2026

- [x] build limpo, testes JVM, lint, debug e release
- [x] APK de testes instrumentados compilado
- [x] `git diff --check`
- [x] Manifest sem `INTERNET`, FileProvider restrito e backup automático
  desativado
- [x] DEX release sem fake BLE debug e sem chamada `connectGatt`; a rota demo
  não é registrada porque a factory release declara indisponibilidade
- [ ] `connectedAndroidTest`, instalação e matriz física — ADB sem aparelho e
  nenhum AVD configurado

Os 21 avisos de lint restantes são não bloqueantes: versões deliberadamente
preservadas, `neverForLocation` em API 31+, dicionário inglês sobre textos
portugueses e disponibilidade de SDK mais novo.

## Checkpoint no telefone de 28/07/2026

- [x] `connectedAndroidTest`: 16/16 no Samsung SM-S908E/API 36, pela variante
  isolada `instrumented`
- [x] pacote normal debug continuou instalado e abriu após os testes
- [x] scanner encontrou `Yoda1`, trocou para amostragem e mostrou peso estável
- [ ] comparar unidade e valor com o visor, salvar uma vez e repetir três
  pesagens reais
- [ ] repetir os smoke tests Compose com a tela do telefone desbloqueada; uma
  repetição com `isKeyguardShowing=true` não expôs a hierarquia de UI

Use `connectedAndroidTest`, nunca `connectedDebugAndroidTest`, quando houver
dados locais no APK debug. A variante isolada usa outro package justamente para
que a limpeza do runner não afete os dados de uso manual.

## Código e build

- [ ] `./gradlew clean test lint assembleDebug assembleRelease`
- [ ] `./gradlew assembleDebugAndroidTest`
- [x] `./gradlew connectedAndroidTest` em dispositivo autorizado
- [ ] `git diff --check`
- [ ] schema Room exportado e migration presente se versão > 1
- [ ] APK release não possui rota visível de demonstração
- [ ] nenhuma credencial, token ou SDK proprietário

## Manifest e privacidade

- [ ] sem permissão `INTERNET`
- [ ] BLE por versão e sem permissões extras
- [ ] notificações solicitadas só ao ativar lembrete
- [ ] Health Connect solicitado só após ação
- [ ] `allowBackup=false` e regras sensíveis excluídas
- [ ] FileProvider não exportado e restrito a `shared-reports`
- [ ] logs release não contêm endereço, payload, peso ou observação

## Produto

- [ ] peso BLE só salva após unidade confirmada e estabilidade
- [ ] ausência de métricas continua ausente
- [ ] duplicidade pede revisão
- [ ] excluir perfil não afeta outros perfis
- [ ] esquecer balança mantém histórico
- [ ] excluir tudo exige duas confirmações
- [ ] dados demo visíveis e removíveis, somente debug

## UI

- [ ] claro, escuro e sistema
- [ ] 320, 360, 600 e 840 dp
- [ ] retrato, paisagem e tela dividida
- [ ] fonte 200%, TalkBack e teclado
- [ ] estados vazio, erro, permissão e andamento

## Físico

- [ ] roteiro BLE completo
- [ ] PDF/CSV/JSON abrem fora do app
- [ ] Sharesheet e SAF
- [ ] Health Connect sem duplicata
- [ ] lembrete abre Medir sem scan automático

Itens físicos pendentes devem ser declarados na entrega; não devem ser
marcados como aprovados apenas porque o APK compila.
