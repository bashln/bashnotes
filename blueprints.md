# BLUEPRINTS

## Estrutura de Pastas e Responsabilidades

- `app/src/main/java/com/offlinenotes/data/`: 
  - Camada de persistência.
  - `NotesRepository`: Gerencia I/O de arquivos usando SAF (`DocumentFile`).
  - `SettingsRepository`: Gerencia preferências do usuário e metadados de notas (tags) usando `DataStore`.
  - `NoteFileNaming`: Lógica de nomenclatura e extensões.

- `app/src/main/java/com/offlinenotes/domain/`:
  - Modelos de dados puros e regras de negócio independentes de framework.
  - `NoteMeta`: Representação leve de uma nota.
  - `NoteKind`: Tipos de notas suportados (.md, .org, checklist).

- `app/src/main/java/com/offlinenotes/ui/`:
  - Componentes de interface usando Jetpack Compose.
  - Subpastas por feature (`notes`, `editor`, `settings`, `sync`, `privacy`, `help`).
  - `theme/`: Definições de cores, tipografia e formas.
  - `help/`: Tela de ajuda com informações do app (versão, build date, autor, links).

- `app/src/main/java/com/offlinenotes/viewmodel/`:
  - Orquestração de estado da UI e ponte entre `ui` e `data`.
  - Implementa lógica de filtragem, seleção e eventos.

## Hotspots e Acoplamentos

- **`NotesListViewModel`**: Centraliza a lógica de listagem, busca e seleção múltipla. Risco de alta complexidade.
- **`NotesRepository`**: Dependência forte do contexto Android para operações SAF.
- **Navegação**: Definida centralmente em `OfflineNotesApp.kt`.
- **Editor**: Usa `VisualTransformation` para syntax highlighting em tempo real sem alterar o conteúdo do arquivo. Otimizado com `Regex.findAll(MULTILINE)` para evitar split linha a linha.

## Invariantes

- Arquivos em disco são a única fonte de verdade para o conteúdo das notas.
- SAF é o único meio de acesso ao armazenamento externo.
- Nenhuma dependência de rede no fluxo de notas.
