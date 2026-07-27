# Regras do projeto ControlaPeso

- Preservar Kotlin, Jetpack Compose e Material 3.
- Não atualizar Gradle, Android Gradle Plugin ou Kotlin sem necessidade técnica comprovada.
- Não adicionar dependências sem justificativa.
- Não colocar lógica Bluetooth LE em Composables.
- Não calcular métricas corporais sem que o protocolo tenha sido validado com dados reais.
- Preservar o payload bruto em qualquer implementação de protocolo.
- Tratar permissões de Bluetooth conforme a versão do Android.
- Evitar scans infinitos e sempre liberar callbacks e recursos.
- Criar testes unitários para parsers.
- Executar `./gradlew test`, `./gradlew lint` e `./gradlew assembleDebug` antes de finalizar.
- Informar todos os arquivos modificados na entrega.
