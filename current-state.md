<!-- markdownlint-disable MD013 -->

# CURRENT STATE

## Projeto: OfflineNotes

App Android de notas offline-first.

## Status: Alpha (v0.4.1)

O app é funcional para o ciclo básico de notas (CRUD).

## O que funciona

- [x] Seleção de pasta raiz via SAF.
- [x] Listagem recursiva e busca de arquivos .md e .org.
- [x] Edição com auto-save e modo visualização (Preview).
- [x] Gerenciamento de Tags (armazenadas no DataStore).
- [x] Seleção múltipla para deleção e tags em lote.
- [x] Temas personalizados e modo escuro.
- [x] Syntax highlighting para listas e blocos de código (Markdown/Org).
- [x] Tela de ajuda com versão, build date, autor e links oficiais.
- [x] Tema "Obsidianite" com preview especializado para Org mode.
- [x] Modos de agrupamento: Por Tag, Por Pasta, Por Tipo.
- [x] Filtro rápido por tipo de arquivo: Todos, Org, Markdown.
- [x] Caminho relativo exibido nos cards de notas.
- [x] Persistência das preferências de agrupamento e filtro.
- [x] Filtros movidos para drawer menu (interface mais limpa).
- [x] Backup de tags para arquivo (recuperação após atualizações).
- [x] Restauração automática de tags do backup se DataStore vazio.

## Roadmap

### v0.4 — Explorer + Organização ✅

- [x] Modo de agrupamento na lista: Por Tag (atual), Por Pasta, Por Tipo
- [x] Filtro rápido por tipo: Todos, Org, Markdown
- [x] Mostrar caminho relativo nos cards
- [x] Persistência das preferências de agrupamento e filtro

### v0.4.1 — UI Cleanup + Tags Resilience ✅

- [x] Mover filtros para drawer menu (interface principal mais limpa)
- [x] Backup de tags para arquivo na pasta do usuário
- [x] Auto-restauração de tags do backup se DataStore estiver vazio

### v0.5 — Editor-first + Navegação Redesenhada

- [ ] Nova home abre diretamente no editor da última nota editada
- [ ] Explorer em painel lateral (tablet) ou bottom sheet modal (telefone)
- [ ] Explorador mostra: Árvore de pastas, Arquivos por pasta, Filtro .org/.md
- [ ] Navegação rápida entre arquivos sem sair do editor

## Proximos passos (v0.5)

- [ ] Nova home abre diretamente no editor da última nota editada
- [ ] Explorer em painel lateral (tablet) ou bottom sheet modal (telefone)
- [ ] Explorador mostra: Árvore de pastas, Arquivos por pasta, Filtro .org/.md
- [ ] Navegação rápida entre arquivos sem sair do editor

## Riscos e Pendências

- **Persistência SAF**: Garantir que as permissões persistíveis não expirem ou sejam tratadas graciosamente.
- **UI de Navegação**: Atualmente usa navegação linear/dropdown, com hamburger menu.
- **Sync**: A aba de Sync é apenas um placeholder informativo.
- **Performance**: A listagem de arquivos via SAF pode ser lenta em pastas com muitos arquivos; cache ou paginação podem ser necessários no futuro.
