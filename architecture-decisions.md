<!-- markdownlint-disable MD013 -->

# ARCHITECTURE DECISIONS

## ADR 1: Uso de SAF (Storage Access Framework) para I/O

- **Contexto**: O app precisa acessar arquivos do usuário no Android sem requerer permissões amplas de armazenamento (MANAGE_EXTERNAL_STORAGE).
- **Decisão**: Utilizar `ACTION_OPEN_DOCUMENT_TREE` para obter acesso a uma árvore de diretórios.
- **Consequência**: Privacidade total, mas performance de I/O inferior a acesso direto via `File` API.

## ADR 2: DataStore para Metadados e Preferências

- **Contexto**: Precisamos armazenar a pasta raiz, o tema e as tags das notas (que não estão no corpo do arquivo).
- **Decisão**: Usar `androidx.datastore:datastore-preferences` em vez de SQLite/Room.
- **Consequência**: Simplicidade e integração nativa com Coroutines/Flow, mas limitado a dados estruturados simples.

## ADR 3: MVVM sem Framework de DI

- **Contexto**: O projeto visa simplicidade máxima e poucas dependências.
- **Decisão**: Usar `ViewModelProvider.Factory` manual para injeção de dependências nos ViewModels.
- **Consequência**: Boilerplate leve nos Factories, mas evita a complexidade de Hilt/Koin em um projeto pequeno.

## ADR 4: Navigation

- **Contexto**: `project.md` exige uma hierarquia clara e minimalista.
- **Decisão**: Toda a navegação principal será baseada em um hamburger menu no canto superior direito.
- **Consequência**: UX previsível e focada.

## ADR 6: Drawer Menu for Filters (v0.4.1)

- **Contexto**: Controles de busca, agrupamento e filtro ocupavam espaço fixo na tela principal, poluindo a visualização.
- **Decisão**: Mover filtros para `ModalNavigationDrawer` deslizante da direita, aberto via botão na TopBar. Gestos desabilitados (`gesturesEnabled = false`) para evitar conflito com scroll da lista.
- **Consequência**: Interface principal mais limpa; filtros acessíveis mas não dominantes; padrão consistente com Material Design 3.

## ADR 5: Conditional Preview Rendering (v0.3.0)

- **Contexto**: A paleta Obsidianite requer um visual "limpo" sem cards para preview Org, diferente do estilo Material Design padrão usado pelos outros temas.
- **Decisão**: Implementar routing condicional em `NotePreview.kt` baseado em `palette == ThemePalette.Obsidianite && isOrg`. Quando ambas condições são verdadeiras, renderiza o estilo Obsidianite; caso contrário, mantém o render padrão com cards.
- **Consequência**:
  - Dupla implementação de render: Standard (cards) vs Obsidianite (clean layout).
  - Parâmetros explícitos (`palette`) passados através da cadeia de composables (`OfflineNotesApp` → `EditorScreen` → `NotePreview`).
  - Sem regressão: Markdown e outros temas continuam funcionando com a aparência existente.
  - Sem abstrações excessivas: Sem CompositionLocal adicional para tokens do preview.
