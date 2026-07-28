# Matriz de validação física

Preencha resultado, data e evidência. “Pendente” significa que código/build
sozinho não valida o comportamento.

Em 28/07/2026, o Samsung `SM-S908E` (Android 16/API 36) foi conectado por ADB.
O scanner encontrou a `Yoda1`, reagiu a um anúncio OKOK e a tela apresentou um
peso estabilizado. A unidade não foi comparada com o visor e nenhum salvamento
foi confirmado nessa sessão; esses itens continuam pendentes.

| Área | Cenário | Ambiente mínimo | Resultado atual |
|---|---|---|---|
| BLE | encontrar `Yoda1` por advertising | Samsung SM-S908E/API 36 | Validado em 28/07/2026: `Yoda1`, mudança `LOW_POWER` → `LOW_LATENCY` |
| BLE | unidade coincide com visor | telefone + balança | Pendente |
| BLE | estabilidade gera exatamente um salvamento | telefone + balança | Parcial: UI apresentou “Peso estabilizado”; salvar/persistir não foi acionado |
| BLE | três balanças próximas permanecem visíveis | telefone + balanças | Pendente |
| BLE | Bluetooth desligado durante scan | telefone | Pendente |
| BLE | permissão negada e permanente | API 30 e API 31+ | Pendente |
| BLE | variante sem `neverForLocation` visível | aparelho/variante afetada | Pendente |
| Data | digitar e validar `DD-MM-AAAA` | Samsung SM-S908E/API 36 | Validado: medição manual, filtro de histórico e meta aceitaram dígitos sequenciais e exibiram a máscara correta |
| Navegação | aba Medir retorna à tela raiz | Samsung SM-S908E/API 36 | Validado: formulário manual → Histórico → Medir retornou a “Registrar peso”, sem restaurar o formulário |
| Perfis | criar, ativar e excluir perfil de teste | Samsung SM-S908E/API 36 | Validado com `PerfilTeste`; o perfil ativo voltou para `Teste_Debug` antes da exclusão |
| Metas | prazo, pausar e reativar | Samsung SM-S908E/API 36 | Validado: prazo salvo e exibido como `28-12-2026`; pausa e reativação persistiram na tela |
| Persistência | reiniciar processo mantém histórico | telefone/emulador | Pendente no fluxo manual; repositories também passaram em teste instrumentado |
| Relatório | gerar arquivos localmente | Samsung SM-S908E/API 36 | Validado: PDF, CSV e JSON foram gerados para os dados de teste; abertura externa, compartilhamento e SAF continuam pendentes |
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
