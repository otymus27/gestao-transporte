CREATE TABLE IF NOT EXISTS tb_ficha_solicitacao (
                                                    id              BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                    data_viagem     DATE        NOT NULL,
                                                    placa_veiculo   VARCHAR(10) NOT NULL,
    data_criacao    DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    id_usuario      BIGINT,
    CONSTRAINT fk_ficha_usuario FOREIGN KEY (id_usuario) REFERENCES tb_usuarios(id)
    );

-- Adiciona a FK na tabela de solicitações já existente
ALTER TABLE tb_solicitacao
    ADD COLUMN id_ficha BIGINT NULL,
    ADD CONSTRAINT fk_solicitacao_ficha
        FOREIGN KEY (id_ficha) REFERENCES tb_ficha_solicitacao(id) ON DELETE CASCADE;