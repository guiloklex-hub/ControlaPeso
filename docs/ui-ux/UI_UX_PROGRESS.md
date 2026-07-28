# Progresso UI/UX

Atualizado em 28/07/2026.

## Estado

Implementação Compose e validação visual concluídas. O único item externo
incompleto é a criação dos componentes e frames finais no Figma: o conector
passou a retornar `INVALID_ARGUMENT` até para leitura mínima.

## Concluído

- auditoria, inventário, baseline, pesquisa e plano;
- três direções descritas e avaliadas pela matriz;
- Calm Health escolhida com total ponderado 9,36;
- arquivo Figma criado com 82 variáveis, nove estilos de texto, dois estilos
  de elevação, Cover e Foundations;
- design system Compose para cor, tipografia, forma, espaçamento, elevação,
  movimento e tamanhos;
- componentes de hero, métrica, status, meta, ações, estados, perfil,
  medição, ajustes e contêiner responsivo;
- previews e fixtures determinísticos sem I/O;
- navegação compact/medium/expanded e retorno de cada aba à sua raiz;
- Dashboard, Medição ao vivo, Histórico, gráfico e Ajustes redesenhados;
- Perfis, Metas, Relatórios, formulários, Detalhe, Balanças, Privacidade,
  Sobre, Onboarding e Diagnóstico alinhados ao sistema;
- capturas antes/depois no Samsung SM-S908E;
- duas rodadas de revisão visual documentadas;
- correção da sobreposição da barra inferior em fonte 200%;
- teste unitário dos breakpoints e teste instrumentado dos rótulos em 200%;
- 19 testes instrumentados executados com sucesso no telefone.

## Evidências

- [Design system](DESIGN_SYSTEM.md);
- [direções e matriz](DESIGN_DIRECTIONS.md);
- [revisão visual](VISUAL_REVIEW.md);
- [manifesto de screenshots](screenshots/manifest.md);
- [ControlaPeso UI Exploration](https://www.figma.com/design/yI28NOZ0CgbnQ6HLXCR4sV).

## Validações executadas até aqui

```text
./gradlew compileDebugKotlin                         BUILD SUCCESSFUL
./gradlew test compileInstrumentedAndroidTestKotlin BUILD SUCCESSFUL
./gradlew test lint assembleDebug assembleRelease   BUILD SUCCESSFUL
./gradlew connectedAndroidTest                      BUILD SUCCESSFUL
./gradlew installDebug                              BUILD SUCCESSFUL
git diff --check                                    sem erros
```

O primeiro `connectedAndroidTest` detectou uma asserção ambígua no estado
vazio do Dashboard. A UI foi corrigida para ter título e corpo distintos e a
suíte foi repetida: 19/19 testes passaram.

O runner imprime um aviso não fatal ao tentar definir
`MANAGE_EXTERNAL_STORAGE` para `androidx.test.services`, pacote ausente no
telefone. O processo segue e a suíte termina verde.

Lint termina verde e mantém 21 avisos informativos já compatíveis com a
governança do projeto: versões preservadas, atributo BLE condicionado à API e
falsos positivos do corretor inglês sobre textos em português. Nenhum aviso
foi desabilitado.

## Figma

O plano Starter permitiu um modo por coleção; claro e escuro usam coleções
semânticas distintas. A estrutura criada foi validada inicialmente sem aliases
quebrados, tokens sem sintaxe ou tokens sem escopo.

Depois das fundações, todas as chamadas `use_figma` e `get_metadata`, inclusive
scripts que apenas retornam o nome das páginas, passaram a falhar com
`INVALID_ARGUMENT`. Portanto:

- as três direções estão especificadas e pontuadas no repositório;
- os componentes e frames Dashboard/Medição/Histórico/Ajustes/expanded ainda
  não existem no Figma;
- não é alegada comparação screenshot-versus-frame;
- nenhuma tela Compose foi gerada pelo Figma.

## Bloqueios ambientais

- `java` não está no `PATH`; as validações usam o Temurin 21 em
  `/tmp/controlapeso-jdk21.YVQjvw`;
- TalkBack e teclado físico ainda pedem validação humana;
- comparação final com frames depende da recuperação do conector Figma.

## Estado final do aparelho

O APK debug final foi instalado às 12:42 no SM-S908E. Fonte do sistema voltou
a 0,8, rotação automática voltou a estar ativa, tema do app voltou a escuro e
as opções de contraste reforçado/efeitos reduzidos usadas no teste voltaram a
desativadas.
