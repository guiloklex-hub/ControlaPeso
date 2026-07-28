# Auditoria inicial de UI/UX — ControlaPeso

## Escopo analisado

Projeto Android em Kotlin, Jetpack Compose e Material 3, package `br.com.paivalab.controlapeso`.
A auditoria foi feita sobre o código-fonte enviado, sem alterar o projeto.

## Conclusão

O aplicativo está funcional, bem estruturado para um projeto inicial e possui boa cobertura de recursos. O principal problema visual não é falta de funcionalidades, e sim ausência de um **sistema de design completo** e de um **ciclo visual de validação**.

Hoje a UI é predominantemente formada por `Card`, `Text`, `Button`, `OutlinedButton`, `FilterChip` e `FlowRow`, com pouca diferenciação visual entre níveis de importância. Isso costuma produzir uma aparência correta, porém genérica e semelhante a um protótipo técnico.

## Evidências no código

- `ui/theme/Type.kt` personaliza somente `bodyLarge`; os demais estilos usam os padrões do Material.
- `ui/theme/Theme.kt` possui bons esquemas de cor, modo claro/escuro, cor dinâmica e alto contraste, mas não fornece um conjunto próprio de `Shapes` nem tokens de espaçamento, elevação e movimento.
- Foram encontradas aproximadamente 73 utilizações diretas de `Card` nas telas.
- Não foram encontradas funções anotadas com `@Preview`.
- Não foram encontradas animações Compose relevantes (`AnimatedContent`, `animate*`, transições de estado etc.).
- Há apenas uma utilização consistente de `TopAppBar`, concentrada na tela de diagnóstico.
- A iconografia é usada principalmente na navegação; o restante da experiência é muito textual.
- A adaptação de tamanho é feita principalmente por `BoxWithConstraints` e limites manuais de 600/840 dp, sem uma camada central baseada em `WindowSizeClass` ou Material 3 Adaptive.
- Algumas telas e o `NavHost` são grandes e concentram muitas responsabilidades visuais, dificultando iteração e consistência.
- O gráfico é funcional, acessível e leve, mas ainda possui aparência de componente técnico: eixos e seleção simples, pouca hierarquia e ausência de estados visuais mais refinados.
- Os testes existentes verificam funcionalidade, tema escuro, fonte ampliada e alguns layouts expandidos, mas não existe regressão visual por screenshots.

## Pontos fortes que devem ser preservados

- BLE e parser já funcionais.
- Arquitetura separando UI, domínio, dados e Bluetooth.
- Estado via ViewModel/StateFlow.
- Tema claro, escuro, sistema, cor dinâmica e alto contraste.
- Modo de efeitos reduzidos.
- Histórico, metas, perfis, relatórios, backup, Health Connect e lembretes.
- Tratamento cuidadoso de privacidade.
- Testes unitários e instrumentados já existentes.
- Layouts que já possuem alguma adaptação para 840 dp.

## Direção recomendada

### Personalidade do produto

- Calmo
- Confiável
- Privado
- Humano
- Claro
- Premium sem ostentação
- Orientado a progresso, não a culpa
- Tecnológico apenas onde ajuda

### Linguagem visual

- Material 3 como base, com personalidade própria.
- Superfícies limpas e hierarquia forte.
- Um único “hero component” por tela.
- Menos cartões; mais agrupamento por seção e superfícies com níveis distintos.
- Tipografia mais expressiva, sobretudo para peso, variação e títulos.
- Iconografia consistente em ações e listas.
- Bordas, formas e elevações definidas por tokens.
- Movimento funcional e curto.
- Transparência somente em cabeçalhos ou superfícies especiais, nunca como padrão para todos os cartões.
- Cores semânticas para tendência, conexão, sucesso, atenção e erro, sem depender exclusivamente de cor.

## Mudanças de maior impacto

1. Criar um design system local com tokens e componentes reutilizáveis.
2. Criar previews para todas as telas e principais componentes.
3. Gerar dados fake determinísticos para previews.
4. Criar screenshot tests para estados e tamanhos críticos.
5. Redesenhar primeiro Dashboard, Medição ao Vivo, Histórico e Navegação.
6. Depois padronizar formulários, listas, ajustes, relatórios e telas secundárias.
7. Centralizar decisões adaptáveis usando classes de tamanho de janela.
8. Realizar pelo menos duas rodadas de comparação visual após a implementação.

## Critérios para escolher uma direção visual

O agente deve produzir três direções e pontuá-las de 0 a 10:

- Usabilidade e clareza: 25%
- Hierarquia da informação: 20%
- Acessibilidade: 20%
- Consistência e escalabilidade: 15%
- Adaptabilidade: 10%
- Desempenho e simplicidade técnica: 10%

A direção vencedora não deve ser a mais decorativa, e sim a que tiver a melhor pontuação total e menor risco de regressão.

## Ferramentas recomendadas

- Compose `@Preview` e multi-preview.
- Android Studio UI Check.
- Layout Inspector.
- Compose Preview Screenshot Testing, se compatível com a versão atual do projeto.
- Emuladores em 360 dp, 600 dp e 840 dp.
- Figma MCP remoto como ferramenta opcional para explorar e consolidar a direção visual.
- Material 3 Design Kit e Material Theme Builder como fontes de tokens e padrões.

## Resultado esperado

Uma evolução visual profunda sem alterar regras de negócio, banco, BLE, parser, exportações ou privacidade. O app deve parecer um produto finalizado, não apenas um conjunto de telas funcionais.
