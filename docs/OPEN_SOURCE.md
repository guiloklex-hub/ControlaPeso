# Governança open source

## Princípios

Controla Peso é um projeto Android local-first e offline-first. As prioridades
de manutenção são, nesta ordem:

1. segurança e privacidade dos dados;
2. integridade da captura BLE e da persistência;
3. acessibilidade e compatibilidade Android;
4. clareza da documentação e reprodutibilidade de releases;
5. evolução visual e recursos adicionais.

Nenhuma contribuição deve trocar dados pessoais por conveniência de produto.
Não há coleta remota, conta, anúncios, telemetria ou backend planejados.

## Manutenção

O repositório usa issues e pull requests no GitHub. O `CODEOWNERS` aponta a
revisão final ao mantenedor atual. Mudanças que mexam em BLE, banco, backup,
permissões, Health Connect ou assinatura exigem revisão cuidadosa e evidência
de teste proporcional ao risco.

Dependabot cria no máximo uma atualização mensal por ecossistema. Cada PR de
atualização ainda deve ser avaliado contra a regra do projeto de não atualizar
Gradle, AGP, Kotlin ou Compose sem necessidade técnica comprovada.

## Versionamento

O app segue SemVer no `versionName` e no [CHANGELOG](../CHANGELOG.md):

- `MAJOR`: quebra de compatibilidade ou migração relevante;
- `MINOR`: recurso compatível;
- `PATCH`: correção compatível.

`versionCode` é monotônico e deve aumentar antes de uma distribuição que
substitua uma instalação existente. A primeira publicação open source usa
`versionCode = 1` e `versionName = 1.0.0`.

## Releases

Tags `v*` acionam a automação de release. O fluxo só publica binários se as
quatro credenciais de assinatura estiverem configuradas como secrets do
repositório. A chave nunca é versionada e o APK nunca é publicado sem
assinatura. Consulte [Releases](RELEASING.md).

## Escopo comunitário

Consulte [CONTRIBUTING.md](../CONTRIBUTING.md), [CODE_OF_CONDUCT.md](../CODE_OF_CONDUCT.md)
e [SECURITY.md](../SECURITY.md). A licença é [Apache-2.0](../LICENSE).
