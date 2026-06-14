# Sugestões de melhoria — Agenda de Contatos

O projeto hoje é um CRUD mínimo: 1 entidade (`Contato` com id, nome, telefone, email),
1 controller com 5 rotas, 1 repository sem métodos customizados e 2 templates Thymeleaf.
Este documento lista funcionalidades para torná-lo mais robusto e complexo, organizadas
em três níveis de dificuldade, com o que cada uma ensina do ecossistema Spring.

> **Antes de começar**: aplicar as correções dos dois bugs já identificados —
> o erro 500 em `/contatos/editar/{id}` com id inexistente (`orElseThrow()` seco) e o
> delete via GET. Várias melhorias abaixo tocam exatamente esses pontos do controller.
> Os detalhes estão em `sugestao_frontend_thymeleaf.md` (seções 4 e 5).

## Nível 1 — Enriquecer o domínio (sem conceitos novos)

| Funcionalidade | O que ensina |
|---|---|
| Mais campos no `Contato` | `LocalDate` + `@DateTimeFormat`, formatação com `#temporals` no Thymeleaf |
| Busca por nome | Métodos derivados do Spring Data (`findByNomeContainingIgnoreCase`) |
| Paginação e ordenação | `Pageable`/`Page` no repository e links de página no template |
| Validação | `@NotBlank`, `@Email`, `@Past` + `@Valid`/`BindingResult` + `th:errors` |
| Favoritos | Campo boolean + botão de alternar na listagem |

Campos sugeridos: data de nascimento (`LocalDate`), endereço, observações, `favorito`.

## Nível 2 — Relacionamentos entre entidades (o salto mais valioso)

É aqui que o projeto deixa de ser trivial, porque passa a modelar dados de verdade.

### Categorias/grupos (recomendado começar por esta)
Nova entidade `Categoria` (Família, Trabalho, Amigos...) com `@ManyToOne` no `Contato`.
Ensina: relacionamento JPA, `<select>` no Thymeleaf populado do banco, filtro da
listagem por categoria.

### Múltiplos telefones por contato
Entidade `Telefone` (número + tipo: celular/casa/trabalho) com
`@OneToMany(cascade = ALL, orphanRemoval = true)`. Notavelmente mais difícil que o
`@ManyToOne`: exige binding de listas dinâmicas no formulário
(`th:field="*{telefones[__${i}__].numero}"`) com botão "adicionar telefone" em
JavaScript. Excelente exercício.

### Aniversariantes do mês
Consulta com `@Query` (JPQL) filtrando pelo mês da data de nascimento + destaque na
listagem ou página própria. Ensina JPQL além dos métodos derivados.

## Nível 3 — Funcionalidades de aplicação real

### Exportar/importar vCard (.vcf)
O formato padrão de contatos — dá para exportar e importar no celular de verdade.
Ensina: download com `ResponseEntity` + `Content-Disposition`, upload com
`MultipartFile`. Versão mais simples para começar: CSV.

### Foto do contato
Upload de imagem (`MultipartFile`), salvando em `uploads/` no disco e servindo na
listagem. Ensina validação de tipo/tamanho de arquivo.

### Lixeira (soft delete)
Em vez de apagar, marcar `deletado = true`; página "Lixeira" com restaurar/excluir
definitivo. Ensina a customizar todas as consultas para excluir registros marcados.

### Auditoria
Campos `criadoEm`/`atualizadoEm` automáticos com `@EnableJpaAuditing` +
`@CreatedDate`/`@LastModifiedDate`.

### Login com Spring Security
Cada usuário vê apenas os próprios contatos (`Contato` ganha `@ManyToOne Usuario`).
Maior peso em portfólio, mas também maior complexidade — deixar por último, quando o
resto estiver sólido.

### Testes automatizados
`@DataJpaTest` para as consultas customizadas e `@WebMvcTest` + MockMvc para os
controllers. Em projeto acadêmico, ter testes diferencia muito.

## Ordem recomendada (melhor aprendizado por esforço)

1. Correção dos bugs existentes (500 no editar, delete via GET)
2. Busca por nome + paginação
3. Categorias com `@ManyToOne`
4. Validação completa
5. Múltiplos telefones com `@OneToMany`
6. Exportação vCard/CSV
7. (Depois) foto, lixeira, auditoria, Spring Security, testes

Os passos 2–6 já transformam o projeto em algo que demonstra domínio do ecossistema —
relacionamentos, consultas customizadas, binding complexo e manipulação de arquivos —
sem depender do salto para Spring Security.

## Documentos relacionados

- `sugestao_frontend_thymeleaf.md` — evolução do frontend atual (layout, validação,
  flash messages, páginas de erro)
- `sugestao_frontend_estrutura.md` — alternativa com frontend separado em
  HTML/CSS/TypeScript consumindo API REST
