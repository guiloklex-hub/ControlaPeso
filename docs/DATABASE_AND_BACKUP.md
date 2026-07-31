# Banco de dados e backup

## Room schema v2

Banco: `controla_peso.db`.

### `profiles`

UUID, nome, avatar, altura/nascimento opcionais, unidade, Health Connect,
perfil ativo e timestamps.

Foto de perfil é um arquivo opcional em `files/profile-photos`, indexado pelo
ID do perfil, e não uma coluna do Room.

### `weight_measurements`

UUID, perfil opcional, peso kg, `Instant`, offset, origem, estabilidade,
dispositivo,
nome/endereço copiados, nota, payload bruto e métricas opcionais. Índices
cobrem perfil+horário, dispositivo, origem e payload.

### `goals`

UUID, perfil, pesos inicial/destino, datas, status, indicador ativo e
timestamps. A ativação é transacional e deixa no máximo uma meta ativa por
perfil.

### `scale_devices`

UUID, nome, endereço único, protocolo, último uso/detecção, preferência e
timestamps.

## Integridade

- excluir perfil usa `SET_NULL` para medições e `CASCADE` para metas; o
  histórico de peso não é apagado e pode ser atribuído depois;
- esquecer balança usa `SET_NULL` no FK, mantendo nome/endereço históricos
  copiados na medição;
- troca de perfil ativo é transacional;
- criação/reativação de meta desativa a anterior na mesma transação;
- não existe fallback destrutivo.

O schema exportado fica em `app/schemas/.../1.json` e `2.json`. A
`MIGRATION_1_2` recria apenas a tabela de medições para tornar `profileId`
opcional, copiando todas as colunas sem descartar o payload bruto. O upgrade é
validado por `MigrationTestHelper`.

## Peso e tempo

Peso persistido é `Double` em kg somente após validação da unidade. A UI
converte para kg/lb. Tempo usa `Instant`; o offset original em segundos
preserva a interpretação local para CSV e Health Connect.

## Duplicidade

A consulta considera perfil (inclusive o estado sem perfil), tolerância de
peso, janela temporal e endereço quando ele existe. Entrada manual, sem
endereço esperado, compara todos os dispositivos. O usuário pode autorizar
uma ocorrência legítima.

## Backup

Consulte [REPORT_FORMATS.md](REPORT_FORMATS.md#json). JSON v1 é o único backup
portável. Backup automático do sistema está desabilitado devido à natureza
dos dados.

O JSON não inclui fotos de perfil enquanto a política de inclusão e restauração
de fotos não estiver definida; a foto permanece privada no aparelho.

Importação inválida não toca o banco. Substituição apaga e reinsere tabelas
em uma transação Room depois de criar um backup de segurança. O arquivo de
segurança é apresentado na área de arquivo gerado para o usuário compartilhar
ou salvar.

### Backup local e compartilhamento

O backup local não altera o schema Room. A frequência é guardada no DataStore,
o JSON mais recente fica em `files/local-backups` e o WorkManager cria a cópia
sem exigir rede. Compartilhar copia o arquivo para o cache protegido pelo
FileProvider e abre o Sharesheet; Google Drive pode aparecer como destino
instalado, mas o app não autentica nem usa a Drive API. Consulte
[Backup local e compartilhamento](LOCAL_BACKUP_AND_SHARING.md).

## Exclusão total

Exclusão usa transação Room na ordem segura, limpa DataStore, remove fotos
privadas, cancela o lembrete e o backup local, e remove cache. O banco permanece aberto para que os `Flow` ativos
publiquem imediatamente o estado vazio e o aplicativo retorne ao onboarding;
fechar a instância compartilhada durante o processo deixaria repositories
vivos apontando para um banco fechado.
