# Manifesto de screenshots UI/UX

Capturas de 28/07/2026. Telefone físico Samsung SM-S908E, Android 16/API 36,
resolução 1080 × 2316 px, densidade física 450 dpi e override 420 dpi.
Aplicativo `br.com.paivalab.controlapeso`, versão 1.0 (1). Cor dinâmica estava
ativa, salvo quando o alto contraste substituiu a paleta.

O perfil `Teste_Debug` e os pesos visíveis são dados artificiais já existentes
no APK debug. Nenhum endereço BLE, payload, credencial ou dado pessoal foi
capturado.

## Baseline anterior

| Arquivo | Rota/estado | Orientação | Tema | Font scale | Observação |
| --- | --- | --- | --- | ---: | --- |
| [dashboard-phone.png](before/dashboard-phone.png) | Dashboard com dados | Paisagem, 2316 × 1080 | Escuro | 0,8 | APK instalado às 09:05, antes do redesign desta execução. |

## Rodada 1

| Arquivo | Rota/estado | Orientação | Tema | Font scale |
| --- | --- | --- | --- | ---: |
| [dashboard-phone-dark-expanded.png](after/dashboard-phone-dark-expanded.png) | Dashboard com dados | Paisagem | Escuro | 0,8 |
| [dashboard-phone-light-compact.png](after/dashboard-phone-light-compact.png) | Dashboard com dados | Retrato | Claro | 0,8 |
| [measure-menu-phone-dark-expanded.png](after/measure-menu-phone-dark-expanded.png) | Raiz Medir | Paisagem | Escuro | 0,8 |
| [live-waiting-phone-dark-expanded.png](after/live-waiting-phone-dark-expanded.png) | Medição pronta | Paisagem | Escuro | 0,8 |
| [live-scanning-phone-dark-expanded.png](after/live-scanning-phone-dark-expanded.png) | Scan aguardando peso | Paisagem | Escuro | 0,8 |
| [history-phone-dark-expanded.png](after/history-phone-dark-expanded.png) | Histórico com dados | Paisagem | Escuro | 0,8 |
| [settings-phone-dark-expanded.png](after/settings-phone-dark-expanded.png) | Ajustes | Paisagem | Escuro | 0,8 |
| [settings-phone-light-expanded.png](after/settings-phone-light-expanded.png) | Ajustes | Paisagem | Claro | 0,8 |
| [dashboard-phone-light-font200.png](after/dashboard-phone-light-font200.png) | Dashboard; achado de sobreposição na barra | Retrato | Claro | 2,0 |
| [manual-form-phone-light-font200.png](after/manual-form-phone-light-font200.png) | Formulário; achado de sobreposição na barra | Retrato | Claro | 2,0 |

## Rodada 2

| Arquivo | Rota/estado | Orientação | Tema/opções | Font scale |
| --- | --- | --- | --- | ---: |
| [dashboard-phone-light-font200-round2.png](after/dashboard-phone-light-font200-round2.png) | Dashboard após correção dos rótulos | Retrato | Claro | 2,0 |
| [manual-form-phone-light-font200-round2.png](after/manual-form-phone-light-font200-round2.png) | Formulário após correção dos rótulos | Retrato | Claro | 2,0 |
| [settings-phone-light-high-contrast-reduced.png](after/settings-phone-light-high-contrast-reduced.png) | Ajustes/acessibilidade | Retrato | Claro, contraste reforçado, efeitos reduzidos | 0,8 |

O traço vertical arredondado na borda direita das capturas em retrato é a alça
do Samsung Edge Panel e não pertence ao layout Compose.

## Reprodução

```bash
JAVA_HOME=/tmp/controlapeso-jdk21.YVQjvw \
PATH=/tmp/controlapeso-jdk21.YVQjvw/bin:$PATH \
./gradlew installDebug

adb shell am force-stop br.com.paivalab.controlapeso
adb shell am start -n br.com.paivalab.controlapeso/.MainActivity
adb shell screencap -p /sdcard/controlapeso-ui.png
adb pull /sdcard/controlapeso-ui.png .
```

Para não depender de relógio, banco ou Bluetooth em futuras referências
automatizadas, use os estados em `ui/preview/`. As capturas físicas deste
manifesto validam integração, insets e navegação no aparelho real.
