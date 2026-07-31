# Privacidade

## Identificação e versão deste aviso

Este documento usa um modelo genérico de aviso de privacidade e deve passar
por revisão jurídica antes de uma distribuição final:

- controlador/responsável informado: Guilherme Silva Paiva;
- contato para direitos e encarregado: `contato@paivalab.com.br`;
- versão: 1.0;
- data de publicação: 31/07/2026.

## Dados coletados

Somente dados necessários às funções escolhidas:

- perfis locais: nome, avatar local, foto privada opcional, unidade e
  altura/nascimento opcionais;
- peso, data/hora, offset, origem, estabilidade e observação opcional;
- nome/endereço da balança e payload bruto para medições BLE;
- metas;
- preferências;
- cadastro local de balanças conhecidas.

Campos de composição corporal permanecem nulos quando não são recebidos. O
aplicativo não deriva gordura, água, músculo, metabolismo ou idade corporal.

## Onde ficam

Room, DataStore e fotos de perfil (`files/profile-photos`) usam o armazenamento
privado do aplicativo. Relatórios temporários usam `cache/shared-reports` e
APKs baixados para uma instalação pendente usam `cache/release-updates`; ambos
são removidos pela exclusão local. Não há backend próprio, anúncios,
analytics ou telemetria. O Manifest declara Internet apenas para a consulta
publica de releases no GitHub. Compartilhamentos externos são iniciados pelo
Sharesheet Android após ação da pessoa usuária.

Backup automático do Android está desativado e as regras excluem bancos,
preferências e arquivos. O backup suportado é o JSON local, criado manualmente
ou em frequência escolhida, sem envio automático.
Fotos de perfil permanecem somente no armazenamento privado e ainda não fazem
parte do JSON até que a política de backup de fotos seja definida.

## Permissões

| Permissão | Motivo |
|---|---|
| `BLUETOOTH` e `BLUETOOTH_ADMIN`, até API 30 | compatibilidade BLE antiga |
| `ACCESS_FINE_LOCATION`, até API 30 | requisito de scan no Android 11 ou anterior |
| `BLUETOOTH_SCAN` | encontrar anúncios no Android 12+ |
| `BLUETOOTH_CONNECT` | consultar estado Bluetooth no Android 12+ |
| `POST_NOTIFICATIONS` | somente quando lembretes são ativados |
| `health.WRITE_WEIGHT` | somente após ação na integração Health Connect |
| `INTERNET` | consultar release público |
| `REQUEST_INSTALL_PACKAGES` | entregar APK verificado ao instalador Android, que sempre pede confirmação |

`BLUETOOTH_SCAN` usa `neverForLocation`; o compromisso de visibilidade está
documentado em [BLE_DIAGNOSTIC.md](BLE_DIAGNOSTIC.md).

## Exportar e compartilhar

PDF, CSV e JSON são criados somente por ação do usuário. O Sharesheet concede
leitura temporária de um URI `content://`; nunca é usado `file://`. O SAF
permite escolher uma cópia permanente. Arquivos no cache são removidos
automaticamente depois de 24 horas ou manualmente.

O backup JSON inclui payload/endereço porque sua função é restaurar os dados
completos. PDF, CSV e resumo não incluem o endereço nem o payload bruto.

## Rede, atualização e compartilhamento

Na abertura do processo principal, o aplicativo consulta o release estável
mais recente do repositório oficial no GitHub. A consulta envia a requisição
HTTPS normal (incluindo endereço IP visível ao provedor e um ETag técnico), mas
não envia perfis, medições, payload BLE, identificador de publicidade ou conta
do usuário. Para reutilizar uma resposta `304`, o aparelho guarda apenas a
resposta pública do release já validada junto ao ETag. Falhas ficam silenciosas
e não bloqueiam o uso local.

O backup local automático é opcional e fica no armazenamento privado. O
compartilhamento de um JSON usa o Sharesheet Android somente após uma ação
explícita; Google Drive pode aparecer como destino instalado, mas o aplicativo
não autentica nem envia diretamente para ele.

O download de uma atualização confere hash, package, assinatura e versão antes
de abrir o instalador Android. A permissão de fontes desconhecidas e a
confirmação final pertencem ao sistema Android; a instalação não é silenciosa.

Cancelar o Sharesheet não altera o arquivo local. Cópias que a pessoa escolhe
enviar para fora do aplicativo passam a ser administradas pelo destino
selecionado.

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

Perfis, fotos privadas, medições, metas, balanças, preferências e temporários
locais são removidos, e o onboarding reaparece. A exclusão não afeta o Health
Connect nem arquivos que o usuário salvou fora do aplicativo.

## Logs

Logs detalhados são opt-in e somente debug. Release pode registrar estados e
erros técnicos mínimos, mas o corpo que contém endereço e payload é bloqueado
por `BuildConfig.DEBUG`. Ao compartilhar um diagnóstico, mantenha a opção de
mascarar endereços ativada.
