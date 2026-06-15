# Sugestões de melhorias — funcionalidades de baixo esforço

Lista de funcionalidades que se encaixam direto na arquitetura atual
(entidade `Contato` + `ContatoRepository` + `ContatoController` + Thymeleaf), sem precisar
dos refactors maiores (camada de service, DTOs etc.). Cada uma reaproveita um padrão que
o projeto já usa.

## Visão geral

| Funcionalidade | Esforço | Por que é fácil aqui |
|---|---|---|
| Mensagens de sucesso | Muito baixo | Flash message já existe (usado no `editar`) e a `<div class="alerta">` já está na `lista.html` |
| Ordenação alfabética | Muito baixo | Trocar `findAll()` por `findAll(Sort.by("nome"))` |
| Favoritar contato ⭐ | Baixo | Campo boolean (o `ddl-auto` cria a coluna) + botão de alternar reaproveitando o POST→redirect do delete |
| Páginas de erro 404/500 | Muito baixo | Basta criar os templates em `templates/error/` — o Spring Boot usa automaticamente |
| Mais campos no contato | Baixo | Adicionar campos na entidade + inputs no form; `LocalDate` ensina `@DateTimeFormat` |
| Exportar contatos em CSV | Baixo–médio | Endpoint com `ResponseEntity` + `Content-Disposition`, sem dependência nova |

## Detalhamento

### 1. Mensagens de sucesso ("Contato salvo/excluído")
Fecha uma lacuna de UX que já está quase pronta.
- No `salvar` e no `deletar`, receber `RedirectAttributes` e chamar
  `ra.addFlashAttribute("mensagem", "Contato salvo com sucesso!")` antes do redirect.
- A `lista.html` já exibe `${mensagem}` na `<div class="alerta">` — nada a fazer na view.

### 2. Ordenação alfabética da listagem
- Trocar `repository.findAll()` por `repository.findAll(Sort.by("nome"))` no `listar`
  (e usar a mesma ordenação na busca, se quiser).

### 3. Favoritar contato ⭐
- Entidade: adicionar `private boolean favorito;` (+ getter/setter). O `ddl-auto=update`
  cria a coluna automaticamente.
- Controller: novo `@PostMapping("/contatos/{id}/favoritar")` que inverte o valor e salva,
  seguindo o mesmo padrão POST→redirect do delete.
- View: um botão/estrela na coluna de ações; opcionalmente ordenar favoritos primeiro
  (`Sort.by(Sort.Order.desc("favorito"), Sort.Order.asc("nome"))`).

### 4. Páginas de erro personalizadas (404 / 500)
- Criar `src/main/resources/templates/error/404.html` e `error/500.html`. O Spring Boot
  os utiliza automaticamente — zero configuração.

### 5. Mais campos no contato
- Sugestões: data de nascimento (`LocalDate` + `@DateTimeFormat`), endereço, observações.
- Adicionar os campos na entidade `Contato` e os inputs correspondentes em `form.html`
  (e colunas em `lista.html` se quiser exibi-los). `LocalDate` ensina formatação de data
  no Thymeleaf (`#temporals`).

### 6. Exportar contatos em CSV
- A mais "impressionante" para uma apresentação, ainda com esforço modesto.
- Novo endpoint `@GetMapping("/contatos/exportar")` que monta o CSV e retorna um
  `ResponseEntity<String>` (ou byte[]) com cabeçalhos
  `Content-Type: text/csv` e `Content-Disposition: attachment; filename="contatos.csv"`.
- Não exige dependência nova. Versão posterior: importar CSV com `MultipartFile`.

## Ordem recomendada (melhor retorno pelo esforço)

1. **Mensagens de sucesso** — rápida e completa o que já existe.
2. **Ordenação alfabética** — praticamente uma linha.
3. **Favoritos** — pequena, satisfatória, ensina bem o ciclo POST→redirect.
4. **Páginas de erro 404/500** — polimento de baixo custo.
5. **Mais campos** / **Exportar CSV** — quando quiser algo mais visível ou "de app real".

> Documento relacionado: `sugestao_melhoras.md` (roadmap maior, em 3 níveis de
> dificuldade, incluindo relacionamentos, paginação e Spring Security).
