# CLAUDE.md

Este arquivo fornece orientação ao Claude Code (claude.ai/code) ao trabalhar com código neste repositório.

## Visão geral do projeto

Sistema de Gestão de Transporte: aplicação fullstack (backend Spring Boot + frontend Angular + MySQL) para gestão de transporte institucional — usuários, veículos (carros), motoristas, setores, destinos, solicitações, fichas de solicitação, dashboard e relatórios em PDF/Excel/CSV.

## Comandos

### Backend (`backend/`)

```bash
cd backend
mvn spring-boot:run          # roda localmente, porta 8080
mvn test                     # roda os testes
mvn clean package -DskipTests
```

Configuração local padrão (`backend/src/main/resources/application.properties`): MySQL em `localhost:3306/db_nutran`, usuário/senha `root`/`root`, segredo JWT fixo (somente para dev).

### Frontend (`frontend/`)

```bash
cd frontend
npm install --legacy-peer-deps   # obrigatório — há conflito de peer deps sem essa flag
npm start                        # ng serve, porta 4200
npm run build                    # build de produção → dist/
npm test                         # Karma/Jasmine
```

### Docker (stack completa)

```bash
docker network create otymus_net   # uma vez só, rede externa que o compose espera
docker compose up --build -d
```

- Frontend (Nginx): `http://localhost:85`
- A API não é exposta pelo compose principal. Para expor também em `localhost:8080`, use o override de dev:
  `docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build -d`
- O MySQL é um container externo pré-existente (`mysql_server`) na rede `otymus_net` — não é iniciado pelo compose deste projeto. `DB_HOST` no `.env` precisa apontar para o nome desse container.

## Arquitetura

### Fluxo de requisição

Navegador → Nginx (`frontend/nginx.conf`, porta 80 no container / 85 no host) serve a SPA Angular compilada e faz proxy reverso de `/api/*` para o container `api` (`http://api-transporte:8080` internamente, resolvido via DNS interno do Docker em `127.0.0.11`, para o proxy sobreviver a reinícios de container). O roteamento da SPA cai em `index.html` para qualquer caminho que não seja um arquivo.

### Organização dos pacotes do backend (`backend/src/main/java/com/br/sistema/`)

- `controllers/` — endpoints REST, um por domínio (Carro, Motorista, Setor, Destino, Solicitacao, FichaSolicitacao, Usuario, Role, Dashboard, Authentication, Logout, RecuperarSenha).
- `services/` — regras de negócio, um por domínio, espelhando os controllers.
- `repositories/` — repositórios Spring Data JPA.
- `entities/` — entidades JPA, agrupadas por pasta de domínio, cada uma geralmente com subpasta `DTO` para os formatos de request/response.
- `autenticacao/` — autenticação JWT: `SecurityConfigurations` (stateless, resource server JWT, allowlist de CORS, BCrypt), `JpaUserDetailsService`, `SessionTrackingFilter`/`SessionTracker` (rastreia sessões ativas), `CustomTokenClaims`.
- `relatorio/` — módulo de relatórios, estruturado como um mini pacote em camadas próprio (`controller/`, `Services/`, `repository/`, `DTO/`, `enums/`), além de `JasperConfig`/`JasperPrecompiler`/`JasperUtils` para integração com o JasperReports.

### Modelo de autorização

Perfis: `ADMIN` (acesso completo, incluindo gestão de usuários/roles e redefinição administrativa de senha), `SUPERVISOR` (mesmo acesso operacional do ADMIN, exceto gestão de usuários/roles, redefinição administrativa de senha e exclusões), `GERENTE` (módulos operacionais + relatórios, sem administração de usuários), `BASIC` (acesso operacional restrito). O JWT carrega os perfis em uma claim `roles`, mapeada para authorities do Spring com o prefixo `ROLE_`. Ver `.claude/SKILLS/perfis/SKILL.md` para a matriz completa de permissões por módulo/ação.

### Migrations do banco de dados (Flyway)

As migrations ficam em `backend/src/main/resources/db/migration/`, aplicadas automaticamente na inicialização (`spring.flyway.enabled=true`).

**Nunca editar uma migration já aplicada (atualmente V1–V9).** O Flyway valida checksums na inicialização; editar uma migration depois que ela já rodou em algum lugar quebra a inicialização em todo ambiente onde ela já foi aplicada, com um erro como:

```
Migration checksum mismatch for migration version N
```

Isso já aconteceu na prática (o V1 foi editado após o deploy) e exigiu reparar manualmente o `flyway_schema_history.checksum` em todo ambiente já implantado. Qualquer mudança de schema ou dado inicial deve virar uma migration nova (`V10__...`, `V11__...`, etc.), nunca em uma já existente.

### Relatórios (JasperReports)

Os templates de relatório ficam em `backend/src/main/resources/reports/` como `.jrxml` (compilados em tempo de execução, não a partir de binários `.jasper` desatualizados), para que mudanças de layout tenham efeito sem uma etapa de compilação separada. Isso depende do `jasperreports-jdt` + das properties do compilador JDT em `application.properties`:

```properties
net.sf.jasperreports.compiler.class=net.sf.jasperreports.jdt.JRJdtCompiler
net.sf.jasperreports.compiler.java=net.sf.jasperreports.jdt.JRJdtCompiler
```

Isso evita depender de um executável `javac` disponível no container em runtime. As datas dos relatórios usam o formato `dd-MM-yyyy`; cabeçalho e títulos de coluna aparecem somente na primeira página.

O relatório de Solicitação (`/api/solicitacao/relatorio`, `RelatorioSolicitacaoService`) tem variantes de agrupamento via o enum `TipoRelatorioSolicitacao` (SIMPLES, POR_SETOR, POR_MOTORISTA, POR_CARRO, POR_DESTINO, POR_USUARIO), cada uma mapeada para um `.jrxml` próprio no mapa `RELATORIOS` do service. Há também um relatório dedicado de Produção por Usuário (`/api/producao/relatorio`, `RelatorioProducaoService`), que soma por usuário a quantidade de fichas e de solicitações criadas num período — usado para comprovação de produção de quem digitou os registros. Diferente dos demais, esse não é um agrupamento visual de uma lista: é uma consulta agregada própria (`FichaSolicitacaoRepository.buscarProducaoPorUsuario`, `GROUP BY` por usuário, filtrando por `FichaSolicitacao.dataCriacao` — a data em que o registro foi digitado, não a data da viagem).

### Organização do frontend (`frontend/src/app/`)

Angular 20 com standalone components. `components/` tem uma pasta por módulo/funcionalidade (dashboard, usuario, carro, motorista, setor, destino, solicitacao, ficha, relatorios, login, sobre, layout-admin, etc.), além de `services/`, `guards/`, `pipes/`, `models/`, `enums/`. `features/admin/` concentra código de funcionalidades administrativas, separado da árvore geral de `components/`.

## Skills relacionadas do projeto

- `.claude/SKILLS/perfis/SKILL.md` — a matriz de permissões oficial (perfil × módulo × ação).
- `.claude/commands/security-arch.md` — notas de arquitetura de segurança deste projeto.
