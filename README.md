Portal Fullstack (Backend + Frontend + MySQL)
Este projeto é um monorepo que integra o backend (Spring Boot), o frontend (Angular + Nginx) e o MySQL usando Docker Compose.

🚀 Estrutura do projeto
/portal-full/ ├── backend/ # API Spring Boot │ ├── src/ │ ├── Dockerfile │ └── .env # variáveis específicas do backend ├── frontend/ # Frontend Angular (serviço via Nginx) │ ├── src/ │ ├── Dockerfile │ └── nginx.conf ├── docker-compose.yml # orquestra os serviços ├── .env # variáveis globais (MySQL, volumes) └── README.md

⚙️ Pré-requisitos
Docker
Docker Compose
📌 Configuração de variáveis de ambiente

1. .env (na raiz)
   Arquivo obrigatório para configurar banco de dados e storage.

# ======================

# Configurações globais

# ======================

# MySQL

DB_NAME=portal_db
DB_USER=portal_user
DB_PASSWORD=portal_secret

# Caminho para volume de storage do backend

STORAGE_PATH=/app/storage

2. backend/.env

Arquivo usado pelo Spring Boot.

# ======================

# Configurações do backend

# ======================

# Datasource

SPRING_DATASOURCE_URL=jdbc:mysql://mysql:3306/${DB_NAME}
SPRING_DATASOURCE_USERNAME=${DB_USER}
SPRING_DATASOURCE_PASSWORD=${DB_PASSWORD}

# Storage interno (montado pelo docker-compose)

STORAGE_PATH=/app/storage

# Perfil ativo

SPRING_PROFILES_ACTIVE=prod

▶️ Como rodar

Na raiz do projeto, execute:

docker-compose build
docker-compose up -d

🌍 Endpoints

Frontend (Angular + Nginx):
👉 http://localhost:86

Backend (Spring Boot):
👉 http://localhost:8082

Banco MySQL:
👉 localhost:3307 (usuário/senha conforme .env)

🔗 Integração

O frontend está configurado para usar environment.apiUrl = '/api'.

O Nginx redireciona chamadas /api/... para o backend (http://api:8082).

O backend se conecta ao MySQL pelo host mysql na rede interna do Docker Compose.

🛠️ Comandos úteis

Ver logs dos containers:

docker-compose logs -f

Reconstruir tudo do zero:

docker-compose down -v
docker-compose build --no-cache
docker-compose up -d

Acessar o MySQL dentro do container:

docker exec -it mysql mysql -uportal_user -pportal_secret portal_db

📌 Observações

Todos os serviços compartilham a mesma rede app-network.

Se precisar expor em outro host/IP, basta ajustar o docker-compose.yml (port mapping).

O backend roda sempre no perfil prod (--spring.profiles.active=prod).
