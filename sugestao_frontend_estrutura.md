# Sugestão de estrutura de frontend (HTML/CSS/TypeScript)

Hoje o projeto usa **Thymeleaf**: o HTML é gerado no servidor pelo Spring Boot. Para
trabalhar com HTML/CSS/TypeScript puros, a recomendação é separar as duas camadas:

- **Backend** vira uma **API REST** que devolve JSON (`/api/contatos`)
- **Frontend** vira um projeto independente na pasta `frontend/`, que consome essa API
  com `fetch`

Essa separação é o padrão de mercado e evita misturar a compilação do TypeScript com o
build do Maven.

## Estrutura de pastas recomendada

```
agenda-contatos/
├── src/                          # backend Spring Boot (já existe)
│   └── main/java/com/agenda/agenda_contatos/
│       ├── controller/
│       │   ├── ContatoController.java       # páginas Thymeleaf (pode manter ou remover depois)
│       │   └── ContatoRestController.java   # NOVO: API REST que devolve JSON
│       ├── model/
│       └── repository/
├── frontend/                     # NOVO: projeto frontend independente
│   ├── index.html                # página da lista de contatos
│   ├── form.html                 # página do formulário (novo/editar)
│   ├── package.json              # dependências (typescript, vite)
│   ├── tsconfig.json             # configuração do compilador TypeScript
│   ├── vite.config.ts            # dev server + proxy para o backend
│   ├── src/
│   │   ├── main.ts               # lógica da página de listagem
│   │   ├── form.ts               # lógica do formulário
│   │   ├── api.ts                # funções fetch (GET/POST/PUT/DELETE em /api/contatos)
│   │   └── types.ts              # interface Contato { id, nome, telefone, email }
│   └── styles/
│       └── style.css             # estilos (migrar o CSS que hoje está nos templates)
├── pom.xml
└── mvnw
```

## Por que essa estrutura

- **`frontend/` na raiz, fora de `src/`** — o Maven não tenta compilar nada ali, e o
  frontend tem seu próprio ciclo de vida (instalar, compilar, rodar) sem tocar no Java.
- **`src/` (do frontend) só com TypeScript** — o navegador não executa `.ts`; é preciso
  compilar para JavaScript. O [Vite](https://vitejs.dev) faz isso automaticamente e ainda
  fornece um servidor de desenvolvimento com recarga automática. Alternativa minimalista:
  usar só `tsc --watch` e referenciar o `.js` gerado no HTML, sem nenhuma dependência além
  do compilador.
- **`api.ts` separado de `main.ts`/`form.ts`** — concentra toda a comunicação HTTP num
  lugar só; as páginas só chamam funções como `listarContatos()` e `salvarContato(c)`.
- **`types.ts`** — a interface `Contato` espelha a entidade Java e dá ao TypeScript a
  checagem de tipos sobre o JSON da API.

## Mudanças necessárias no backend

1. **Criar `ContatoRestController.java`** com `@RestController` e
   `@RequestMapping("/api/contatos")`, expondo:
   - `GET /api/contatos` — lista todos
   - `GET /api/contatos/{id}` — busca um (retornar **404** se não existir, aproveitando
     para corrigir o erro 500 já identificado no `orElseThrow()`)
   - `POST /api/contatos` — cria
   - `PUT /api/contatos/{id}` — atualiza
   - `DELETE /api/contatos/{id}` — remove (corrige também o problema de deletar via GET)
2. **Liberar CORS em desenvolvimento**, pois o frontend roda em outra porta
   (ex.: 5173 do Vite) — `@CrossOrigin(origins = "http://localhost:5173")` no controller,
   ou configurar o proxy do Vite apontando `/api` para `http://localhost:8080` (preferível,
   pois dispensa CORS).

## Fluxo de desenvolvimento

```bash
# Terminal 1 — backend
./mvnw spring-boot:run                  # API em http://localhost:8080

# Terminal 2 — frontend (primeira vez: instalar Node via 'sudo pacman -S nodejs npm')
cd frontend
npm install                             # baixa typescript + vite (primeira vez)
npm run dev                             # páginas em http://localhost:5173
```

Para "produção" local, `npm run build` gera os arquivos estáticos finais em
`frontend/dist/`; eles podem ser copiados para `src/main/resources/static/` para que o
próprio Spring Boot sirva tudo numa porta só.

## Alternativa mais simples (sem Node/Vite)

Se quiser evitar qualquer ferramenta JavaScript, dá para colocar HTML/CSS/JS direto em
`src/main/resources/static/` e compilar o TypeScript manualmente com `tsc`. Funciona,
mas você perde recarga automática e o fluxo fica menos confortável — para aprender
TypeScript de verdade, a estrutura com `frontend/` + Vite é a recomendada.
