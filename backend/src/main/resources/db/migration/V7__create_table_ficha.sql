-- =============================================================
-- V4__criar_ficha_solicitacao.sql
-- Migration segura para produção — preserva todos os dados
-- existentes em tb_solicitacao
-- =============================================================

-- -------------------------------------------------------------
-- PASSO 1: Criar a tabela master FichaSolicitacao
-- -------------------------------------------------------------
CREATE TABLE IF NOT EXISTS tb_ficha_solicitacao (
                                                    id               BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                    data_viagem      DATE        NOT NULL,
                                                    placa_veiculo    VARCHAR(10) NOT NULL,
    data_criacao     DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_atualizacao DATETIME(6) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    criado_por_id    BIGINT      DEFAULT NULL,
    CONSTRAINT fk_ficha_usuario FOREIGN KEY (criado_por_id) REFERENCES tb_usuarios(id)
    );

-- -------------------------------------------------------------
-- PASSO 2: Para cada solicitação existente, criar uma ficha
-- usando data_solicitacao e a placa do carro vinculado.
-- Cada solicitação antiga gera uma ficha individual.
-- -------------------------------------------------------------
INSERT INTO tb_ficha_solicitacao (data_viagem, placa_veiculo, criado_por_id)
SELECT
    s.data_solicitacao,
    c.placa,
    s.id_usuario
FROM tb_solicitacao s
         INNER JOIN tb_carro c ON c.id = s.id_carro
ORDER BY s.id;

-- -------------------------------------------------------------
-- PASSO 3: Adicionar coluna id_ficha em tb_solicitacao
-- Nullable inicialmente para permitir o preenchimento a seguir
-- -------------------------------------------------------------
ALTER TABLE tb_solicitacao
    ADD COLUMN id_ficha BIGINT DEFAULT NULL,
    ADD CONSTRAINT fk_solicitacao_ficha
        FOREIGN KEY (id_ficha) REFERENCES tb_ficha_solicitacao(id)
        ON DELETE CASCADE;

-- -------------------------------------------------------------
-- PASSO 4: Vincular cada solicitação à sua ficha
-- Match por data + placa + usuario para evitar ambiguidade
-- -------------------------------------------------------------
UPDATE tb_solicitacao s
    INNER JOIN tb_carro c ON c.id = s.id_carro
    INNER JOIN tb_ficha_solicitacao f
    ON  f.data_viagem   = s.data_solicitacao
    AND f.placa_veiculo = c.placa
    AND f.criado_por_id = s.id_usuario
    SET s.id_ficha = f.id;

-- -------------------------------------------------------------
-- PASSO 5: Tornar id_ficha NOT NULL após preenchimento
-- ATENÇÃO: Antes de rodar em produção, valide manualmente:
--   SELECT COUNT(*) FROM tb_solicitacao WHERE id_ficha IS NULL;
-- O resultado deve ser 0 antes de continuar.
-- -------------------------------------------------------------
ALTER TABLE tb_solicitacao
    MODIFY COLUMN id_ficha BIGINT NOT NULL;

-- -------------------------------------------------------------
-- PASSO 6: Tornar id_carro NULLABLE — não remove ainda
-- Preserva histórico e evita quebra imediata.
-- Remoção definitiva ocorre na V5 após validação em produção.
-- -------------------------------------------------------------
ALTER TABLE tb_solicitacao
DROP FOREIGN KEY tb_solicitacao_ibfk_1,
    MODIFY COLUMN id_carro BIGINT DEFAULT NULL;

-- Mantém índice para performance sem FK
ALTER TABLE tb_solicitacao
    ADD INDEX idx_solicitacao_carro (id_carro);