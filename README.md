# Sistema de Gestao de Transporte

Sistema web fullstack para gestao de transporte institucional. O projeto reune backend Spring Boot, frontend Angular e banco MySQL, com autenticacao JWT, controle de perfis, cadastros operacionais, fichas de solicitacao, dashboard e relatorios em PDF/Excel.

## Visao Geral

O sistema centraliza o fluxo de transporte da instituicao, permitindo administrar usuarios, veiculos, motoristas, setores, destinos, solicitacoes e fichas. Tambem oferece uma central de relatorios com filtros e exportacao.

Principais recursos:

- Autenticacao e autorizacao com JWT.
- Perfis de acesso: `ADMIN`, `SUPERVISOR`, `GERENTE` e `BASIC`.
- CRUD de usuarios, carros, motoristas, setores e destinos.
- Gestao de solicitacoes e fichas com multiplas solicitacoes.
- Dashboard administrativo com indicadores.
- Relatorios de solicitacoes (agrupados por setor, motorista, veiculo, destino ou usuario), motoristas, carros, setores e destinos.
- Relatorio de producao por usuario: quantidade de fichas e de solicitacoes criadas por usuario em um periodo, para comprovacao de producao de quem digitou os registros.
- Exportacao de relatorios em PDF e Excel.
- Frontend Angular servido por Nginx em producao.
- Backend Spring Boot com Flyway e MySQL.

## Stack

Backend:

- Java 21
- Spring Boot 3.5.x
- Spring Security
- Spring Data JPA / Hibernate
- Flyway
- MySQL
- JasperReports 7
- Maven

Frontend:

- Angular 20
- TypeScript
- SCSS
- Bootstrap / Bootstrap Icons
- Angular Material
- ApexCharts / Chart.js
- Nginx

Infra:

- Docker
- Docker Compose
- Rede Docker externa `otymus_net`

## Estrutura do Projeto

```text
gestao-transporte/
|-- backend/
|   |-- src/main/java/com/br/sistema/
|   |   |-- autenticacao/
|   |   |-- controllers/
|   |   |-- entities/
|   |   |-- exceptions/
|   |   |-- relatorio/
|   |   |-- repositories/
|   |   |-- services/
|   |   `-- utils/
|   |-- src/main/resources/
|   |   |-- db/migration/
|   |   |-- reports/
|   |   |-- application.properties
|   |   `-- application-prod.properties
|   |-- Dockerfile
|   `-- pom.xml
|-- frontend/
|   |-- src/
|   |-- Dockerfile
|   |-- nginx.conf
|   |-- package.json
|   `-- angular.json
|-- docker-compose.yml
|-- docker-compose.dev.yml
`-- README.md
```

## Modulos

- Dashboard: indicadores, cards de resumo e atalhos.
- Usuarios: cadastro, edicao, perfis e credenciais.
- Carros: cadastro e consulta de veiculos.
- Motoristas: cadastro, consulta e relatorios.
- Setores: cadastro, consulta e relatorios.
- Destinos: cadastro, consulta e relatorios.
- Solicitacoes: acompanhamento, status, motorista, setor, destino e quilometragem.
- Fichas de solicitacao: agrupamento de multiplas solicitacoes em uma ficha.
- Relatorios: filtros e exportacao em PDF/Excel, incluindo relatorio de producao por usuario (fichas e solicitacoes).
- Sobre: informacoes institucionais e tecnicas do sistema.

## Configuracao

Crie ou ajuste o arquivo `.env` na raiz do projeto. Exemplo:

```env
# Backend
APP_NAME=sistema
SERVER_PORT=8080
SPRING_PROFILES_ACTIVE=prod
JWT_SECRET=troque-esta-chave-por-uma-chave-segura

# Database
DB_HOST=mysql_server
DB_PORT=3306
DB_NAME=db_nutran
DB_USER=root
DB_PASSWORD=root

# JPA / Flyway
JPA_DDL_AUTO=none
SHOW_SQL=false
FLYWAY_ENABLED=true
LOG_LEVEL=INFO
SECURITY_LOG_LEVEL=INFO
BEAN_LOG_LEVEL=INFO
```

Observacoes:

- Em producao, troque `JWT_SECRET` por uma chave forte.
- O `docker-compose.yml` espera que exista uma rede Docker externa chamada `otymus_net`.
- O banco MySQL usado em Docker deve estar acessivel pelo host configurado em `DB_HOST`, por exemplo `mysql_server`.

## Executando com Docker

1. Crie a rede externa, se ela ainda nao existir:

```bash
docker network create otymus_net
```

2. Suba os servicos:

```bash
docker compose up --build -d
```

3. Acesse:

- Frontend: `http://localhost:85`
- Backend pela rede interna: `api:8080`
- Backend exposto localmente, usando o compose dev: `http://localhost:8080`

Para expor tambem a API localmente, use:

```bash
docker compose -f docker-compose.yml -f docker-compose.dev.yml up --build -d
```

Comandos uteis:

```bash
docker compose logs -f
docker compose logs -f api
docker compose logs -f frontend
docker compose down
docker compose up --build -d
```

## Executando Localmente

### Backend

Requisitos:

- JDK 21
- Maven
- MySQL

Comandos:

```bash
cd backend
mvn spring-boot:run
```

Configuracao local padrao em `backend/src/main/resources/application.properties`:

- Porta: `8080`
- Banco: `jdbc:mysql://localhost:3306/db_nutran`
- Usuario: `root`
- Senha: `root`

### Frontend

Requisitos:

- Node.js 20 ou superior
- npm

Comandos:

```bash
cd frontend
npm install --legacy-peer-deps
npm start
```

O Angular sobe por padrao em:

```text
http://localhost:4200
```

Build de producao:

```bash
cd frontend
npm run build
```

## Relatorios

Os relatorios ficam em:

```text
backend/src/main/resources/reports/
```

O projeto usa JasperReports. Os relatorios de solicitacoes sao carregados a partir dos arquivos `.jrxml`, para garantir que alteracoes de layout sejam refletidas sem depender de arquivos `.jasper` antigos.

Configuracoes importantes:

```properties
net.sf.jasperreports.compiler.class=net.sf.jasperreports.jdt.JRJdtCompiler
net.sf.jasperreports.compiler.java=net.sf.jasperreports.jdt.JRJdtCompiler
```

Isso permite compilar `.jrxml` usando o compilador JDT do Jasper, sem depender de um executavel `javac` disponivel no container em runtime.

Padroes atuais dos relatorios de solicitacoes:

- Cabecalho principal e titulos de colunas aparecem somente na primeira pagina.
- Datas das tabelas aparecem no formato `dd-MM-yyyy`, exemplo `01-01-2025`.
- Relatorios disponiveis: simples, por setor, por motorista, por carro e por destino.

## Banco de Dados

As migrations ficam em:

```text
backend/src/main/resources/db/migration/
```

O Flyway esta habilitado por padrao. Em producao, a flag pode ser controlada por:

```env
FLYWAY_ENABLED=true
```

## Seguranca

- Autenticacao baseada em JWT.
- Controle de rotas e endpoints por perfil.
- Token enviado pelo frontend nas chamadas autenticadas.
- Nginx encaminha chamadas `/api/` para o backend no container `api`.

Perfis principais:

- `ADMIN`: acesso completo, incluindo usuarios, roles, redefinicao administrativa de senha e exclusoes.
- `SUPERVISOR`: acesso operacional equivalente ao ADMIN, exceto usuarios, roles, redefinicao administrativa de senha e exclusoes.
- `GERENTE`: acesso aos modulos operacionais permitidos e relatorios.
- `BASIC`: acesso operacional restrito.

## Endpoints e Proxy

Em Docker, o frontend e servido pelo Nginx na porta `85`.

Chamadas iniciadas pelo frontend para:

```text
/api/
```

sao redirecionadas pelo Nginx para:

```text
http://api:8080
```

## Testes e Validacao

Backend:

```bash
cd backend
mvn test
```

Frontend:

```bash
cd frontend
npm test
```

Builds:

```bash
cd backend
mvn clean package -DskipTests

cd ../frontend
npm run build
```

## Problemas Comuns

### `Cannot run program "javac"` ao gerar relatorio

Verifique se a dependencia `jasperreports-jdt` esta instalada e se as properties do Jasper apontam para:

```properties
net.sf.jasperreports.jdt.JRJdtCompiler
```

### Backend nao conecta no MySQL em Docker

Confira:

- A rede `otymus_net` existe.
- O container do MySQL esta na mesma rede.
- `DB_HOST` aponta para o nome correto do container MySQL.
- Usuario, senha e banco existem.

### API nao acessa pelo navegador em Docker

O `docker-compose.yml` principal expoe somente o frontend. Para expor o backend em `localhost:8080`, use tambem `docker-compose.dev.yml`.

## Autor

Fabio de Alencar Rocha
