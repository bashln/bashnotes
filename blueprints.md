# BLUEPRINTS

## Estrutura de Pastas e Responsabilidades

- `app/src/main/java/com/offlinenotes/data/`:
  - Camada de persistência.
  - `NotesRepository`: Gerencia I/O de arquivos usando SAF (`DocumentFile`).
  - `SettingsRepository`: Gerencia preferências do usuário e metadados de notas (tags) usando `DataStore`. Inclui backup/restauração de tags para arquivo (`tags_backup.json`).
  - `NoteFileNaming`: Lógica de nomenclatura e extensões.

- `app/src/main/java/com/offlinenotes/domain/`:
  - Modelos de dados puros e regras de negócio independentes de framework.
  - `NoteMeta`: Representação leve de una nota (inclui uri, nome, caminho relativo, tipo).
  - `NoteKind`: Tipos de notas suportados (.md, .org, checklist).
  - `GroupingMode`: Enum para modos de agrupamento (TAG, FOLDER, TYPE).
  - `FileTypeFilter`: Enum para filtros de tipo (ALL, ORG, MARKDOWN).

- `app/src/main/java/com/offlinenotes/ui/`:
  - Componentes de interface usando Jetpack Compose.
  - Subpastas por feature (`notes`, `editor`, `settings`, `sync`, `privacy`, `help`).
  - `theme/`: Definições de cores, tipografia e formas.
    - `ThemeSettings.kt`: Enum `ThemePalette` (TokyoNight, Catppuccin, RosePine, Obsidianite) e `ThemeMode`.
    - `Color.kt`: Tokens de cores por paleta. Obsidianite usa cores escuras (#100e17 bg, #0fb6d6 cyan accent).
    - `Theme.kt`: `OfflineNotesTheme` aplica o ColorScheme baseado na paleta selecionada.
  - `help/`: Tela de ajuda com informações do app (versão, build date, autor, links).
  - `notes/NotesListScreen.kt`: Lista de notas com drawer menu (`ModalNavigationDrawer`) para filtros e agrupamento.

- `app/src/main/java/com/offlinenotes/viewmodel/`:
  - Orquestração de estado da UI e ponte entre `ui` e `data`.
  - Implementa lógica de filtragem, seleção e eventos.

## Hotspots e Acoplamentos

- **`NotesListViewModel`**: Centraliza a lógica de listagem, busca e seleção múltipla. Risco de alta complexidade.
- **`NotesRepository`**: Dependência forte do contexto Android para operações SAF.
- **Navegação**: Definida centralmente em `OfflineNotesApp.kt`.
- **Editor**: Usa `VisualTransformation` para syntax highlighting em tempo real sem alterar o conteúdo do arquivo. Otimizado com `Regex.findAll(MULTILINE)` para evitar split linha a linha.
- **`NotePreview.kt`**: Renderização de preview com dupla implementação:
  - Padrão (Standard): Cards com Material Design para todos os temas exceto Obsidianite+Org.
  - Obsidianite: Layout "limpo" sem cards, hierarquia visual com barras laterais e cores específicas do tema (cyan accent #0fb6d6, dim blue #45aaff).
  - Suporta blocos: Heading, Checklist, Bullet, CodeBlock, Paragraph com estilização inline (bold, italic, code, links).

## Decisões de Arquitetura (ADRs Implícitas)

### Tema Obsidianite
- **Escopo**: Paleta dark-only inspirada no tema Obsidianite (bennyxguo/Obsidian-Obsidianite).
- **Design**: Aplica estilo "Obsidian-like" exclusivamente ao preview de Org mode; Markdown mantém aparência padrão.
- **Implementação**: 
  - Tokens de cor definidos em `Color.kt` (background-primary #100e17, accent cyan #0fb6d6, etc.)
  - Routing condicional em `NotePreviewContent()` baseado em `palette == ThemePalette.Obsidianite && isOrg`
  - Sem CompositionLocal adicional - parâmetros explícitos mantêm testabilidade
- **Racional**: Evita over-engineering; mudanças localizadas; preview org ganha identidade visual distinta sem afetar outros temas.

## Invariantes

- Arquivos em disco são a única fonte de verdade para o conteúdo das notas.
- SAF é o único meio de acesso ao armazenamento externo.
- Nenhuma dependência de rede no fluxo de notas.
- Preview deve funcionar para ambos formatos (isOrg=true/false) sem regressão.

---

## v0.4.1 Implementation Summary

### Filter Menu Drawer
Filtros e busca movidos para `ModalNavigationDrawer` que desliza da direita:
- `NotesListScreen.kt`: Envolvido em `ModalNavigationDrawer` com `ModalDrawerSheet`
- Conteúdo do drawer: campo de busca, chips de agrupamento (Tag/Pasta/Tipo), chips de filtro (Todos/Org/Markdown)
- Botão de filtro na TopBar abre o drawer (`Icons.Default.FilterList`)
- Interface principal mais limpa, sem controles sempre visíveis

### Tags Backup/Restore
Persistência resiliente de tags via arquivo JSON:
- `SettingsRepository.kt`: Métodos `backupTagsToFile()` e `restoreTagsFromBackup()`
- Backup escrito em `tags_backup.json` na pasta raiz selecionada
- Restauração automática na inicialização se DataStore vazio mas backup existe
- TagEntry serializado como JSON (uri, tag, timestamp)
- `NotesListViewModel.kt`: Trigger de backup async após alterações de tags
