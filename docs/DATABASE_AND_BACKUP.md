# Banco de dados e backup

## Room schema v1

Banco: `controla_peso.db`.

### `profiles`

UUID, nome, avatar, altura/nascimento opcionais, unidade, Health Connect,
perfil ativo e timestamps.

### `weight_measurements`

UUID, perfil, peso kg, `Instant`, offset, origem, estabilidade, dispositivo,
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

- excluir perfil usa cascade para medições e metas;
- esquecer balança usa `SET_NULL` no FK, mantendo nome/endereço históricos
  copiados na medição;
- troca de perfil ativo é transacional;
- criação/reativação de meta desativa a anterior na mesma transação;
- não existe fallback destrutivo.

O schema exportado fica em `app/schemas/.../1.json`. Como a primeira versão do
banco é v1, ainda não há migration histórica. A próxima alteração deverá:

1. incrementar versão;
2. adicionar `Migration`;
3. preservar o schema v1;
4. criar `MigrationTestHelper`;
5. testar upgrade e dados existentes.

## Peso e tempo

Peso persistido é `Double` em kg somente após validação da unidade. A UI
converte para kg/lb. Tempo usa `Instant`; o offset original em segundos
preserva a interpretação local para CSV e Health Connect.

## Duplicidade

A consulta considera perfil, tolerância de peso, janela temporal e endereço
quando ele existe. Entrada manual, sem endereço esperado, compara todos os
dispositivos. O usuário pode autorizar uma ocorrência legítima.

## Backup

Consulte [REPORT_FORMATS.md](REPORT_FORMATS.md#json). JSON v1 é o único backup
portável. Backup automático do sistema está desabilitado devido à natureza
dos dados.

Importação inválida não toca o banco. Substituição apaga e reinsere tabelas
em uma transação Room depois de criar um backup de segurança. O arquivo de
segurança é apresentado na área de arquivo gerado para o usuário compartilhar
ou salvar.

## Exclusão total

Exclusão usa transação Room na ordem segura, limpa DataStore, cancela o
lembrete e remove cache. O banco permanece aberto para que os `Flow` ativos
publiquem imediatamente o estado vazio e o aplicativo retorne ao onboarding;
fechar a instância compartilhada durante o processo deixaria repositories
vivos apontando para um banco fechado.
