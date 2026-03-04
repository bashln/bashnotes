<!-- markdownlint-disable MD013 -->

# CURRENT STATE

## Projeto: OfflineNotes

App Android de notas offline-first.

## Status: Alpha (v0.2.2)

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

## Proximos passos

- [ ] Melhorias no editor: suporte a tabelas e outras formatações avançadas
- [ ] Considerar debounce no syntax highlighting para arquivos muito grandes
- [ ] Adicionar testes unitários para `SyntaxHighlightingTransformation`

## Riscos e Pendências

- **Persistência SAF**: Garantir que as permissões persistíveis não expirem ou sejam tratadas graciosamente.
- **UI de Navegação**: Atualmente usa navegação linear/dropdown, com hamburger menu.
- **Sync**: A aba de Sync é apenas um placeholder informativo.
- **Performance**: A listagem de arquivos via SAF pode ser lenta em pastas com muitos arquivos; cache ou paginação podem ser necessários no futuro.
