# Política de segurança

## Versões suportadas

| Versão | Suportada |
|---|---|
| `1.0.x` | Sim |
| Anteriores | Não |

## Reportar uma vulnerabilidade

Não abra issue pública para vulnerabilidades que possam expor dados de saúde,
payloads BLE, arquivos compartilhados, permissões, backup ou assinatura de
releases.

Use uma [Security Advisory privada](https://github.com/guiloklex-hub/ControlaPeso/security/advisories/new)
e inclua, quando possível:

- descrição e impacto esperado;
- passos mínimos para reproduzir;
- versões do app e Android;
- prova de conceito sem dados de terceiros;
- sugestão de correção, se houver.

O projeto confirmará o recebimento, avaliará a reprodução e coordenará uma
correção antes da divulgação pública. Não há SLA formal; vulnerabilidades que
envolvam execução de código, exposição de dados ou assinatura têm prioridade.

## Escopo sensível

Relate especialmente problemas relacionados a:

- permissões Bluetooth e exposição de endereço/payload;
- exportação, `FileProvider`, SAF e Sharesheet;
- backup e persistência local;
- Health Connect;
- validação ou distribuição de APKs assinados;
- dependências e workflow de release.
