# Checklist de release

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

## Código e build

- [ ] `./gradlew clean test lint assembleDebug assembleRelease`
- [ ] `./gradlew assembleDebugAndroidTest`
- [ ] `./gradlew connectedAndroidTest` em dispositivo autorizado
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
