-- =============================================================
-- V6: Move id_usuario da tb_solicitacao para tb_ficha_solicitacao
--     e remove id_carro da tb_solicitacao
--
-- Lógica:
--   1. Adiciona id_usuario em tb_ficha_solicitacao
--   2. Preenche id_usuario na ficha com base nas solicitações existentes
--   3. Torna id_usuario NOT NULL na ficha
--   4. Remove FK, índice e coluna id_usuario da tb_solicitacao
--   5. Remove FK, índice e coluna id_carro da tb_solicitacao
-- =============================================================

-- -------------------------------------------------------------
-- 1. Adicionar id_usuario na tb_ficha_solicitacao (se não existir)
-- -------------------------------------------------------------
SET @col_usuario_ficha = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'tb_ficha_solicitacao'
      AND COLUMN_NAME  = 'id_usuario'
);
SET @sql1 = IF(@col_usuario_ficha = 0,
    'ALTER TABLE tb_ficha_solicitacao ADD COLUMN id_usuario BIGINT DEFAULT NULL',
    'SELECT 1'
);
PREPARE s1 FROM @sql1; EXECUTE s1; DEALLOCATE PREPARE s1;

-- -------------------------------------------------------------
-- 2. Preencher id_usuario na ficha a partir da solicitação
--    (pega o id_usuario da primeira solicitação de cada ficha)
-- -------------------------------------------------------------
SET @col_usuario_sol = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'tb_solicitacao'
      AND COLUMN_NAME  = 'id_usuario'
);
SET @sql2 = IF(@col_usuario_sol > 0, '
    UPDATE tb_ficha_solicitacao f
    JOIN (
        SELECT id_ficha, MIN(id_usuario) AS id_usuario
        FROM tb_solicitacao
        WHERE id_usuario IS NOT NULL
        GROUP BY id_ficha
    ) s ON s.id_ficha = f.id
    SET f.id_usuario = s.id_usuario
    WHERE f.id_usuario IS NULL
', 'SELECT 1');
PREPARE s2 FROM @sql2; EXECUTE s2; DEALLOCATE PREPARE s2;

-- -------------------------------------------------------------
-- 3. Adicionar FK de id_usuario na ficha (se não existir)
-- -------------------------------------------------------------
SET @fk_usuario_ficha = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME        = 'tb_ficha_solicitacao'
      AND CONSTRAINT_NAME   = 'fk_ficha_usuario_v6'
      AND CONSTRAINT_TYPE   = 'FOREIGN KEY'
);
SET @sql3 = IF(@fk_usuario_ficha = 0,
    'ALTER TABLE tb_ficha_solicitacao ADD CONSTRAINT fk_ficha_usuario_v6 FOREIGN KEY (id_usuario) REFERENCES tb_usuarios(id)',
    'SELECT 1'
);
PREPARE s3 FROM @sql3; EXECUTE s3; DEALLOCATE PREPARE s3;

-- -------------------------------------------------------------
-- 4. Tornar id_usuario NOT NULL na ficha
-- -------------------------------------------------------------
ALTER TABLE tb_ficha_solicitacao
    MODIFY COLUMN id_usuario BIGINT NOT NULL;

-- -------------------------------------------------------------
-- 5. Remover FK de id_usuario da tb_solicitacao (se existir)
-- -------------------------------------------------------------
SET @fk_sol_usuario = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME        = 'tb_solicitacao'
      AND CONSTRAINT_NAME   = 'tb_solicitacao_ibfk_3'
      AND CONSTRAINT_TYPE   = 'FOREIGN KEY'
);
SET @sql5 = IF(@fk_sol_usuario > 0,
    'ALTER TABLE tb_solicitacao DROP FOREIGN KEY tb_solicitacao_ibfk_3',
    'SELECT 1'
);
PREPARE s5 FROM @sql5; EXECUTE s5; DEALLOCATE PREPARE s5;

-- -------------------------------------------------------------
-- 6. Remover coluna id_usuario da tb_solicitacao (se existir)
-- -------------------------------------------------------------
SET @col_sol_usuario = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'tb_solicitacao'
      AND COLUMN_NAME  = 'id_usuario'
);
SET @sql6 = IF(@col_sol_usuario > 0,
    'ALTER TABLE tb_solicitacao DROP COLUMN id_usuario',
    'SELECT 1'
);
PREPARE s6 FROM @sql6; EXECUTE s6; DEALLOCATE PREPARE s6;

-- -------------------------------------------------------------
-- 7. Remover FK de id_carro da tb_solicitacao (se existir)
-- -------------------------------------------------------------
SET @fk_carro = (
    SELECT COUNT(*) FROM information_schema.TABLE_CONSTRAINTS
    WHERE CONSTRAINT_SCHEMA = DATABASE()
      AND TABLE_NAME        = 'tb_solicitacao'
      AND CONSTRAINT_NAME   = 'tb_solicitacao_ibfk_1'
      AND CONSTRAINT_TYPE   = 'FOREIGN KEY'
);
SET @sql7 = IF(@fk_carro > 0,
    'ALTER TABLE tb_solicitacao DROP FOREIGN KEY tb_solicitacao_ibfk_1',
    'SELECT 1'
);
PREPARE s7 FROM @sql7; EXECUTE s7; DEALLOCATE PREPARE s7;

-- -------------------------------------------------------------
-- 8. Remover índice de id_carro (se existir)
-- -------------------------------------------------------------
SET @idx_carro = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'tb_solicitacao'
      AND INDEX_NAME   = 'idx_solicitacao_carro'
);
SET @sql8 = IF(@idx_carro > 0,
    'ALTER TABLE tb_solicitacao DROP INDEX idx_solicitacao_carro',
    'SELECT 1'
);
PREPARE s8 FROM @sql8; EXECUTE s8; DEALLOCATE PREPARE s8;

-- Índice gerado automaticamente pelo MySQL
SET @idx_carro2 = (
    SELECT COUNT(*) FROM information_schema.STATISTICS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'tb_solicitacao'
      AND INDEX_NAME   = 'id_carro'
);
SET @sql9 = IF(@idx_carro2 > 0,
    'ALTER TABLE tb_solicitacao DROP INDEX id_carro',
    'SELECT 1'
);
PREPARE s9 FROM @sql9; EXECUTE s9; DEALLOCATE PREPARE s9;

-- -------------------------------------------------------------
-- 9. Remover coluna id_carro da tb_solicitacao (se existir)
-- -------------------------------------------------------------
SET @col_carro = (
    SELECT COUNT(*) FROM information_schema.COLUMNS
    WHERE TABLE_SCHEMA = DATABASE()
      AND TABLE_NAME   = 'tb_solicitacao'
      AND COLUMN_NAME  = 'id_carro'
);
SET @sql10 = IF(@col_carro > 0,
    'ALTER TABLE tb_solicitacao DROP COLUMN id_carro',
    'SELECT 1'
);
PREPARE s10 FROM @sql10; EXECUTE s10; DEALLOCATE PREPARE s10;