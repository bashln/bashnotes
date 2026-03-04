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
