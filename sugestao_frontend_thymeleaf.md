# Sugestão: frontend mais robusto com Thymeleaf

O projeto já tem um frontend Thymeleaf funcional (`index.html` e `form.html`). Este guia
mostra como evoluí-lo mantendo a renderização no servidor — sem Node, sem build de
JavaScript: o Spring Boot continua servindo tudo sozinho em `localhost:8080`.

## Estrutura de pastas recomendada

Tudo continua dentro de `src/main/resources/`:

```
src/main/resources/
├── templates/                      # páginas Thymeleaf (renderizadas no servidor)
│   ├── layout.html                 # NOVO: esqueleto comum (head, menu, rodapé)
│   ├── contatos/                   # NOVO: agrupar templates por funcionalidade
│   │   ├── lista.html              # substitui o index.html atual
│   │   └── form.html
│   └── error/                      # NOVO: páginas de erro personalizadas
│       ├── 404.html
│       └── 500.html
├── static/                         # arquivos servidos como estão (sem processamento)
│   ├── css/
│   │   └── app.css                 # NOVO: CSS extraído dos <style> dos templates
│   ├── js/
│   │   └── app.js                  # JS leve (confirmação de delete, máscaras etc.)
│   └── img/
└── application.properties
```

Pontos-chave dessa organização:

- **`static/` é servido direto pelo Spring Boot** — `static/css/app.css` fica acessível
  em `http://localhost:8080/css/app.css`. No template:
  `<link rel="stylesheet" th:href="@{/css/app.css}">`.
- **Renomear `index.html` para `contatos/lista.html`** resolve de quebra o bug já
  identificado: o Spring Boot registra qualquer `templates/index.html` como página
  inicial automática e a renderiza sem passar pelo controller (tabela sempre vazia em
  `/`). Sem um template chamado `index`, isso não acontece mais. Adicione no controller
  um redirect da raiz: `@GetMapping("/")` → `return "redirect:/contatos";`.

## 1. Layout único com fragmentos (DRY)

Hoje o `<head>`, o CSS e a estrutura se repetem em cada página. Crie um `layout.html`
com fragmentos reutilizáveis:

```html
<!-- templates/layout.html -->
<head th:fragment="head(titulo)">
    <meta charset="UTF-8">
    <title th:text="${titulo}">Agenda</title>
    <link rel="stylesheet" th:href="@{/css/app.css}">
</head>

<nav th:fragment="menu">
    <a th:href="@{/contatos}">Contatos</a>
    <a th:href="@{/contatos/novo}">Novo</a>
</nav>
```

E nas páginas, em vez de repetir tudo:

```html
<head th:replace="~{layout :: head('Agenda de Contatos')}"></head>
<body>
    <nav th:replace="~{layout :: menu}"></nav>
    ...
</body>
```

(Para projetos maiores existe o `thymeleaf-layout-dialect`, mas fragmentos nativos
resolvem bem aqui.)

## 2. Validação de formulário com mensagens de erro

Hoje dá para salvar contatos totalmente vazios. Combine **Bean Validation** com o
suporte do Thymeleaf a erros de campo:

1. Adicionar a dependência `spring-boot-starter-validation` no `pom.xml`.
2. Anotar a entidade:

```java
@NotBlank(message = "O nome é obrigatório")
private String nome;

@Email(message = "Email inválido")
private String email;
```

3. No controller, receber com `@Valid` e devolver ao formulário se houver erros:

```java
@PostMapping("/salvar")
public String salvar(@Valid @ModelAttribute("contato") Contato contato,
                     BindingResult result) {
    if (result.hasErrors()) {
        return "contatos/form";   // reexibe o form com os erros
    }
    repository.save(contato);
    return "redirect:/contatos";
}
```

4. No template, exibir o erro junto ao campo:

```html
<input type="text" th:field="*{nome}"
       th:classappend="${#fields.hasErrors('nome')} ? 'input-erro'"/>
<span class="erro" th:if="${#fields.hasErrors('nome')}" th:errors="*{nome}"></span>
```

## 3. Mensagens de feedback (flash messages)

Depois de salvar ou deletar, mostre uma confirmação na listagem usando
`RedirectAttributes`:

```java
@PostMapping("/salvar")
public String salvar(..., RedirectAttributes ra) {
    repository.save(contato);
    ra.addFlashAttribute("mensagem", "Contato salvo com sucesso!");
    return "redirect:/contatos";
}
```

```html
<div class="alerta sucesso" th:if="${mensagem}" th:text="${mensagem}"></div>
```

## 4. Deletar com POST (corrige o problema do GET)

Deletar via link GET é inseguro (pré-carregamento de link já apaga o registro). Troque o
link por um mini-formulário:

```html
<form th:action="@{/contatos/deletar/{id}(id=${contato.id})}" method="post"
      onsubmit="return confirm('Deseja deletar?')" style="display:inline">
    <button type="submit" class="btn btn-deletar">Deletar</button>
</form>
```

E no controller, `@GetMapping("/deletar/{id}")` vira `@PostMapping("/deletar/{id}")`.

## 5. Tratar id inexistente (corrige o erro 500)

No `editar`, em vez de `orElseThrow()` seco:

```java
@GetMapping("/editar/{id}")
public String editar(@PathVariable Long id, Model model, RedirectAttributes ra) {
    var contato = repository.findById(id);
    if (contato.isEmpty()) {
        ra.addFlashAttribute("mensagem", "Contato não encontrado.");
        return "redirect:/contatos";
    }
    model.addAttribute("contato", contato.get());
    return "contatos/form";
}
```

## 6. Refinamentos da listagem

- **Lista vazia**: `th:if="${#lists.isEmpty(contatos)}"` para exibir "Nenhum contato
  cadastrado" em vez de uma tabela vazia.
- **Busca por nome**: método `findByNomeContainingIgnoreCase(String nome)` no repository
  + um `<form method="get">` com campo de busca + parâmetro
  `@RequestParam(required = false) String busca` no controller.
- **Paginação**: trocar `findAll()` por `findAll(Pageable)` (o `JpaRepository` já
  suporta) e renderizar os links de página com `th:each` sobre
  `${page.totalPages}`.
- **Páginas de erro personalizadas**: qualquer template em `templates/error/404.html` e
  `templates/error/500.html` é usado automaticamente pelo Spring Boot — sem configuração.

## Ordem sugerida de implementação

1. Extrair CSS para `static/css/app.css` e criar o `layout.html` (ganho imediato, zero risco)
2. Renomear `index.html` → `contatos/lista.html` + redirect na raiz (corrige o bug do `/`)
3. Deletar via POST e tratamento do id inexistente (corrige os dois problemas de execução)
4. Validação com `@Valid` + `th:errors`
5. Flash messages
6. Busca, paginação e páginas de erro

Cada passo funciona de forma independente — dá para fazer um por vez e testar com
`./mvnw spring-boot:run` entre eles.
