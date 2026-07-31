# Regras do projeto ControlaPeso

- Preservar Kotlin, Jetpack Compose e Material 3.
- Preservar o scanner BLE e os parsers funcionais; qualquer adaptação deve ter
  testes de regressão e manter o payload bruto.
- Não inventar campos, unidade, estabilidade ou detalhes do protocolo.
- Não atualizar Gradle, Android Gradle Plugin ou Kotlin sem necessidade técnica comprovada.
- Não adicionar dependências sem justificativa.
- Não colocar lógica Bluetooth LE em Composables.
- Não calcular métricas corporais sem que o protocolo tenha sido validado com dados reais.
- Preservar o payload bruto em qualquer implementação de protocolo.
- Tratar permissões de Bluetooth conforme a versão do Android.
- Evitar scans infinitos e sempre liberar callbacks e recursos.
- Criar testes unitários para parsers.
- Manter os dados locais, sem analytics, anúncios, backend ou telemetria.
- Manter layouts adaptáveis e acessíveis, incluindo fontes ampliadas.
- Não usar fallback destrutivo em migrations do Room.
- Não incluir o modo de demonstração em builds release.
- Manter backups locais; qualquer compartilhamento externo deve usar o Sharesheet
  Android e não adicionar OAuth, backend ou integração direta de nuvem sem nova
  decisão explícita.
- Executar `./gradlew test`, `./gradlew lint` e `./gradlew assembleDebug` antes de finalizar.
- Informar todos os arquivos modificados na entrega.
