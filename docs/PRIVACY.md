# Privacidade

## Dados coletados

Somente dados necessários às funções escolhidas:

- perfis locais: nome, avatar local, unidade e altura/nascimento opcionais;
- peso, data/hora, offset, origem, estabilidade e observação opcional;
- nome/endereço da balança e payload bruto para medições BLE;
- metas;
- preferências;
- cadastro local de balanças conhecidas.

Campos de composição corporal permanecem nulos quando não são recebidos. O
aplicativo não deriva gordura, água, músculo, metabolismo ou idade corporal.

## Onde ficam

Room e DataStore usam o armazenamento privado do aplicativo. Relatórios
temporários usam `cache/shared-reports`. Não há servidor, conta, anúncios,
analytics ou telemetria. O Manifest não declara Internet.

Backup automático do Android está desativado e as regras excluem bancos,
preferências e arquivos. O backup suportado é o JSON manual e explícito.

## Permissões

| Permissão | Motivo |
|---|---|
| `BLUETOOTH` e `BLUETOOTH_ADMIN`, até API 30 | compatibilidade BLE antiga |
| `ACCESS_FINE_LOCATION`, até API 30 | requisito de scan no Android 11 ou anterior |
| `BLUETOOTH_SCAN` | encontrar anúncios no Android 12+ |
| `BLUETOOTH_CONNECT` | consultar estado Bluetooth no Android 12+ |
| `POST_NOTIFICATIONS` | somente quando lembretes são ativados |
| `health.WRITE_WEIGHT` | somente após ação na integração Health Connect |

`BLUETOOTH_SCAN` usa `neverForLocation`; o compromisso de visibilidade está
documentado em [BLE_DIAGNOSTIC.md](BLE_DIAGNOSTIC.md).

## Exportar e compartilhar

PDF, CSV e JSON são criados somente por ação do usuário. O Sharesheet concede
leitura temporária de um URI `content://`; nunca é usado `file://`. O SAF
permite escolher uma cópia permanente. Arquivos no cache são removidos
automaticamente depois de 24 horas ou manualmente.

O backup JSON inclui payload/endereço porque sua função é restaurar os dados
completos. PDF, CSV e resumo não incluem o endereço nem o payload bruto.

## Health Connect

É opcional, por perfil, e inicialmente somente escrita. Após permissão,
recebe peso, horário, offset, método de registro, tipo balança e identificador
idempotente. Não recebe:

- payload BLE;
- endereço ou nome da balança;
- observação;
- nome do perfil;
- dados demo;
- campos corporais ausentes.

Desativar impede novas escritas. Excluir o banco do Controla Peso não apaga
registros já entregues ao Health Connect; gerencie-os no próprio Health
Connect.

## Excluir

Em **Ajustes > Privacidade e dados**:

1. exporte um JSON, se desejar;
2. toque em excluir todos os dados;
3. confirme o aviso;
4. digite `EXCLUIR`.

Perfis, medições, metas, balanças, preferências e temporários locais são
removidos, e o onboarding reaparece. A exclusão não afeta o Health Connect nem
arquivos que o usuário salvou fora do aplicativo.

## Logs

Logs detalhados são opt-in e somente debug. Release pode registrar estados e
erros técnicos mínimos, mas o corpo que contém endereço e payload é bloqueado
por `BuildConfig.DEBUG`. Ao compartilhar um diagnóstico, mantenha a opção de
mascarar endereços ativada.
