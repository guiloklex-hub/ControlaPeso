# Dependências

Gradle 9.5.0, AGP 9.3.1, Kotlin 2.2.10 e Compose BOM 2026.02.01 foram
preservados. Nenhuma dependência foi atualizada por conveniência.

| Dependência | Versão | Justificativa |
|---|---:|---|
| KSP | 2.3.6 | code generation do Room compatível com o Kotlin atual |
| Room runtime/ktx/compiler/testing | 2.8.4 | histórico local, FKs, transações, Flow e testes |
| DataStore Preferences | 1.2.1 | preferências pequenas e reativas |
| Navigation Compose | 2.9.8 | cinco destinos e rotas de detalhe |
| Lifecycle Compose/ViewModel | 2.9.4 | coleta consciente do lifecycle e ViewModels Compose; versão alinhada explicitamente à resolução transitiva |
| Core SplashScreen | 1.2.0 | splash compatível desde API 24 |
| Kotlinx Serialization JSON | 1.9.0 | schema de backup validável e versionado |
| WorkManager KTX | 2.11.2 | limpeza e lembretes aproximados persistentes |
| Health Connect client | 1.1.0 | escrita opcional de `WeightRecord` |
| Material icons core | BOM | ícones pequenos da navegação, sem pacote estendido |
| kotlinx-coroutines-test | 1.9.0 | relógio, dispatcher e `StateFlow` determinísticos em testes de ViewModel |

PDF usa `PdfDocument`; CSV usa biblioteca padrão; compartilhamento usa
FileProvider/SAF. Não foram adicionadas bibliotecas de gráfico, DI, banco
remoto, rede, analytics, imagem ou animação.

O layout adaptável foi implementado com APIs já presentes no Compose
(`BoxWithConstraints`, `NavigationBar`, `NavigationRail`, `FlowRow` e painéis
limitados por largura). A dependência Material 3 Adaptive prevista no plano
não foi necessária e, portanto, não foi adicionada.

## Health Connect e minSdk

O cliente 1.1.0 declara minSdk 26, mas o produto preserva minSdk 24. O
manifest merger usa `tools:overrideLibrary="androidx.health.connect.client"`
de forma explícita. A instância é lazy, `availability()` retorna antes de
qualquer chamada em API < 26 e a UI não registra o contrato nessas versões.

Essa decisão exige teste de inicialização em API 24/25 antes de release; está
registrada na matriz física. Elevar minSdk quebraria compatibilidade existente
sem necessidade.
