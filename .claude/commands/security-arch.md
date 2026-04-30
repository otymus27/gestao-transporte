# Arquitetura de Segurança — Gestão Transporte

Analise a arquitetura de segurança atual do projeto e gere um relatório completo cobrindo os itens abaixo. Leia os arquivos relevantes antes de responder — não use apenas o que está em memória.

## O que analisar

### 1. Autenticação e Autorização
- Leia `backend/src/main/java/com/br/sistema/autenticacao/` — JWT, SecurityConfigurations, TokenService, JpaUserDetailsService
- Leia `frontend/src/app/guards/` e `frontend/src/app/interceptors/` — guards e interceptores Angular
- Identifique: algoritmo JWT, expiração do token, claims, roles (ADMIN/GERENTE/BASIC), como as roles são verificadas no backend e no frontend

### 2. Proteção dos Endpoints
- Leia os controllers em `backend/src/main/java/com/br/sistema/controllers/`
- Liste quais endpoints são públicos vs. protegidos e com quais roles

### 3. Gestão de Secrets
- Leia `.env`, `backend/.env`, `backend/src/main/resources/application.properties`, `application-prod.properties`
- Identifique credenciais hardcoded, JWT secret, senhas de banco

### 4. Segurança de Rede e Docker
- Leia `docker-compose.yml`, `frontend/nginx.conf`
- Identifique portas expostas, rede Docker, headers HTTP de segurança, CORS

### 5. Armazenamento de Tokens no Frontend
- Leia `frontend/src/app/services/auth.service.ts`
- Verifique onde o JWT é armazenado (localStorage vs. cookie HttpOnly) e como é decodificado

### 6. Logs e Tratamento de Erros
- Leia `application-prod.properties` — nível de log em produção
- Leia `backend/src/main/java/com/br/sistema/exceptions/ApiExceptionHandler.java`

## Formato do relatório

Para cada item acima, entregue:
- **Status atual**: o que está implementado
- **Risco**: CRÍTICO / ALTO / MÉDIO / BAIXO / BOM
- **Recomendação**: ação concreta para corrigir ou manter

Finalize com uma tabela resumo com colunas: Componente | Status | Risco | Ação.

Priorize achados críticos e altos no topo.
