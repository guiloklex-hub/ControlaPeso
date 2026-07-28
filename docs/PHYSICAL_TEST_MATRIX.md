# Matriz de validação física

Preencha resultado, data e evidência. “Pendente” significa que código/build
sozinho não valida o comportamento.

No checkpoint final de 27/07/2026, `adb devices -l` não listou aparelhos e não
havia AVD configurado. O APK instrumentado foi compilado, mas a matriz abaixo
não foi promovida com base apenas no build.

| Área | Cenário | Ambiente mínimo | Resultado atual |
|---|---|---|---|
| BLE | encontrar `Yoda1` por advertising | Samsung SM-S908E/API 36 | Validado no baseline |
| BLE | unidade coincide com visor | telefone + balança | Pendente |
| BLE | estabilidade gera exatamente um salvamento | telefone + balança | Pendente |
| BLE | três balanças próximas permanecem visíveis | telefone + balanças | Pendente |
| BLE | Bluetooth desligado durante scan | telefone | Pendente |
| BLE | permissão negada e permanente | API 30 e API 31+ | Pendente |
| BLE | variante sem `neverForLocation` visível | aparelho/variante afetada | Pendente |
| Persistência | reiniciar processo mantém histórico | telefone/emulador | Pendente |
| Relatório | PDF abre e tabela não corta | leitor externo | Pendente |
| Compartilhar | WhatsApp/e-mail/Drive ou equivalente | app instalado | Pendente |
| SAF | salvar e reabrir PDF/CSV/JSON | seletor do sistema | Pendente |
| Health | escrever e ressincronizar peso | API 26+ + Health Connect | Pendente |
| Health | provedor ausente/desatualizado | API aplicável | Pendente |
| Notificação | conceder/negar e tocar lembrete | API 33+ | Pendente |
| Acessibilidade | TalkBack e fonte 200% | telefone | Pendente |
| Adaptável | 320/360/600/840 dp e split screen | emulador/tablet | Pendente |
| Tema | claro/escuro/sistema/dinâmico | API 24 e API 31+ | Pendente |
| Compatibilidade | inicialização sem Health Connect | API 24/25 | Pendente |

## Evidência BLE mínima

Para cada pesagem real, guardar:

- valor e unidade do visor;
- payload bruto mascarado;
- `rawWeight`, valor anunciado, propriedade e sequência;
- RSSI e timestamp;
- leituras coletadas, amplitude e duração da janela;
- perfil escolhido;
- quantidade de linhas criadas no histórico.

Nunca conclua unidade, impedância ou flags por um único payload.
