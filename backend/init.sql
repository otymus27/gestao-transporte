-- Cria o banco se não existir
CREATE DATABASE IF NOT EXISTS db_nutran
  CHARACTER SET utf8mb4
  COLLATE utf8mb4_unicode_ci;

-- Cria o usuário se não existir
CREATE USER IF NOT EXISTS 'otymus27'@'%' IDENTIFIED WITH mysql_native_password BY 'admin271@';

-- Garante que o root também usa mysql_native_password (compatibilidade com drivers)
ALTER USER 'root'@'%' IDENTIFIED WITH mysql_native_password BY 'root';
ALTER USER 'root'@'localhost' IDENTIFIED WITH mysql_native_password BY 'root';

-- Dá privilégios completos no banco db_nutran para o usuário
GRANT ALL PRIVILEGES ON db_nutran.* TO 'otymus27'@'%';

-- Aplica mudanças
FLUSH PRIVILEGES;

