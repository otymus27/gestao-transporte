-- =============================================================
-- V5: Integração FichaSolicitacao
-- MySQL compatible — sem IF NOT EXISTS em constraints/indexes
-- =============================================================

-- -------------------------------------------------------------
-- 1. Criar tb_ficha_solicitacao se ainda não existir
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tb_ficha_solicitacao (
                                                    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                    data_viagem      DATE        NOT NULL,
                                                    placa_veiculo    VARCHAR(10) NOT NULL,
    data_criacao     DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_atualizacao DATETIME(6) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    criado_por_id    BIGINT      DEFAULT NULL,
    CONSTRAINT fk_ficha_usuario FOREIGN KEY (criado_por_id) REFERENCES tb_usuarios(id)
    ) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_0900_ai_ci;

-- -------------------------------------------------------------
-- 2. Migrar dados existentes:
--    Cria uma ficha para cada solicitação sem id_ficha,
--    usando data_solicitacao e placa do carro vinculado.
-- -------------------------------------------------------------
INSERT INTO tb_ficha_solicitacao (data_viagem, placa_veiculo, data_criacao, criado_por_id)
SELECT
    s.data_solicitacao,
    COALESCE(c.placa, 'SEM-PLACA'),
    NOW(),
    s.id_usuario
FROM tb_solicitacao s
         LEFT JOIN tb_carro c ON c.id = s.id_carro
WHERE s.id_ficha IS NULL OR s.id_ficha = 0;

-- -------------------------------------------------------------
-- 3. Preencher id_ficha nas solicitações órfãs
-- -------------------------------------------------------------
UPDATE tb_solicitacao s
    JOIN tb_ficha_solicitacao f
ON f.data_viagem   = s.data_solicitacao
    AND f.criado_por_id = s.id_usuario
    SET s.id_ficha = f.id
WHERE s.id_ficha IS NULL OR s.id_ficha = 0;

-- -------------------------------------------------------------
-- 4. Tornar id_ficha NOT NULL
-- -------------------------------------------------------------
ALTER TABLE tb_solicitacao
    MODIFY COLUMN id_ficha BIGINT NOT NULL;

-- -------------------------------------------------------------
-- 5. Garantir FK do id_ficha — verifica se já existe antes
--    MySQL 8+: usa information_schema para checar
-- -------------------------------------------------------------
SET @fk_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME        = 'tb_solicitacao'
      AND CONSTRAINT_NAME   = 'fk_solicitacao_ficha'
      AND CONSTRAINT_TYPE   = 'FOREIGN KEY'
);

SET @sql = IF(@fk_exists = 0,
    'ALTER TABLE tb_solicitacao ADD CONSTRAINT fk_solicitacao_ficha FOREIGN KEY (id_ficha) REFERENCES tb_ficha_solicitacao(id) ON DELETE CASCADE',
    'SELECT 1 -- fk_solicitacao_ficha já existe, nada a fazer'
);

PREPARE stmt FROM @sql;
EXECUTE stmt;
DEALLOCATE PREPARE stmt;

-- -------------------------------------------------------------
-- 6. Remover FK do id_carro se existir — verifica antes
-- -------------------------------------------------------------
SET @fk_carro_exists = (
    SELECT COUNT(*)
    FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME        = 'tb_solicitacao'
      AND CONSTRAINT_NAME   = 'tb_solicitacao_ibfk_1'
      AND CONSTRAINT_TYPE   = 'FOREIGN KEY'
);

SET @sql2 = IF(@fk_carro_exists > 0,
    'ALTER TABLE tb_solicitacao DROP FOREIGN KEY tb_solicitacao_ibfk_1',
    'SELECT 1 -- FK do carro não existe, nada a fazer'
);

PREPARE stmt2 FROM @sql2;
EXECUTE stmt2;
DEALLOCATE PREPARE stmt2;

-- Remover índice do id_carro se existir
SET @idx_exists = (
    SELECT COUNT(*)
    FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'tb_solicitacao'
      AND INDEX_NAME   = 'idx_solicitacao_carro'
);

SET @sql3 = IF(@idx_exists > 0,
    'ALTER TABLE tb_solicitacao DROP INDEX idx_solicitacao_carro',
    'SELECT 1 -- índice idx_solicitacao_carro não existe'
);

PREPARE stmt3 FROM @sql3;
EXECUTE stmt3;
DEALLOCATE PREPARE stmt3;

-- -------------------------------------------------------------
-- Coluna id_carro permanece como dado histórico (sem FK ativa).
-- Remova com V6 após confirmar que nada depende dela.
-- -------------------------------------------------------------