# Como contribuir

Obrigado por considerar uma contribuição ao Controla Peso. O projeto prioriza
privacidade, rastreabilidade de dados BLE e uma experiência simples em Android.

## Antes de começar

- Leia o [README](README.md), a [arquitetura](docs/ARCHITECTURE.md) e a
  [política de privacidade](docs/PRIVACY.md).
- Leia [AGENTS.md](AGENTS.md) ao trabalhar com automação assistida.
- Procure uma issue existente antes de abrir outra.
- Para mudanças grandes, abra primeiro uma issue de proposta para alinharmos
  objetivo, compatibilidade e teste físico.

## Ambiente local

É necessário JDK 21 e Android SDK 36.1. Não atualize Gradle, AGP, Kotlin ou
Compose apenas por conveniência; proponha e justifique a mudança em separado.

```bash
./gradlew test
./gradlew lint
./gradlew assembleDebug
./gradlew assembleRelease
```

Use `./gradlew connectedAndroidTest` apenas com emulador ou telefone dedicado.
A variante `instrumented` existe para não apagar dados do APK debug usado
manualmente.

## Regras para Bluetooth e dados de saúde

- Não reescreva o scanner ou parser funcional sem captura real e testes de
  regressão.
- Preserve o payload bruto de protocolos observados.
- Não invente unidade, estabilidade, UUID, métrica corporal ou regra de
  conversão.
- Não conecte nem escreva em GATT por suposição.
- Não adicione rede, analytics, backend, SDK proprietário ou telemetria.
- Não envie endereço BLE, payload, nome de perfil ou notas para Health Connect.

Se você quiser contribuir com uma nova balança, abra uma issue com modelo,
versão do Android, anúncios mascarados, valor/unidade exibidos e sequência
temporal. Nunca publique endereço Bluetooth completo, dados pessoais ou backup
de terceiros.

## Código e interface

- Preserve Kotlin, Compose e Material 3.
- Mantenha lógica BLE fora de Composables; use estado imutável e `StateFlow`.
- Não adicione dependências sem justificativa técnica e alternativa avaliada.
- Mantenha fontes ampliadas, temas claro/escuro e layout adaptável.
- Use textos em recursos quando forem visíveis ao usuário.
- Inclua teste para parser, regra de domínio ou correção que possa regredir.

## Pull requests

1. Crie uma branch descritiva a partir de `main`.
2. Mantenha o diff focado e atualize documentos relevantes.
3. Execute os comandos de qualidade acima e informe o resultado.
4. Descreva riscos, dispositivos físicos testados e limitações pendentes.
5. Não inclua chaves, APKs locais, arquivos de banco, logs sensíveis ou dados
   de pessoas usuárias.

As contribuições são distribuídas sob a [Apache-2.0](LICENSE). Ao enviar um PR,
você confirma que tem direito de contribuir o conteúdo sob essa licença.

## Comunicação

Use issues para bugs, propostas e dúvidas técnicas. Vulnerabilidades devem ser
reportadas segundo [SECURITY.md](SECURITY.md). Todas as interações seguem o
[Código de Conduta](CODE_OF_CONDUCT.md).
