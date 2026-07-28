# Baseline do projeto

Data: 27/07/2026  
Branch: `feat/complete-weight-app`  
Commit: `1212cb4`

## Stack preservada

- Package e namespace: `br.com.paivalab.controlapeso`
- Gradle Wrapper: 9.5.0
- Android Gradle Plugin: 9.3.1
- Kotlin: 2.2.10
- compileSdk: Android 36, minor API 1
- targetSdk: 36
- minSdk: 24
- Compose BOM: 2026.02.01
- Interface: Jetpack Compose e Material 3

O shell não possui Java configurado globalmente. Os comandos abaixo foram
executados com `JAVA_HOME=/tmp/controlapeso-jdk21.YVQjvw`.

## Resultado

```text
./gradlew clean test lint assembleDebug assembleRelease
BUILD SUCCESSFUL in 1m 4s
99 actionable tasks: 99 executed
```

`test`, `lint`, `assembleDebug` e `assembleRelease` passaram. O Android Gradle
Plugin informou apenas que `libandroidx.graphics.path.so` foi empacotada sem
strip, aviso já presente e não bloqueante.

Não havia aparelho ou emulador listado por `adb devices -l` neste checkpoint,
portanto `connectedAndroidTest`, instalação e validação física não foram
executados.

## BLE confirmado no código

- scan amplo, sem filtro, com duração total de 15 segundos;
- fase inicial `LOW_POWER` e fase de amostragem `LOW_LATENCY`;
- deduplicação por endereço na ViewModel;
- captura de UUIDs, manufacturer data, service data e frame bruto;
- parser `OkOkAdvertisementParser` para o anúncio Yoda1 já capturado;
- valor bruto big-endian dividido por 100, ainda sem unidade fisicamente
  confirmada;
- propriedades `0x24` (ocioso) e `0x25` (medição), sem inferir estabilidade;
- parser conectado Chipsea conservador e desacoplado.

Esses comportamentos são invariantes de regressão para as próximas fases.
