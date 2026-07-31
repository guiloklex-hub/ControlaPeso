# Backup local e compartilhamento

O ControlaPeso mantém o backup no armazenamento privado do aparelho. Não há
OAuth, Google Drive API, conta remota ou API key no aplicativo.

## Backup local

Em **Ajustes > Dados e backup**, a pessoa pode escolher:

- Desativado;
- Diário;
- Semanal;
- Mensal, representado por um intervalo de 30 dias.

O agendamento usa WorkManager e cria o arquivo
`files/local-backups/controla-peso-backup-latest.json`. A cópia é criada a
partir do mesmo `JsonBackupManager` usado pela exportação manual. A operação
é local, não exige rede e não altera o banco se a escrita falhar.

O aplicativo mantém apenas a cópia local mais recente para evitar crescimento
silencioso do armazenamento. A frequência não liga compartilhamento: qualquer
envio externo exige uma ação explícita.

## Compartilhar

`Compartilhar último backup` copia o arquivo para o cache temporário protegido
pelo `FileProvider` e abre o Sharesheet Android. Google Drive, e-mail, cabo ou
qualquer outro destino só aparece se estiver instalado e disponível no
aparelho. O ControlaPeso não autentica, escolhe ou confirma o destino externo.

Cancelar o Sharesheet não altera o backup local. A cópia temporária é tratada
pelo mesmo limpador de arquivos dos relatórios compartilhados.

## Restauração

Importar JSON continua usando o Storage Access Framework. O arquivo completo é
validado antes da prévia; mesclar/substituir permanece explícito e a migração
Room não é destrutiva. Fotos privadas de perfil não entram no JSON.

## Exclusão local

Excluir os dados locais também cancela o WorkManager do backup e remove
`files/local-backups`, além de Room, DataStore, fotos e caches temporários.
Arquivos que a pessoa já compartilhou para fora do aplicativo não estão sob o
alcance dessa ação.

## Testes

- criar backup manual e conferir o JSON;
- substituir a cópia anterior sem criar arquivos ilimitados;
- ativar/desativar cada frequência e conferir o WorkManager;
- compartilhar, cancelar e voltar sem alterar dados;
- restaurar JSON inválido e válido sem estado parcial;
- excluir dados locais e confirmar cancelamento do agendamento;
- testar tela estreita, fonte ampliada e acessibilidade.
