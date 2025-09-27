--
-- Script de Migração do Banco de Dados para o Gerenciador de Arquivos Web
--
-- NOTA: Este script é idempotente. Ele utiliza 'CREATE TABLE IF NOT EXISTS' para
-- evitar erros caso as tabelas já existam.
--

-- Criação da tabela de roles
CREATE TABLE IF NOT EXISTS tb_roles (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        nome VARCHAR(255) NOT NULL UNIQUE
    );



-- Criação da tabela de usuários
-- Adicionado 'data_criacao', 'data_atualizacao' e 'nome_completo'
CREATE TABLE IF NOT EXISTS tb_usuarios (
   id BIGINT AUTO_INCREMENT PRIMARY KEY,
   username VARCHAR(255) NOT NULL UNIQUE,
    password VARCHAR(255) NOT NULL,
    senha_provisoria BOOLEAN NOT NULL DEFAULT FALSE,
    nome_completo VARCHAR(150) NOT NULL,
    data_criacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_atualizacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6) ON UPDATE CURRENT_TIMESTAMP(6)
    );

-- Criação da tabela de relacionamento entre usuários e roles
CREATE TABLE IF NOT EXISTS tb_usuarios_roles (
                                                 user_id BIGINT NOT NULL,
                                                 role_id BIGINT NOT NULL,
                                                 PRIMARY KEY (user_id, role_id),
    FOREIGN KEY (user_id) REFERENCES tb_usuarios(id) ON DELETE CASCADE,
    FOREIGN KEY (role_id) REFERENCES tb_roles(id) ON DELETE CASCADE
    );

-- Tabela de Pastas
CREATE TABLE IF NOT EXISTS tb_pasta (
                                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                        nome_pasta VARCHAR(255) NOT NULL,
    caminho_completo VARCHAR(1024) NOT NULL,
    data_criacao DATETIME(6) NOT NULL,
    data_atualizacao DATETIME(6) NOT NULL,
    is_publico BOOLEAN NOT NULL DEFAULT TRUE,
    pasta_pai_id BIGINT,
    criado_por_id BIGINT,
    CONSTRAINT fk_subpastas_pasta_pai FOREIGN KEY (pasta_pai_id) REFERENCES tb_pasta (id),
    CONSTRAINT fk_pastas_usuarios FOREIGN KEY (criado_por_id) REFERENCES tb_usuarios (id)
    );

-- Tabela de Arquivos
CREATE TABLE IF NOT EXISTS tb_arquivo (
                                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                          nome_arquivo VARCHAR(255) NOT NULL,
    caminho_armazenamento VARCHAR(1024) NOT NULL,
    tamanho_bytes BIGINT,
    data_upload DATETIME(6) NOT NULL,
    data_atualizacao DATETIME(6) NOT NULL,
    is_publico BOOLEAN NOT NULL DEFAULT TRUE,
    hash_arquivo VARCHAR(64),
    tipo_mime VARCHAR(100),
    pasta_id BIGINT,
    criado_por_id BIGINT,
    CONSTRAINT fk_arquivos_pastas FOREIGN KEY (pasta_id) REFERENCES tb_pasta (id),
    CONSTRAINT fk_arquivos_usuarios FOREIGN KEY (criado_por_id) REFERENCES tb_usuarios (id)
    );

-- Tabela de relacionamento entre usuários e as pastas principais que eles podem acessar
CREATE TABLE IF NOT EXISTS tb_permissao_pasta (
                                                  usuario_id BIGINT NOT NULL,
                                                  pasta_id BIGINT NOT NULL,
                                                  PRIMARY KEY (usuario_id, pasta_id),
    FOREIGN KEY (usuario_id) REFERENCES tb_usuarios(id),
    FOREIGN KEY (pasta_id) REFERENCES tb_pasta(id)
    );

--
-- Inserção de dados iniciais
--

-- Inserção de roles
INSERT INTO tb_roles (nome) VALUES
                                ('ADMIN'),
                                ('BASIC'),
                                ('GERENTE')
    ON DUPLICATE KEY UPDATE nome = nome;

-- Inserção de usuários
INSERT INTO tb_usuarios (username, password, senha_provisoria, nome_completo, data_criacao, data_atualizacao) VALUES
   ('admin','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'Administrador do Sistema','2025-09-17 10:34:32.322224','2025-09-17 14:05:11.524568'),
   ('1373269','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'JESANA ADORNO SOARES COSTA','2025-09-17 10:34:32.322224','2025-09-17 10:59:00.003140'),
   ('1305158','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'EUREUDES RODRIGUES SANTOS','2025-09-17 10:34:32.322224','2025-09-18 11:39:35.680566'),
   ('14329301','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'Fabio de Alencar Rocha','2025-09-17 10:40:04.777697','2025-09-17 14:11:47.130158'),
   ('14407965','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'OSIEL ALEX FERREIRA PACHECO','2025-09-17 11:01:46.391264','2025-09-17 11:01:46.391264'),
   ('17116538','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'Ana Karoliny Couto Nascimento','2025-09-17 11:03:09.183948','2025-09-17 11:03:09.183948'),
   ('16821181','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'MARCIA CAVALCANTI DA SILVA','2025-09-17 11:03:39.015709','2025-09-17 11:03:39.015709'),
   ('1896474','$$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'LAILA SILVA GONÇALVES','2025-09-17 11:04:12.150431','2025-09-17 11:04:12.150431'),
   ('14430827','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'MARIO EDUARDO BILL PRIMO','2025-09-17 11:04:33.813962','2025-09-17 11:04:33.813962'),
    ('16734726','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'PRISCILA SPINDOLA DA COSTA SIMPLICIO','2025-09-17 11:05:09.524326','2025-09-17 11:05:09.524326'),
	 ('16578384','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'ALINE MARIA CAMPOS DE MELO','2025-09-17 11:05:42.973927','2025-09-17 11:05:42.973927'),
	 ('1440516','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'14405016','2025-09-17 11:06:24.350051','2025-09-17 11:06:24.350051'),
	 ('1827979','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'HELLEM AGUIAR RAMOS','2025-09-17 11:06:55.382892','2025-09-17 11:06:55.382892'),
	 ('14439360','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'LUCINA BRAZ LEITE','2025-09-17 11:07:30.504324','2025-09-17 11:07:30.504324'),
	 ('1893459','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'THIAGO GUIMARES FONSECA','2025-09-17 11:07:52.354715','2025-09-17 11:07:52.354715'),
	 ('13165322','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'ELIZETE VIDAL SANTOS SILVA','2025-09-17 11:08:13.734488','2025-09-17 11:08:13.734488'),
	 ('16595181','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'DANYELLE PINHEIRO VERISSIMO ','2025-09-17 11:08:45.071626','2025-09-17 11:08:45.071626'),
	 ('16711505','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'AGHATA CRISTIAN GONTIJO BRITO DE ASSIS ZEREFOSS','2025-09-17 11:10:08.921318','2025-09-17 11:10:08.921318'),
	 ('1529056','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'IVO ALVARO ALVES DE SOUSA','2025-09-17 11:10:34.659678','2025-09-17 11:10:34.659678'),
('1436395X','$$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'JULY EWELLIN HUCOMUSON DUTRA','2025-09-17 11:11:10.255911','2025-09-17 11:11:10.255911'),
	 ('14354241','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'LUIZA FERNANDA DUARTE ROQUE','2025-09-17 11:11:29.470434','2025-09-17 11:11:29.470434'),
	 ('1965247','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'MIRIAN COSTA E SILVA','2025-09-17 11:12:19.989529','2025-09-17 11:12:19.989529'),
	 ('14339323','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'NEIDE APARECIDA RAMOS ROCHA','2025-09-17 11:12:37.967276','2025-09-17 11:12:37.967276'),
	 ('16737520','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'JESSICA DE AREA LEAO SILVA','2025-09-17 11:13:12.579876','2025-09-17 11:13:12.579876'),
	 ('1546961','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'MAYANE SANTANA DE OLIVEIRA','2025-09-17 11:13:33.038719','2025-09-17 11:13:33.038719'),
	 ('17178975','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'HIGOR ALENCAR DOS SANTOS','2025-09-17 11:13:58.543051','2025-09-17 11:13:58.543051'),
	 ('16616928','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'ANNA CLAUDIA LEAL','2025-09-17 11:14:18.097471','2025-09-17 11:14:18.097471'),
	 ('17170214','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'PRISCILA FONSECA CESAR','2025-09-17 11:14:39.601061','2025-09-17 11:14:39.601061'),
	 ('16735412','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'NAYARA MOTA CARDOSO FERREIRA','2025-09-17 11:14:56.800812','2025-09-17 11:14:56.800812'),
    ('16797760','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'ENOQUE DE SOUZA','2025-09-17 11:15:14.285040','2025-09-17 11:15:14.285040'),
	 ('1470167','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'GIRLENE FERREIRA AGUIAR','2025-09-17 11:15:32.417876','2025-09-17 11:15:32.417876'),
	 ('1715658','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'JAQUELINE DOS SANTOS CALDAS COSTA','2025-09-17 11:15:50.574665','2025-09-17 11:15:50.574665'),
	 ('1724819','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'HELBER GUEDES LEMOS GOMES','2025-09-17 11:16:10.632080','2025-09-17 11:16:10.632080'),
	 ('1516914','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'SANDRA MARIA AZEVEDO MARÇAL','2025-09-17 11:16:31.016978','2025-09-17 11:16:31.016978'),
	 ('16585895','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'TANAJARA OTILIA BENECH OLIVEIRA','2025-09-17 11:16:51.688152','2025-09-17 11:16:51.688152'),
	 ('1726536','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'VANISIA MOREIRA DANTAS DE SOUSA','2025-09-17 11:17:11.333309','2025-09-17 11:17:11.333309'),
	 ('14405016','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'LIDIA FERREIRA DA SILVA CESAR','2025-09-17 11:17:29.292656','2025-09-17 11:17:29.292656'),
	 ('16614755','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'ISLA MARIA DE OLIVEIRA BRAGA','2025-09-17 11:18:14.165926','2025-09-17 11:18:14.165926'),
	 ('1711599X','$2a$10$RuPJRDA5waAaFUazCqHR6OjEXV89x3Rx47RKlT8R/0.JiubM6RZz6',0,'HUDSON DE JESUS RIBEIRO','2025-09-17 11:18:45.277258','2025-09-17 11:18:45.277258')


    ON DUPLICATE KEY UPDATE
                         password = VALUES(password),
                         nome_completo = VALUES(nome_completo);

-- Inserção de relacionamento usuário ↔ role
INSERT INTO tb_usuarios_roles (user_id, role_id) VALUES
                                                     (1,1),
                                                     (2,1),
                                                     (3,3),
                                                     (4,3),
                                                     (5,3),
                                                     (6,3),
                                                     (7,3),
                                                     (8,3),
                                                     (9,3),
                                                     (10,3),
                                                     (11,3),
                                                     (12,3),
                                                     (13,3),
                                                     (14,3),
                                                     (15,3),
                                                     (16,3),
                                                     (17,3),
                                                     (18,3),
                                                     (19,3),
                                                     (20,3),
                                                     (21,3),
                                                     (22,3),
                                                     (23,3),
                                                     (24,3),
                                                     (25,3),
                                                     (26,3),
                                                     (27,3),
                                                     (28,3),
                                                     (29,3),
                                                     (30,3),
                                                     (31,3),
                                                     (32,3),
                                                     (33,3),
                                                     (34,3),
                                                     (35,3),
                                                     (36,3),
                                                     (37,3),
                                                     (38,3),
                                                     (39,3)

ON DUPLICATE KEY UPDATE user_id = user_id;

-- Criação da tabela de auditoria de usuarios logados
CREATE TABLE tb_login_audit (
                                id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                username VARCHAR(100) NOT NULL,
                                data_login TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);