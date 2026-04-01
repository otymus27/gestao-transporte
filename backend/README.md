# Sistema de Gestão de Transporte

Sistema web para gerenciamento de transporte institucional, com controle de usuários, carros, motoristas, setores, destinos, solicitações, fichas de solicitação, relatórios e dashboard administrativo.

## Visão geral

O projeto foi desenvolvido para centralizar e organizar o fluxo de transporte institucional em uma única plataforma, oferecendo cadastro, consulta, acompanhamento operacional, relatórios e controle de acesso por perfil.

A aplicação possui frontend em Angular e backend em Spring Boot, utilizando autenticação baseada em JWT e persistência em banco de dados relacional.

## Principais objetivos

* Centralizar a gestão de transporte em uma plataforma única.
* Padronizar cadastros e processos operacionais.
* Facilitar o acompanhamento de solicitações e fichas.
* Disponibilizar relatórios para apoio administrativo.
* Garantir segurança com autenticação e autorização por perfil.
* Entregar uma interface moderna, clara e responsiva.

## Funcionalidades

### Dashboard

* Exibição de indicadores principais do sistema.
* Cards com atalhos rápidos para módulos administrativos.
* Resumo operacional do ambiente.
* Acesso direto à central de relatórios.

### Usuários

* Cadastro e gerenciamento de usuários.
* Controle de perfis de acesso.
* Alteração de senha.
* Autenticação com token JWT.

### Carros

* Cadastro e gerenciamento de veículos.
* Consulta e manutenção de registros.
* Integração com relatórios.

### Motoristas

* Cadastro e gerenciamento de motoristas.
* Consulta por filtros.
* Integração com relatórios.

### Setores

* Cadastro e gerenciamento de setores.
* Consulta e manutenção de registros.
* Integração com relatórios.

### Destinos

* Cadastro e gerenciamento de destinos.
* Consulta e manutenção de registros.
* Integração com relatórios.

### Solicitações

* Cadastro e acompanhamento de solicitações.
* Controle de status.
* Associação com motorista, setor e destino.
* Consulta paginada.
* Integração com relatórios.

### Fichas de solicitação

* Criação de fichas com múltiplas solicitações.
* Associação do usuário logado como responsável.
* Atualização de ficha com substituição controlada das solicitações.
* Consulta, filtragem e exclusão.

### Relatórios

* Página centralizada de relatórios.
* Exportação em PDF, Excel e CSV.
* Relatórios por módulo.
* Filtros por entidade.

### Página Sobre

* Apresentação institucional do sistema.
* Informações técnicas do projeto.
* Stack tecnológica.
* Controle de acesso e histórico visual do produto.

## Perfis de acesso

O sistema trabalha com perfis de autorização para restringir funcionalidades conforme o papel do usuário.

### ADMIN

* Controle total do sistema.
* Gerenciamento de usuários.
* Acesso aos módulos administrativos.
* Acesso aos relatórios.
* Ações críticas de manutenção.

### GERENTE

* Acesso aos módulos operacionais e administrativos permitidos.
* Gerenciamento de carros, motoristas, setores, destinos e solicitações.
* Acesso aos relatórios.
* Sem administração completa de usuários.

### BASIC

* Acesso às funcionalidades operacionais permitidas.
* Uso de solicitações e fichas conforme regra do sistema.
* Sem acesso às rotinas administrativas restritas.

## Arquitetura geral

O projeto está dividido em duas aplicações principais:

* **Frontend**: interface web construída em Angular.
* **Backend**: API REST desenvolvida em Spring Boot.

### Frontend

* Angular Standalone Components
* TypeScript
* SCSS
* Bootstrap Icons
* Integração HTTP com API REST
* Controle de autenticação no cliente
* Layout administrativo com sidebar, dashboard e páginas de CRUD

### Backend

* Spring Boot
* Spring Security
* JWT
* Spring Data JPA
* Hibernate
* MySQL
* DTOs para entrada e saída de dados
* Serviços com regras de negócio
* Repositórios para persistência

## Stack utilizada

### Frontend

* Angular
* TypeScript
* HTML
* SCSS
* Bootstrap Icons

### Backend

* Java
* Spring Boot
* Spring Security
* JWT
* Spring Data JPA
* Hibernate

### Banco de dados

* MySQL

### Relatórios

* Exportação em PDF
* Exportação em Excel
* Exportação em CSV

## Estrutura esperada do projeto

```text
projeto/
├── backend/
│   ├── src/main/java/
│   ├── src/main/resources/
│   ├── pom.xml
│   └── Dockerfile
├── frontend/
│   ├── src/
│   ├── angular.json
│   ├── package.json
│   └── Dockerfile
└── docker-compose.yml
```

## Organização do backend

Exemplo conceitual de organização:

```text
backend/src/main/java/com/br/sistema/
├── controllers/
├── services/
├── repositories/
├── entities/
│   ├── Usuario/
│   ├── Carro/
│   ├── Motorista/
│   ├── Setor/
│   ├── Destino/
│   ├── Solicitacao/
│   └── FichaSolicitacao/
├── autenticacao/
├── exceptions/
└── utils/
```

## Organização do frontend

Exemplo conceitual de organização:

```text
frontend/src/app/
├── components/
│   ├── dashboard/
│   ├── sidebar/
│   ├── setores/
│   ├── destinos/
│   ├── motoristas/
│   ├── carros/
│   ├── solicitacoes/
│   ├── fichas/
│   ├── relatorios/
│   └── sobre/
├── services/
├── guards/
├── interceptors/
└── shared/
```

## Requisitos para execução local

### Backend

* Java 17 ou superior
* Maven
* MySQL

### Frontend

* Node.js
* npm
* Angular CLI

## Configuração do backend

1. Criar o banco de dados MySQL.
2. Ajustar as propriedades de conexão.
3. Configurar credenciais e segredo JWT.
4. Executar a aplicação Spring Boot.

Exemplo genérico de propriedades:

```properties
spring.datasource.url=jdbc:mysql://localhost:3306/seu_banco
spring.datasource.username=seu_usuario
spring.datasource.password=sua_senha
spring.jpa.hibernate.ddl-auto=update
jwt.secret=sua_chave_secreta
```

## Execução do backend

```bash
./mvnw spring-boot:run
```

Ou:

```bash
mvn spring-boot:run
```

## Configuração do frontend

1. Instalar dependências.
2. Ajustar a URL base da API, se necessário.
3. Iniciar o servidor Angular.

## Execução do frontend

```bash
npm install
npm start
```

Ou, dependendo do script definido:

```bash
ng serve
```

## Build de produção do frontend

```bash
npm run build
```

## Execução com Docker

Se o projeto estiver configurado com Docker e Docker Compose:

```bash
docker compose up --build
```

Ou:

```bash
docker-compose up --build
```

## Variáveis e configurações importantes

Recomenda-se definir corretamente:

* URL do banco de dados
* usuário e senha do banco
* segredo JWT
* ambiente de execução
* URL base da API no frontend

## Fluxo básico de uso

1. Usuário realiza login.
2. O sistema autentica via JWT.
3. O menu lateral é montado conforme o perfil.
4. O dashboard apresenta indicadores e atalhos.
5. O usuário acessa os módulos disponíveis.
6. Relatórios podem ser gerados na página central de relatórios.

## Regras de navegação

* O sidebar foi simplificado para links diretos por módulo.
* Relatórios foram centralizados em uma única página.
* Os cards do dashboard funcionam como atalhos para os módulos principais.

## Recursos visuais implementados

* Layout administrativo moderno.
* Sidebar simplificada.
* Dashboard com atalhos clicáveis.
* Página Sobre com visual premium.
* Tabelas com melhor hierarquia visual.
* Interface responsiva.

## Boas práticas adotadas

* Separação entre frontend e backend.
* Uso de DTOs para transporte de dados.
* Regras de negócio concentradas em services.
* Persistência delegada a repositories.
* Controle de autenticação e autorização.
* Componentização no frontend.
* Padronização visual da interface.

## Segurança

* Autenticação baseada em JWT.
* Controle de acesso por perfil.
* Restrição de menus e rotas conforme permissões.
* Associação do usuário logado em operações sensíveis, como fichas.

## Relatórios

A aplicação possui uma central de relatórios que reúne os módulos principais, permitindo:

* consulta com filtros
* exportação em PDF
* exportação em Excel
* exportação em CSV

## Possíveis melhorias futuras

* Logs de auditoria por operação.
* Notificações em tempo real.
* Dashboard com gráficos analíticos.
* Histórico detalhado de alterações.
* Testes automatizados frontend e backend.
* Deploy contínuo com pipeline CI/CD.

## Autor

**Fábio de Alencar Rocha**
Desenvolvedor Full Stack

## Licença

Defina aqui a licença adotada pelo projeto, se aplicável.

---

## Exemplo de seção rápida para GitHub

### Como rodar o projeto

```bash
# backend
cd backend
mvn spring-boot:run

# frontend
cd frontend
npm install
npm start
```

### Acesso

Após subir os serviços, acesse a aplicação pelo endereço configurado no frontend.
