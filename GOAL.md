Você está trabalhando em um aplicativo Android existente chamado ControlaPeso.

Projeto:
~/AndroidStudioProjects/ControlaPeso

Package e namespace existentes:
br.com.paivalab.controlapeso

CONTEXTO IMPORTANTE

O aplicativo já consegue:

- detectar a balança Bluetooth Low Energy;
- receber informações da balança;
- identificar corretamente o peso;
- apresentar uma interface simples e leve.

Essa comunicação BLE funcionando é o componente mais importante do projeto.

NÃO substitua, reescreva ou remova a implementação BLE existente sem primeiro
entender completamente o código atual.

Qualquer refatoração da camada Bluetooth deve:

- preservar o comportamento atual;
- preservar o parser que identifica corretamente o peso;
- manter os payloads brutos disponíveis para diagnóstico;
- possuir testes;
- ser feita somente quando necessária;
- ser validada no build;
- não inventar detalhes do protocolo.

OBJETIVO

Transformar o projeto existente em um aplicativo Android completo, moderno,
bonito, leve, confiável e privado para acompanhamento de peso corporal.

O aplicativo deve:

- capturar medições da balança BLE;
- salvar o histórico localmente;
- mostrar tendências e evolução;
- trabalhar com vários perfis;
- permitir metas de peso;
- gerar relatórios;
- exportar dados;
- compartilhar relatórios por WhatsApp, e-mail e outros aplicativos;
- funcionar bem em celulares pequenos, celulares grandes, tablets,
  dobráveis, retrato, paisagem e tela dividida;
- oferecer modo claro, escuro e seguir o sistema;
- integrar opcionalmente com Health Connect;
- permanecer funcional sem internet;
- não exigir cadastro;
- não possuir anúncios;
- não enviar dados para servidores;
- não depender de serviços pagos.

O aplicativo não é um dispositivo médico e não deve fornecer diagnósticos,
prescrições, classificações alarmistas ou promessas de saúde.

MODO DE TRABALHO

Execute o trabalho em etapas, mas continue automaticamente até concluir todas
as etapas possíveis.

Antes de fazer alterações:

1. Analise completamente o projeto.
2. Localize toda a implementação BLE.
3. Identifique como o peso é recebido.
4. Identifique como a estabilidade da medição é determinada.
5. Identifique versões de:
   - Gradle;
   - Android Gradle Plugin;
   - Kotlin;
   - Compose;
   - Material 3;
   - compileSdk;
   - targetSdk;
   - minSdk.
6. Identifique dependências existentes.
7. Execute o build atual.
8. Registre o estado inicial em docs/BASELINE.md.
9. Crie IMPLEMENTATION_PLAN.md com todas as fases e critérios de aceite.
10. Depois da análise, implemente sem esperar nova confirmação, salvo se houver
    um bloqueio real que torne a continuação impossível.

Não faça uma reconstrução indiscriminada do projeto.

Não atualize Gradle, AGP, Kotlin ou Compose apenas para usar uma API nova.

Caso uma dependência necessária seja incompatível com o projeto atual:

- explique o problema;
- escolha a alternativa compatível mais simples;
- não faça atualização em cascata sem justificativa.

PRINCÍPIOS DO PRODUTO

O aplicativo deve ser:

- local-first;
- offline-first;
- acessível;
- adaptável;
- leve;
- fácil de usar;
- seguro;
- testável;
- preparado para manutenção;
- visualmente agradável sem sacrificar legibilidade;
- transparente sobre quais métricas são medidas e quais são calculadas.

Nenhuma métrica corporal pode ser inventada.

Se a balança fornece apenas peso, mostrar apenas:

- peso;
- data;
- hora;
- evolução;
- estatísticas calculadas a partir do histórico;
- IMC somente quando o usuário informar altura.

Impedância, gordura corporal, massa muscular, água corporal, massa óssea,
gordura visceral, metabolismo basal e outras métricas só podem aparecer quando:

- forem realmente recebidas da balança; ou
- houver um algoritmo documentado, validado e explicitamente identificado
  como estimativa.

Nunca apresentar uma estimativa como medição direta.

DESIGN VISUAL

Criar uma identidade visual moderna e elegante usando Jetpack Compose e
Material 3.

Nome exibido:
Controla Peso

Conceito visual:

- saúde;
- tranquilidade;
- evolução;
- clareza;
- tecnologia discreta;
- aparência premium;
- interface simples;
- informações fáceis de compreender.

Usar:

- Material 3;
- edge-to-edge;
- cantos arredondados;
- espaços consistentes;
- tipografia hierárquica;
- ícones do Material;
- cartões com transparência moderada;
- fundos com gradientes suaves;
- superfícies translúcidas;
- sombras discretas;
- animações curtas;
- transições naturais;
- feedback visual durante a medição;
- skeleton ou indicador de carregamento quando necessário.

A transparência não pode prejudicar contraste ou leitura.

Não aplicar blur pesado em todas as telas.

Criar nas configurações uma opção:

- Efeitos visuais completos;
- Efeitos reduzidos.

Quando efeitos reduzidos estiverem ativos:

- remover transparências excessivas;
- remover animações decorativas;
- usar superfícies sólidas;
- preservar todas as funcionalidades.

Não utilizar imagens remotas na interface.

Não adicionar bibliotecas grandes somente para animação.

TEMAS

Implementar três opções:

- Seguir o sistema;
- Claro;
- Escuro.

Adicionar também:

- cores dinâmicas do Android quando disponíveis;
- opção de ativar ou desativar cores dinâmicas;
- paleta própria como fallback;
- persistência da escolha com DataStore;
- preview dos temas;
- suporte correto à barra de status e navegação;
- contraste adequado em todos os modos.

Todas as telas devem funcionar em modo claro e escuro.

ARQUITETURA

Usar uma arquitetura organizada, sem exagerar na abstração.

Estrutura sugerida:

br.com.paivalab.controlapeso/

app/
    ControlaPesoApplication.kt
    AppContainer.kt

bluetooth/
    ExistingBleImplementation.kt
    BleConnectionManager.kt
    BleMeasurement.kt
    BleDeviceInfo.kt
    BleConnectionState.kt
    ScaleProtocol.kt
    ScaleParser.kt
    BleDiagnosticsRepository.kt

data/
    local/
        ControlaPesoDatabase.kt
        dao/
        entity/
        mapper/
        migration/
    preferences/
        AppPreferences.kt
        AppPreferencesRepository.kt
    repository/
        MeasurementRepositoryImpl.kt
        ProfileRepositoryImpl.kt
        GoalRepositoryImpl.kt
        DeviceRepositoryImpl.kt
    export/
        CsvExporter.kt
        PdfReportGenerator.kt
        JsonBackupManager.kt
        ShareFileManager.kt
    healthconnect/
        HealthConnectManager.kt

domain/
    model/
    repository/
    usecase/

ui/
    navigation/
    components/
    theme/
    onboarding/
    dashboard/
    measurement/
    history/
    measurementdetail/
    reports/
    profiles/
    goals/
    devices/
    settings/
    diagnostics/
    about/

worker/
    MeasurementReminderWorker.kt
    ExportCleanupWorker.kt

Não colocar lógica de banco, BLE, exportação ou regras de negócio dentro de
Composables.

Usar:

- ViewModel;
- StateFlow;
- coroutines;
- repositories;
- use cases apenas onde melhorarem clareza;
- Room;
- DataStore Preferences;
- Navigation Compose;
- WorkManager para lembretes aproximados;
- FileProvider para compartilhamento;
- PdfDocument ou solução nativa equivalente para PDF;
- Health Connect como integração opcional.

Não introduzir Hilt ou outro framework de injeção de dependências sem
necessidade real.

Preferir um AppContainer simples e testável.

Não adicionar Firebase.

Não adicionar analytics.

Não adicionar SDK de publicidade.

Não adicionar backend.

BANCO DE DADOS

Usar Room.

Criar entidades equivalentes a:

ProfileEntity

- id: String UUID;
- name: String;
- avatarKey: String?;
- heightCm: Double?;
- birthDate: LocalDate?;
- isActive: Boolean;
- createdAt: Instant;
- updatedAt: Instant.

Não exigir data de nascimento ou sexo para salvar peso.

Campos corporais adicionais só devem ser solicitados quando realmente
necessários para uma funcionalidade escolhida pelo usuário.

WeightMeasurementEntity

- id: String UUID;
- profileId: String;
- weightKg: Double;
- measuredAt: Instant;
- zoneOffsetSeconds: Int?;
- source: BLE, MANUAL, IMPORT, HEALTH_CONNECT;
- isStable: Boolean;
- deviceId: String?;
- note: String?;
- rawPayloadHex: String?;
- impedanceOne: Double?;
- impedanceTwo: Double?;
- bodyFatPercent: Double?;
- muscleMassKg: Double?;
- bodyWaterPercent: Double?;
- boneMassKg: Double?;
- visceralFatLevel: Double?;
- metabolicAge: Int?;
- createdAt: Instant;
- updatedAt: Instant.

Todos os campos de composição corporal devem ser nullable.

Nunca gravar zero para indicar “não disponível”.

GoalEntity

- id;
- profileId;
- startWeightKg;
- targetWeightKg;
- startDate;
- targetDate opcional;
- isActive;
- createdAt;
- updatedAt.

ScaleDeviceEntity

- id;
- displayName;
- bluetoothAddress;
- protocolName;
- lastConnectedAt;
- lastSeenAt;
- isPreferred;
- createdAt;
- updatedAt.

Criar índices e relacionamentos necessários.

Não utilizar fallbackToDestructiveMigration.

Criar testes de migração quando houver mais de uma versão do banco.

Trabalhar internamente com peso em quilogramas.

Converter para libras somente na apresentação e exportação conforme a
preferência do usuário.

Não armazenar peso internamente como texto formatado.

NAVEGAÇÃO

Em telas compactas, usar barra de navegação inferior.

Destinos principais:

- Início;
- Histórico;
- Medir;
- Relatórios;
- Ajustes.

A ação Medir pode ter destaque visual central, desde que permaneça acessível.

Em telas médias ou expandidas:

- usar NavigationRail ou NavigationDrawer adaptável;
- evitar apenas esticar a interface do celular;
- usar layout lista-detalhe;
- aproveitar o espaço adicional;
- limitar a largura máxima de conteúdos textuais;
- permitir que gráfico e resumo apareçam lado a lado.

Preservar estado ao mudar orientação e tamanho da janela.

TELA DE INTEGRAÇÃO INICIAL

Criar onboarding curto e opcional.

Etapas:

1. Boas-vindas.
2. Explicação sobre dados locais.
3. Permissões Bluetooth.
4. Criação do primeiro perfil.
5. Seleção opcional de meta.
6. Seleção de tema.
7. Explicação de como subir na balança.
8. Integração opcional com Health Connect.

Permitir pular itens opcionais.

Não bloquear o aplicativo caso o usuário não queira criar uma meta ou usar
Health Connect.

TELA INÍCIO

A tela inicial deve mostrar:

- saudação apropriada;
- perfil ativo;
- estado da balança;
- botão para conectar ou medir;
- último peso;
- data e horário da última medição;
- diferença em relação à medição anterior;
- diferença em relação ao início do período;
- progresso da meta;
- gráfico resumido;
- média móvel opcional;
- maior e menor peso do período;
- quantidade de medições;
- ações rápidas;
- estado vazio amigável quando não houver medições.

A diferença deve indicar claramente:

- aumento;
- redução;
- sem alteração;
- falta de dados suficientes.

Não usar mensagens culpabilizantes.

Não usar frases como:

- peso ruim;
- falhou;
- engordou demais;
- resultado perigoso.

Preferir mensagens neutras:

- variação registrada;
- tendência do período;
- nova medição salva;
- ainda não há dados suficientes.

TELA DE MEDIÇÃO AO VIVO

Criar uma experiência visual dedicada para receber peso da balança.

Estados:

- Bluetooth desligado;
- permissão ausente;
- procurando balança;
- balança encontrada;
- conectando;
- conectado;
- aguardando alguém subir;
- recebendo peso;
- peso variando;
- peso estabilizado;
- salvando;
- salvo;
- conexão perdida;
- erro recuperável;
- erro não recuperável.

Durante a medição:

- mostrar o valor em tamanho grande;
- mostrar unidade;
- usar animação leve;
- mostrar indicador de estabilidade;
- mostrar nome da balança;
- mostrar qualidade aproximada do sinal, sem prometer precisão;
- permitir cancelar;
- permitir selecionar o perfil;
- não salvar leituras intermediárias como medições finais;
- impedir duplicatas causadas por notificações BLE repetidas;
- preservar logs técnicos somente no modo de diagnóstico.

Quando o peso estabilizar:

- dar feedback visual;
- vibrar levemente se habilitado;
- salvar uma única vez;
- mostrar confirmação;
- permitir adicionar nota;
- permitir corrigir perfil;
- permitir excluir a medição recém-salva;
- permitir compartilhar a medição.

Preservar o algoritmo de estabilidade já existente.

Caso não exista algoritmo explícito, criar um detector conservador e
configurável, baseado em múltiplas leituras semelhantes durante um intervalo,
com constantes documentadas e testes.

Não utilizar um único pacote isolado como medição estável sem justificativa do
protocolo.

DUPLICIDADE

Criar proteção contra duplicidade.

Considerar:

- identificador do pacote;
- payload bruto;
- horário;
- transição para estado estável;
- peso;
- dispositivo;
- perfil.

Não excluir silenciosamente possíveis medições legítimas.

Quando houver dúvida, apresentar confirmação ao usuário.

HISTÓRICO

Criar tela completa de histórico com:

- gráfico de linha;
- pontos individuais;
- linha de tendência ou média móvel;
- seleção de período;
- lista cronológica;
- agrupamento por dia ou mês;
- filtros;
- resumo estatístico;
- estados vazios;
- carregamento;
- tratamento de erro.

Períodos:

- 7 dias;
- 30 dias;
- 3 meses;
- 6 meses;
- 1 ano;
- Tudo;
- Intervalo personalizado.

Exibir:

- peso inicial do período;
- peso final;
- variação absoluta;
- variação percentual;
- média;
- mínimo;
- máximo;
- quantidade de medições;
- frequência média de registro.

O gráfico deve:

- funcionar em tema claro e escuro;
- ser legível com fonte ampliada;
- permitir toque nos pontos;
- mostrar data e valor;
- possuir resumo textual acessível;
- não depender exclusivamente de cor;
- não exagerar visualmente pequenas variações;
- informar claramente o intervalo do eixo.

Preferir implementação com Compose Canvas.

Adicionar biblioteca de gráfico somente se:

- for realmente necessária;
- estiver ativa e mantida;
- for compatível com o projeto;
- tiver licença apropriada;
- não aumentar excessivamente o aplicativo;
- sua inclusão for documentada.

MEDIÇÕES MANUAIS

Permitir adicionar peso manualmente.

Campos:

- peso;
- data;
- hora;
- perfil;
- observação;
- origem manual.

Validar:

- campo vazio;
- formato decimal;
- valores impossíveis;
- valores extremos;
- datas futuras;
- duplicatas prováveis.

Permitir editar e excluir medições.

Toda exclusão deve solicitar confirmação e oferecer Snackbar para desfazer
quando tecnicamente viável.

DETALHE DA MEDIÇÃO

Mostrar:

- peso;
- data;
- hora;
- perfil;
- origem;
- dispositivo;
- observação;
- diferença para a anterior;
- métricas adicionais disponíveis;
- informação sobre estabilidade;
- botão editar;
- botão excluir;
- botão compartilhar.

Payload bruto e endereço Bluetooth não devem aparecer na tela normal.

Esses dados devem ficar somente na área de diagnóstico.

METAS

Permitir:

- definir peso desejado;
- registrar peso inicial;
- prazo opcional;
- editar meta;
- pausar meta;
- concluir meta;
- remover meta;
- visualizar progresso;
- visualizar quanto falta;
- visualizar evolução desde o início.

Não sugerir ritmo de emagrecimento ou ganho de peso como recomendação médica.

Não exibir metas automaticamente como saudáveis ou não saudáveis.

Adicionar textos neutros e um aviso de que decisões de saúde devem ser
discutidas com profissional qualificado.

PERFIS

Suportar vários perfis locais.

Funcionalidades:

- criar;
- editar;
- excluir;
- selecionar perfil ativo;
- avatar local;
- altura opcional;
- meta individual;
- histórico separado;
- relatórios separados;
- preferência de unidade;
- integração Health Connect por perfil principal.

Ao receber uma medição:

- salvar no perfil ativo;
- permitir trocar antes de confirmar;
- sugerir perfil somente quando houver segurança;
- nunca atribuir automaticamente quando dois perfis tiverem pesos semelhantes;
- pedir confirmação em caso de ambiguidade.

Ao excluir um perfil:

- explicar o que acontecerá com o histórico;
- oferecer exportação antes da exclusão;
- exigir confirmação clara;
- não excluir acidentalmente outros perfis.

IMC

Calcular IMC somente quando houver altura válida no perfil.

Usar:

IMC = peso em kg / altura em metros ao quadrado

Mostrar como informação derivada.

Não utilizar o IMC para diagnóstico.

Não mostrar classificação clínica automaticamente na primeira versão.

Explicar:

- calculado a partir do peso e altura;
- não medido pela balança;
- possui limitações;
- não substitui avaliação profissional.

RELATÓRIOS

Criar uma central de relatórios.

Permitir escolher:

- perfil;
- período;
- formato;
- inclusão de gráfico;
- inclusão de tabela;
- inclusão de observações;
- inclusão de métricas adicionais;
- unidade.

Formatos obrigatórios:

1. PDF visual.
2. CSV.
3. JSON para backup e restauração.

RELATÓRIO PDF

Gerar localmente.

Conteúdo:

- nome Controla Peso;
- perfil;
- intervalo;
- data da geração;
- resumo;
- primeiro e último peso;
- variação;
- média;
- mínimo;
- máximo;
- quantidade de medições;
- gráfico;
- tabela das medições;
- observações quando selecionadas;
- unidade;
- aviso de que o relatório não é diagnóstico médico.

Paginar corretamente.

Não cortar tabela.

Não gerar páginas vazias.

Não depender de internet.

CSV

Gerar em UTF-8.

Para pt-BR:

- usar ponto e vírgula como separador;
- documentar o formato;
- usar data e hora sem ambiguidade;
- informar unidade no cabeçalho;
- incluir uma linha por medição.

Colunas sugeridas:

id;
perfil;
peso;
unidade;
data;
hora;
origem;
dispositivo;
estavel;
observacao;
impedancia_1;
impedancia_2;
gordura_corporal;
massa_muscular;
agua_corporal;
massa_ossea;
gordura_visceral;
idade_metabolica.

Campos indisponíveis devem ficar vazios, nunca preenchidos com zero inventado.

JSON

Usar versão de esquema:

{
  "schemaVersion": 1
}

Incluir:

- perfis;
- medições;
- metas;
- dispositivos opcionais;
- preferências exportáveis.

Não incluir:

- permissões;
- credenciais;
- tokens;
- cache;
- logs;
- configurações internas de segurança.

Criar validação antes de importar.

Importação deve:

- mostrar prévia;
- contar registros;
- identificar duplicatas;
- permitir mesclar ou substituir;
- criar backup antes de substituir;
- nunca apagar dados automaticamente após arquivo inválido.

COMPARTILHAMENTO

Usar Android Sharesheet.

Usar FileProvider e URI content://.

Nunca usar file://.

Permitir compartilhar:

- uma medição como texto;
- resumo do período como texto;
- PDF;
- CSV;
- backup JSON.

O compartilhamento deve funcionar com:

- WhatsApp;
- e-mail;
- Telegram;
- Google Drive;
- outros aplicativos compatíveis.

Não criar integrações específicas e frágeis para cada aplicativo.

Não exigir que WhatsApp esteja instalado.

Mensagem de medição sugerida:

"Minha medição no Controla Peso:
Peso: 75,4 kg
Data: 27/07/2026 às 18:30"

O usuário deve poder revisar o conteúdo antes de compartilhar.

Não compartilhar dados sem ação explícita.

Gerar arquivos temporários em diretório apropriado.

Criar limpeza segura dos arquivos temporários.

Não deixar relatórios pessoais indefinidamente no cache.

HEALTH CONNECT

Implementar integração opcional e isolada.

A integração não pode ser obrigatória para usar o aplicativo.

Funcionalidades:

- verificar disponibilidade;
- explicar o que será compartilhado;
- solicitar permissões somente após ação do usuário;
- escrever peso usando WeightRecord;
- usar horário e fuso corretos;
- identificar registros criados pelo Controla Peso;
- evitar duplicatas;
- mostrar estado da sincronização;
- permitir desativar;
- permitir tentar novamente;
- não enviar payload bruto;
- não enviar endereço Bluetooth;
- não enviar observações privadas sem necessidade.

Inicialmente, priorizar sincronização do Controla Peso para Health Connect.

Importação do Health Connect deve ser uma opção separada.

Caso a integração de leitura aumente muito o escopo ou gere conflito de
duplicação, implementar a escrita completamente e preparar a arquitetura para
leitura posterior.

DISPOSITIVOS

Criar tela de balanças conhecidas.

Mostrar:

- nome;
- estado;
- último uso;
- última detecção;
- protocolo identificado;
- dispositivo preferido;
- opção esquecer;
- opção conectar;
- opção abrir diagnóstico.

Não iniciar scan permanente.

Não manter conexão BLE sem necessidade.

Não executar varredura contínua em segundo plano.

Não solicitar permissão de localização além do necessário.

CONFIGURAÇÕES

Criar seções:

Aparência

- seguir sistema;
- claro;
- escuro;
- cores dinâmicas;
- efeitos completos;
- efeitos reduzidos.

Medição

- kg ou lb;
- perfil padrão;
- salvar automaticamente após estabilidade;
- confirmação antes de salvar;
- vibração;
- som opcional;
- balança preferida.

Histórico

- período padrão;
- exibir média móvel;
- agrupamento;
- unidade.

Lembretes

- ativar ou desativar;
- dias da semana;
- horário aproximado;
- mensagem neutra;
- respeitar permissões de notificação;
- permitir cancelar.

Integrações

- Health Connect;
- exportação;
- importação.

Privacidade

- dados armazenados localmente;
- excluir todos os dados;
- exportar antes de excluir;
- política de privacidade local;
- logs de diagnóstico;
- limpar arquivos temporários.

Acessibilidade

- efeitos reduzidos;
- alto contraste adicional, se necessário;
- tamanho visual dos gráficos;
- feedback tátil.

Sobre

- versão;
- build;
- licenças;
- aviso médico;
- código-fonte ou repositório quando configurado;
- informações do protocolo da balança;
- contato, somente se já existir no projeto.

LEMBRETES

Usar WorkManager para lembretes aproximados.

Não solicitar permissão de alarme exato.

Não criar notificações excessivas.

Permitir:

- horário;
- dias;
- desativação;
- alteração;
- toque para abrir a tela de medição.

Texto neutro:

"Que tal registrar seu peso hoje?"

Não usar pressão, culpa ou mensagens alarmistas.

DIAGNÓSTICO BLE

Preservar e aprimorar a tela de diagnóstico existente.

Ela deve ser acessível em:

Ajustes > Diagnóstico Bluetooth

Mostrar somente quando o usuário entrar nessa área:

- estado Bluetooth;
- permissões;
- scan;
- dispositivo;
- endereço;
- RSSI;
- serviços;
- características;
- UUIDs;
- notifications e indications;
- payload bruto;
- payload interpretado;
- timestamps;
- estado de estabilidade;
- erros GATT;
- parser selecionado.

Permitir:

- copiar payload;
- limpar logs;
- exportar diagnóstico;
- ocultar endereço ao exportar;
- ativar logs detalhados apenas em debug;
- executar um teste de conexão;
- visualizar versão do parser.

Em builds release:

- não registrar payloads continuamente;
- não registrar informações pessoais;
- não manter logs indefinidamente;
- não expor detalhes técnicos na tela principal.

PRIVACIDADE E SEGURANÇA

Todos os dados devem permanecer no aparelho por padrão.

Não criar conta.

Não enviar telemetria.

Não enviar peso para servidor.

Não adicionar internet permission sem necessidade comprovada.

Caso INTERNET já exista e não seja utilizada, analisar se pode ser removida sem
quebrar o projeto.

Compartilhamento e Health Connect devem depender de ação e autorização do
usuário.

Usar armazenamento interno para dados privados.

Usar FileProvider para compartilhamento temporário.

Não salvar relatórios automaticamente em pasta pública.

Usar Storage Access Framework quando o usuário escolher onde salvar.

Antes de excluir todos os dados:

- explicar a irreversibilidade;
- oferecer exportação;
- solicitar confirmação adicional;
- fechar banco e limpar dados corretamente.

Verificar configuração de backup automático.

Como são dados de saúde, não permitir que uma decisão padrão exponha dados sem
que o usuário saiba.

Documentar em docs/PRIVACY.md:

- quais dados são coletados;
- onde ficam;
- como excluir;
- como exportar;
- quais permissões são usadas;
- como Health Connect funciona;
- que não há servidor;
- que não há anúncios;
- que não há analytics.

ACESSIBILIDADE

Implementar:

- suporte a TalkBack;
- semantic properties;
- content descriptions adequadas;
- labels em campos;
- ordem de foco;
- mínimo de 48 dp para alvos de toque;
- contraste;
- suporte a fontes grandes;
- suporte a escala de fonte de pelo menos 200%;
- navegação por teclado em telas grandes;
- gráficos com descrição textual;
- mensagens de erro que não dependam somente de cor;
- feedback claro de conexão;
- indicação textual além de ícones.

Não adicionar contentDescription redundante em elementos decorativos.

TELAS ADAPTÁVEIS

Criar layouts para:

Compact

- telefone em retrato;
- uma coluna;
- barra inferior;
- cartões empilhados.

Medium

- telefone grande;
- dobrável;
- tablet pequeno;
- NavigationRail;
- duas colunas quando útil.

Expanded

- tablet;
- ChromeOS;
- modo desktop;
- NavigationRail ou Drawer;
- dashboard em painéis;
- histórico lista-detalhe;
- gráfico e estatísticas lado a lado;
- largura máxima de leitura.

Usar APIs adaptáveis compatíveis com as versões atuais do projeto.

Não bloquear orientação.

Não fixar layout em retrato.

Testar:

- 320 dp;
- 360 dp;
- 600 dp;
- 840 dp;
- paisagem;
- tela dividida;
- fonte ampliada.

ESTADOS DE INTERFACE

Toda tela com dados deve tratar:

- carregando;
- sucesso;
- vazio;
- erro;
- sem permissão;
- offline quando aplicável;
- conteúdo parcial;
- ação em andamento.

Não mostrar tela branca sem explicação.

Não usar Toast para fluxos importantes.

Preferir Snackbar, dialog e conteúdo inline conforme o caso.

DESEMPENHO

Manter o aplicativo leve.

Evitar:

- bibliotecas grandes;
- recomposições desnecessárias;
- leitura completa do banco a cada atualização;
- operações de arquivo na thread principal;
- geração de PDF na thread principal;
- scan BLE infinito;
- conexão BLE permanente;
- listas sem LazyColumn;
- imagens grandes;
- animações contínuas;
- logs excessivos.

Usar:

- Flow do Room;
- consultas agregadas;
- paginação ou limitação quando necessário;
- Dispatchers.IO para arquivos e banco;
- chaves estáveis em listas;
- remember e derivedStateOf apenas quando apropriados;
- immutable UI state quando possível.

QUALIDADE DA EXPERIÊNCIA

Adicionar:

- ícone do aplicativo;
- splash screen;
- estados vazios ilustrados com vetores simples;
- mensagens claras;
- confirmação de ações destrutivas;
- desfazer quando viável;
- formatação pt-BR;
- unidade configurável;
- data e hora locais;
- suporte a fuso horário;
- tratamento de mudança de fuso;
- preservação de estado;
- rotação;
- restauração após encerramento do processo.

Colocar textos em strings.xml.

Preparar internacionalização.

Idioma inicial:

- português do Brasil.

Não deixar textos importantes hardcoded em Composables.

TESTES UNITÁRIOS

Criar testes para:

- parser BLE existente;
- conversão de bytes;
- peso;
- estabilidade;
- proteção de duplicidade;
- conversão kg/lb;
- cálculo de IMC;
- estatísticas;
- média;
- mínimo;
- máximo;
- variação;
- média móvel;
- filtros por período;
- validação manual;
- DAOs;
- repositories;
- ViewModels;
- exportação CSV;
- serialização JSON;
- validação de importação;
- geração de resumo;
- lógica de metas;
- atribuição de perfil;
- Health Connect mapper;
- nomes de arquivos;
- limpeza de cache.

Usar dados de teste determinísticos.

Não depender de relógio real diretamente.

Criar abstração Clock quando necessário.

TESTES INSTRUMENTADOS

Criar testes essenciais para:

- banco Room;
- migração;
- navegação principal;
- cadastro de perfil;
- inserção manual;
- edição;
- exclusão;
- troca de tema;
- exportação;
- FileProvider;
- permissões quando tecnicamente testável;
- tela adaptável básica.

Não tentar simular hardware BLE real em todos os testes.

Isolar a interface BLE para permitir fake implementation.

Criar um modo de demonstração somente em debug que injete pesos falsos.

O modo de demonstração:

- não deve existir ou estar acessível em release;
- deve estar claramente identificado;
- deve permitir testar estados de medição;
- não deve misturar dados falsos com dados reais sem aviso;
- deve permitir limpar dados de demonstração.

DOCUMENTAÇÃO

Criar ou atualizar:

README.md

- visão geral;
- requisitos;
- como abrir;
- como compilar;
- como executar;
- como testar;
- arquitetura;
- capturas futuras;
- limitações.

AGENTS.md

- regras permanentes do projeto;
- preservar BLE funcional;
- não inventar protocolo;
- não inventar métricas;
- não atualizar Gradle sem necessidade;
- executar testes;
- preservar privacidade;
- manter layout adaptável;
- manter acessibilidade.

IMPLEMENTATION_PLAN.md

- fases;
- arquivos;
- critérios;
- progresso;
- pendências.

docs/ARCHITECTURE.md

- camadas;
- fluxo BLE;
- banco;
- exportação;
- Health Connect;
- navegação.

docs/BLE_PROTOCOL.md

- UUIDs;
- formato conhecido;
- payloads;
- hipóteses;
- campos confirmados;
- campos desconhecidos;
- estabilidade;
- limitações.

docs/PRIVACY.md

- armazenamento;
- permissões;
- exportação;
- exclusão;
- Health Connect.

docs/REPORT_FORMATS.md

- PDF;
- CSV;
- JSON;
- versões de esquema.

docs/TESTING.md

- unitários;
- instrumentados;
- hardware real;
- modo debug.

docs/RELEASE_CHECKLIST.md

- build;
- testes;
- permissões;
- acessibilidade;
- privacidade;
- compartilhamento;
- temas;
- telas;
- Bluetooth.

FASES DE IMPLEMENTAÇÃO

Fase 0 — Baseline

- analisar projeto;
- localizar BLE;
- executar build;
- documentar estado;
- não alterar comportamento.

Fase 1 — Fundação

- arquitetura;
- Room;
- DataStore;
- repositories;
- modelos;
- navegação;
- tema;
- testes básicos.

Fase 2 — Persistência da medição

- integrar BLE existente ao repository;
- salvar peso estável;
- impedir duplicatas;
- medição manual;
- perfil ativo.

Fase 3 — Interface principal

- onboarding;
- dashboard;
- medição ao vivo;
- histórico;
- detalhe;
- edição;
- exclusão.

Fase 4 — Design e adaptação

- modo claro;
- modo escuro;
- sistema;
- transparência;
- efeitos reduzidos;
- layouts compact, medium e expanded;
- acessibilidade.

Fase 5 — Perfis e metas

- múltiplos perfis;
- seleção;
- ambiguidade;
- metas;
- progresso;
- IMC opcional.

Fase 6 — Relatórios

- PDF;
- CSV;
- JSON;
- exportação;
- importação;
- compartilhamento;
- FileProvider;
- limpeza.

Fase 7 — Integrações

- Health Connect;
- lembretes;
- dispositivos conhecidos;
- diagnóstico aprimorado.

Fase 8 — Qualidade

- testes;
- otimização;
- acessibilidade;
- documentação;
- release build.

Após cada fase:

1. executar compilação;
2. corrigir erros;
3. executar testes relevantes;
4. atualizar IMPLEMENTATION_PLAN.md;
5. continuar para a próxima fase.

Não declarar uma fase concluída se houver funcionalidade simulada ou quebrada.

DEPENDÊNCIAS

Adicionar somente o necessário.

Dependências esperadas, caso ainda não existam:

- Room;
- Room KTX;
- KSP ou mecanismo já usado no projeto;
- DataStore Preferences;
- Navigation Compose;
- Lifecycle ViewModel Compose;
- WorkManager;
- Health Connect client;
- Material 3 Adaptive, somente se compatível;
- Core SplashScreen.

Antes de adicionar cada dependência:

- verificar se já existe;
- verificar compatibilidade;
- usar versão adequada ao catálogo ou stack atual;
- não duplicar bibliotecas;
- documentar a justificativa.

Não adicionar biblioteca de PDF se PdfDocument for suficiente.

Não adicionar biblioteca de CSV para uma exportação simples.

Não adicionar Gson se kotlinx.serialization já estiver disponível.

Não adicionar kotlinx.serialization se outra solução segura já estiver no
projeto e for adequada.

VALIDAÇÃO OBRIGATÓRIA

Executar, conforme disponibilidade do projeto:

./gradlew test
./gradlew lint
./gradlew assembleDebug
./gradlew assembleRelease

Se houver testes instrumentados configurados e dispositivo ou emulador
disponível:

./gradlew connectedAndroidTest

Também executar:

git diff --check
git status --short

Corrigir:

- erros de compilação;
- erros de lint relacionados às alterações;
- imports não usados;
- recursos ausentes;
- strings hardcoded;
- falhas de teste;
- problemas de manifest;
- problemas de FileProvider;
- problemas de navegação;
- crashes previsíveis;
- APIs obsoletas introduzidas.

Não:

- desabilitar lint globalmente;
- ignorar testes;
- adicionar suppression genérica;
- remover testes para obter build verde;
- esconder falhas;
- afirmar que testou Bluetooth real sem dispositivo físico;
- afirmar que WhatsApp foi testado se apenas o Sharesheet foi validado;
- afirmar que Health Connect foi testado sem disponibilidade real.

CRITÉRIOS DE ACEITE

O trabalho somente pode ser considerado concluído quando:

- a captura BLE anterior continuar funcionando;
- uma medição estável puder ser salva;
- o histórico persistir após reiniciar o app;
- o usuário puder inserir, editar e excluir peso;
- o dashboard mostrar o último peso;
- os gráficos funcionarem;
- os filtros funcionarem;
- perfis funcionarem;
- metas funcionarem;
- modo claro funcionar;
- modo escuro funcionar;
- seguir sistema funcionar;
- telas pequenas funcionarem;
- telas grandes funcionarem;
- PDF for gerado;
- CSV for gerado;
- JSON puder ser exportado;
- compartilhamento usar FileProvider;
- dados puderem ser excluídos;
- logs técnicos não vazarem na interface normal;
- todos os campos ausentes permanecerem ausentes;
- build debug passar;
- testes unitários passarem;
- documentação estiver atualizada.

ENTREGA FINAL

Ao concluir, apresentar:

1. resumo executivo;
2. arquitetura adotada;
3. árvore dos principais arquivos;
4. lista completa de arquivos criados;
5. lista completa de arquivos alterados;
6. dependências adicionadas e justificativas;
7. alterações na implementação BLE;
8. explicação da proteção contra duplicatas;
9. esquema do banco;
10. telas implementadas;
11. comportamento adaptável;
12. temas implementados;
13. formatos de relatório;
14. integração com Health Connect;
15. permissões utilizadas;
16. testes criados;
17. comandos executados;
18. resultados de test;
19. resultados de lint;
20. resultados dos builds;
21. limitações;
22. itens que exigem teste em telefone físico;
23. instruções para instalar;
24. roteiro de teste com a balança;
25. roteiro de teste de compartilhamento;
26. roteiro de teste em tablet;
27. diff resumido;
28. saída de git status --short.

Não faça commit automaticamente.

Não faça push.

Não publique o aplicativo.

Deixe todas as alterações disponíveis para revisão.

Quando uma funcionalidade não puder ser concluída, seja explícito:

- o que faltou;
- por que faltou;
- qual arquivo está envolvido;
- qual seria o próximo comando ou alteração;
- como validar depois.

Comece agora pela análise do projeto e pelo build de baseline.