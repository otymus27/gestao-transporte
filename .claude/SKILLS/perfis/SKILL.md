---
name: perfil
description: Use essa skill quando o usuário querer criar, editar ou excluir algum perfil ou perfis para alguma funcionalidade do projeto.
Skill de Arquitetura: RBAC Final — Sistema Gestão Transporte
Atue como arquiteto de software sênior. Implemente as funcionalidades seguindo esta matriz de acessos validada em produção.
---

## 1. Matriz Final de Permissões

| Módulo / Recurso          | ADMIN                          | GERENTE                            | SUPERVISOR                         | BASIC                     |
|---------------------------|--------------------------------|------------------------------------|------------------------------------|---------------------------|
| Dashboard                 | Visualiza                      | Visualiza                          | Visualiza                          | Visualiza                 |
| Solicitações              | CRUD + Excluir + Status        | Criar / Editar / Alterar Status    | Criar / Editar / Alterar Status    | Criar / Editar / Listar   |
| Fichas                    | CRUD                           | CRUD                               | CRUD                               | CRUD                      |
| Carros                    | CRUD                           | Só Leitura                         | Criar / Editar / Listar            | Bloqueado                 |
| Destinos                  | CRUD                           | Só Leitura                         | Criar / Editar / Listar            | Bloqueado                 |
| Motoristas                | CRUD                           | Criar / Editar / Listar            | Criar / Editar / Listar            | Bloqueado                 |
| Setores                   | CRUD                           | Só Leitura                         | Criar / Editar / Listar            | Bloqueado                 |
| Relatórios                | Total                          | Total                              | Total                              | Bloqueado                 |
| Usuários                  | CRUD                           | Só Leitura (listagem)              | Bloqueado                          | Bloqueado                 |
| Roles                     | CRUD                           | Bloqueado                          | Bloqueado                          | Bloqueado                 |
| Alterar Senha (própria)   | Sim                            | Sim                                | Sim                                | Sim                       |
| Redefinir Senha (outros)  | Sim (gerar senha temporária)   | Bloqueado                          | Bloqueado                          | Bloqueado                 |

---

## 2. Regras de Backend (@PreAuthorize)

### Autenticação (`/api`)
```java
POST /api/login   → permitAll()
POST /api/logout  → permitAll()
```

### Dashboard (`/api/dashboard`)
```java
GET /api/dashboard   // isAuthenticated() — proteção global, sem @PreAuthorize explícito
```

### Carros (`/api/carros`)
```java
@PostMapping               @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
@PutMapping("/{id}")       @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
@DeleteMapping("/{id}")    @PreAuthorize("hasRole('ADMIN')")
@GetMapping                @PreAuthorize("hasAnyRole('ADMIN','GERENTE','SUPERVISOR','BASIC')")
@GetMapping("/{id}")       @PreAuthorize("hasAnyRole('ADMIN','GERENTE','SUPERVISOR','BASIC')")
@GetMapping("/buscar")     // isAuthenticated() — sem @PreAuthorize explícito
```

### Destinos (`/api/destino`)
```java
@PostMapping               @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
@PatchMapping("/{id}")     @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
@DeleteMapping("/{id}")    @PreAuthorize("hasRole('ADMIN')")
@GetMapping /**            // isAuthenticated() — sem @PreAuthorize explícito
```

### Motoristas (`/api/motorista`)
```java
@PostMapping               @PreAuthorize("hasAnyRole('ADMIN','GERENTE','SUPERVISOR')")
@PutMapping("/{id}")       @PreAuthorize("hasAnyRole('ADMIN','GERENTE','SUPERVISOR')")
@DeleteMapping("/{id}")    @PreAuthorize("hasRole('ADMIN')")
@GetMapping /**            // isAuthenticated() — sem @PreAuthorize explícito
```

### Setores (`/api/setor`)
```java
@PostMapping               @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
@PutMapping("/{id}")       @PreAuthorize("hasAnyRole('ADMIN','SUPERVISOR')")
@DeleteMapping("/{id}")    @PreAuthorize("hasRole('ADMIN')")
@GetMapping /**            // isAuthenticated() — sem @PreAuthorize explícito
```

### Solicitações (`/api/solicitacao`)
```java
@PostMapping                      @PreAuthorize("hasAnyRole('ADMIN','GERENTE','SUPERVISOR','BASIC')")
@PatchMapping("/{id}")            @PreAuthorize("hasAnyRole('ADMIN','GERENTE','SUPERVISOR','BASIC')")
@DeleteMapping("/{id}")           @PreAuthorize("hasRole('ADMIN')")
@PatchMapping("/{id}/status")     @PreAuthorize("hasAnyRole('ADMIN','GERENTE','SUPERVISOR')")
@GetMapping /**                   // isAuthenticated() — sem @PreAuthorize explícito
```

### Fichas (`/api/fichas`)
```java
@PostMapping               @PreAuthorize("hasAnyRole('ADMIN','GERENTE','SUPERVISOR','BASIC')")
@PatchMapping("/{id}")     @PreAuthorize("hasAnyRole('ADMIN','GERENTE','SUPERVISOR','BASIC')")
@GetMapping                @PreAuthorize("hasAnyRole('ADMIN','GERENTE','SUPERVISOR','BASIC')")
@GetMapping("/{id}")       @PreAuthorize("hasAnyRole('ADMIN','GERENTE','SUPERVISOR','BASIC')")
@GetMapping("/buscar")     @PreAuthorize("hasAnyRole('ADMIN','GERENTE','SUPERVISOR','BASIC')")
@DeleteMapping("/{id}")    @PreAuthorize("hasRole('ADMIN')")
```

### Usuários (`/api/usuario`)
```java
@PostMapping               @PreAuthorize("hasRole('ADMIN')")
@GetMapping                @PreAuthorize("hasAnyRole('ADMIN','GERENTE')")
@GetMapping("/{id}")       @PreAuthorize("hasRole('ADMIN')")
@PatchMapping("/{id}")     @PreAuthorize("hasRole('ADMIN')")
@DeleteMapping("/{id}")    @PreAuthorize("hasRole('ADMIN')")
@GetMapping("/logado")     @PreAuthorize("isAuthenticated()")
```

### Roles (`/api/role`)
```java
// Todos os endpoints: @PreAuthorize("hasRole('ADMIN')")
GET    /api/role
GET    /api/role/{id}
POST   /api/role
PATCH  /api/role/{id}
DELETE /api/role/{id}
```

### Recuperar Senha (`/api/recuperar`)
```java
@PostMapping("/gerar-senha")      @PreAuthorize("hasRole('ADMIN')")
@PostMapping("/redefinir-senha")  @PreAuthorize("hasAnyRole('ADMIN','GERENTE','SUPERVISOR','BASIC')")
@PutMapping("/alterar-senha")     @PreAuthorize("hasAnyRole('ADMIN','GERENTE','SUPERVISOR','BASIC')")
```

### Relatórios (`/api/*/relatorio/**` e `/api/relatorio/**`)
```java
// Nenhum endpoint de relatório possui @PreAuthorize explícito.
// Proteção feita apenas via isAuthenticated() global + guard de rota no frontend.
GET /api/solicitacao/relatorio/{consultar|pdf|excel}
GET /api/carros/relatorio/{consultar|pdf|excel}
GET /api/destino/relatorio/{consultar|pdf|excel}
GET /api/motorista/relatorio/{consultar|pdf|excel}
GET /api/setor/relatorio/{consultar|pdf|excel}
GET /api/relatorio/solicitacoes-por-dia
GET /api/relatorio/solicitacoes-por-setor
GET /api/relatorio/solicitacoes-por-motorista
```

---

## 3. Regras de Frontend (Angular)

### Guard disponível (`auth.guard.ts`)
```typescript
// Um único guard (CanActivateFn) com lógica:
// 1. Não autenticado → redireciona para /login
// 2. Rota com data.roles → verifica se authService.getLoggedInRoles()
//    contém ao menos um dos roles exigidos
// 3. Role inválida → redireciona para /admin/dashboard, retorna false
// 4. Sem data.roles → qualquer autenticado passa
authGuard  // aplicado em TODAS as rotas protegidas
```

### Rotas e seus roles exigidos (`app.routes.ts`)

| Rota                              | Roles permitidos                        |
|-----------------------------------|-----------------------------------------|
| `/admin/dashboard`                | Qualquer autenticado (sem data.roles)   |
| `/admin/carros`                   | ADMIN, GERENTE, SUPERVISOR              |
| `/admin/carros/novo`              | ADMIN, SUPERVISOR                       |
| `/admin/carros/relatorio`         | ADMIN, GERENTE, SUPERVISOR              |
| `/admin/destinos`                 | ADMIN, GERENTE, SUPERVISOR              |
| `/admin/destinos/relatorio`       | ADMIN, GERENTE, SUPERVISOR              |
| `/admin/motoristas`               | ADMIN, GERENTE, SUPERVISOR              |
| `/admin/motoristas/relatorio`     | ADMIN, GERENTE, SUPERVISOR              |
| `/admin/setores`                  | ADMIN, GERENTE, SUPERVISOR              |
| `/admin/setores/relatorio`        | ADMIN, GERENTE, SUPERVISOR              |
| `/admin/solicitacoes`             | ADMIN, GERENTE, SUPERVISOR, BASIC       |
| `/admin/solicitacoes/gerenciar`   | ADMIN, GERENTE, SUPERVISOR              |
| `/admin/solicitacoes/relatorio`   | ADMIN, GERENTE, SUPERVISOR              |
| `/admin/fichas`                   | ADMIN, GERENTE, SUPERVISOR, BASIC       |
| `/admin/fichas/gerenciar`         | ADMIN, GERENTE, SUPERVISOR, BASIC       |
| `/admin/relatorios`               | ADMIN, GERENTE, SUPERVISOR              |
| `/admin/usuarios`                 | ADMIN                                   |
| `/admin/usuarios/gerenciar`       | ADMIN                                   |
| `/admin/perfil/alterar-senha`     | ADMIN, GERENTE, SUPERVISOR, BASIC       |
| `/admin/sobre`                    | ADMIN, GERENTE, SUPERVISOR, BASIC       |

### Visibilidade de Menus (Sidebar)

| Item de Menu       | Condição `*ngIf`                                       |
|--------------------|--------------------------------------------------------|
| Dashboard          | Sempre visível                                         |
| **Cadastros**      |                                                        |
| Usuários           | `isAdmin()`                                            |
| Setores            | `isAdmin() \|\| isGerente() \|\| isSupervisor()`       |
| Destinos           | `isAdmin() \|\| isGerente() \|\| isSupervisor()`       |
| Motoristas         | `isAdmin() \|\| isGerente() \|\| isSupervisor()`       |
| Carros             | `isAdmin() \|\| isGerente() \|\| isSupervisor()`       |
| **Operações**      |                                                        |
| Solicitações       | `isAdmin() \|\| isGerente() \|\| isSupervisor() \|\| isBasic()` |
| Fichas             | `isAdmin() \|\| isGerente() \|\| isSupervisor() \|\| isBasic()` |
| Relatórios         | `isAdmin() \|\| isGerente() \|\| isSupervisor()`       |
| **Conta**          |                                                        |
| Alterar Senha      | `isLogado()`                                           |
| Sobre              | `isLogado()`                                           |

### Visibilidade de Botões e Ações

| Elemento UI                              | Condição                                               |
|------------------------------------------|--------------------------------------------------------|
| "Nova solicitação" (Dashboard)           | `isAdmin() \|\| isGerente() \|\| isSupervisor()`       |
| "Gerenciar carros" (Dashboard)           | `isAdmin() \|\| isGerente() \|\| isSupervisor()`       |
| "Gerenciar motoristas" (Dashboard)       | `isAdmin() \|\| isGerente() \|\| isSupervisor()`       |
| "Gerenciar setores" (Dashboard)          | `isAdmin() \|\| isGerente() \|\| isSupervisor()`       |
| "Abrir relatórios" (Dashboard)           | Sempre visível (sem condição)                          |
| Botão "Excluir Solicitação"              | `isAdmin()`                                            |
| Botão "Alterar Status" da Solicitação    | `isAdmin() \|\| isGerente() \|\| isSupervisor()`       |
| Botão "Excluir Ficha"                    | `isAdmin()`                                            |
| Botão "Novo Carro" / Editar Carro        | `isAdmin() \|\| isSupervisor()`                        |
| Botão "Excluir Carro"                    | `isAdmin()`                                            |
| Botões Criar/Editar Motorista            | `isAdmin() \|\| isGerente() \|\| isSupervisor()`       |
| Botão "Excluir Motorista"                | `isAdmin()`                                            |
| Botões Criar/Editar Setor/Destino        | `isAdmin() \|\| isSupervisor()`                        |
| Botões Excluir Setor/Destino             | `isAdmin()`                                            |

### Helpers de role (sidebar.component.ts)
```typescript
isAdmin()      // roles.includes('ADMIN')
isGerente()    // roles.includes('GERENTE')
isSupervisor() // roles.includes('SUPERVISOR')
isBasic()      // roles.includes('BASIC')
isLogado()     // roles.length > 0
```

Label do perfil no footer da sidebar:
```typescript
isAdmin()
  ? 'Administrador'
  : isGerente()
    ? 'Gerente'
    : isSupervisor()
      ? 'Supervisor'
      : 'Usuário'
```

---

## 4. Atenção: Regras Críticas de Implementação

### Interceptor HTTP Global
O interceptor `auth.interceptor.ts` injeta o JWT em todas as requisições autenticadas.
Respostas `401` redirecionam para `/login`. Respostas `403` redirecionam para `/acesso-negado` ou `/admin/dashboard`.

**Regra:** Nunca chame endpoints restritos de componentes acessíveis por perfis sem permissão. Sempre condicione a chamada HTTP ao role antes de executá-la.

### Relatórios sem @PreAuthorize no backend
Endpoints de relatório não possuem `@PreAuthorize` — a restrição é feita apenas pelo guard de rota no frontend.
Ao adicionar novo endpoint de relatório, inclua `@PreAuthorize("hasAnyRole('ADMIN','GERENTE','SUPERVISOR')")` para consistência com a matriz.

### GETs sem @PreAuthorize explícito
A maioria dos endpoints GET (carros, destinos, setores, motoristas, solicitações) não tem `@PreAuthorize` — a proteção vem do `SecurityFilterChain` que exige autenticação para todos os paths não declarados como `permitAll`. Ao criar endpoints de leitura sensível, adicione `@PreAuthorize` explícito.

### Prefixo ROLE_ no JWT
- Roles armazenadas no claim `roles` com prefixo `ROLE_` (ex.: `ROLE_SUPERVISOR`)
- No `@PreAuthorize`: referenciar sem prefixo — `hasRole('SUPERVISOR')`, `hasAnyRole('ADMIN','SUPERVISOR')`
- No frontend: `AuthService` extrai e normaliza os roles, comparar sem prefixo — `roles.includes('SUPERVISOR')`
