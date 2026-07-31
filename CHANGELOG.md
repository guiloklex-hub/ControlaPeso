# Changelog

Todas as mudanças relevantes deste projeto serão registradas aqui.

O formato segue [Keep a Changelog](https://keepachangelog.com/pt-BR/1.1.0/)
e o versionamento segue [Semantic Versioning](https://semver.org/lang/pt-BR/).

## [Unreleased]

Nenhuma alteração registrada ainda.

## [1.1.0] - 2026-07-31

### Added

- Consulta de atualização estável pelo GitHub com SemVer, ETag, checksum,
  validação de assinatura/package/versão e confirmação pelo Android.
- Backup JSON local opcional com agendamento desativado, diário, semanal ou
  mensal e compartilhamento manual pelo Sharesheet Android.
- Reflow de UI para fonte ampliada e largura compacta, estados acessíveis,
  contexto de perfil e diagnóstico BLE sem busca automática.

### Changed

- Estrutura open source, automação de CI/release e distribuição por ABI.
- Removida a integração OAuth/Google Drive direta; destinos externos aparecem
  somente no Sharesheet após ação explícita.
- Relatórios, privacidade, Ajustes, histórico, perfis e medição seguem a
  direção visual Calm Health, mantendo dados locais e payload BLE bruto.

### Fixed

- Migração Room v1→v2 permite medições sem perfil sem perder histórico ou
  payload bruto.
- Pesos exibidos em kg/lb mantêm duas casas decimais, inclusive em metas e
  relatórios, sem alterar a precisão persistida.
- Ações clicáveis não criam alvos pais concorrentes e mesclam semântica para
  tecnologias assistivas.
- A rota de demonstração permanece indisponível em builds release.

## [1.0.0] - 2026-07-28

### Added

- Registro local de peso por BLE ou entrada manual.
- Histórico, perfis, metas, gráficos, PDF, CSV e backup JSON.
- Diagnóstico BLE e preservação de payload bruto para investigação de protocolo.
- Integração opcional e minimizada com Health Connect.
- Tema claro, escuro, sistema, cores dinâmicas e interface adaptável.

### Security

- Sem Internet, analytics, anúncios, conta ou backend.
- Backup automático desativado e compartilhamento limitado a URIs temporários.

[unreleased]: https://github.com/guiloklex-hub/ControlaPeso/compare/v1.1.0...HEAD
[1.1.0]: https://github.com/guiloklex-hub/ControlaPeso/releases/tag/v1.1.0
[1.0.0]: https://github.com/guiloklex-hub/ControlaPeso/releases/tag/v1.0.0
