# Formatos de relatório

Todos os formatos são gerados offline e em UTF-8 quando textuais.

## PDF

Gerado com `android.graphics.pdf.PdfDocument`. Contém cabeçalho, perfil,
intervalo, geração, resumo, gráfico opcional, tabela opcional, observações e
métricas somente quando presentes, unidade e aviso não médico.

Linhas longas são quebradas na largura útil. A tabela abre nova página antes
de uma linha não caber, sem criar página vazia. O gráfico reduz datasets
grandes deterministicamente e preserva primeiro/último ponto.

## CSV

- encoding UTF-8;
- separador `;`;
- terminador de linha CRLF;
- decimal com vírgula e duas casas;
- data ISO `AAAA-MM-DD`;
- hora ISO local;
- coluna `offset_utc`;
- unidade no nome da coluna e em cada linha;
- uma medição por linha;
- ausentes como campo vazio.

Colunas base:

```text
id;perfil;peso_kg|peso_lb;unidade;data;hora;offset_utc;origem;dispositivo;estavel
```

Observação e métricas adicionais são opcionais. Endereço e payload BLE não
são exportados. Textos iniciados por caracteres de fórmula de planilha recebem
apóstrofo para reduzir risco de execução ao abrir o CSV.

## JSON

Backup completo com:

```json
{
  "schemaVersion": 1
}
```

Inclui perfis, medições, metas, dispositivos e preferências exportáveis.
Preserva UUIDs, `Instant`, offset, origem, valores nulos, endereço e payload
bruto. Não inclui permissões concedidas, cache, logs, tokens ou estado interno
do Health Connect.

Antes da importação:

1. o conteúdo é lido com limite de 20 Mi caracteres e todo o JSON é
   decodificado;
2. schema, UUIDs, datas, enumerações, faixas e referências são validados;
3. a tela mostra contagens e IDs existentes;
4. o usuário escolhe mesclar ou substituir.

Mesclar mantém a versão com `updatedAt` mais novo e normaliza perfil/meta
ativos. Substituir cria primeiro um JSON de segurança no cache e executa as
mudanças Room em transação. Preferências DataStore são restauradas depois da
transação, pois Room e DataStore não compartilham transação atômica.
Se a restauração falhar, o arquivo de segurança é mantido e a falha é mostrada
sem deixar a tela presa em andamento.

## Nomes e compartilhamento

Nomes são normalizados, sem separadores de caminho, e incluem timestamp. Os
MIME types são:

- PDF: `application/pdf`;
- CSV: `text/csv`;
- JSON: `application/json`.

O Sharesheet recebe URI `content://` e permissão temporária de leitura. O
botão salvar usa Storage Access Framework.
