-- Tabela de Pastas de Formulários
CREATE TABLE IF NOT EXISTS tb_pasta_formulario (
                                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                                   nome_pasta VARCHAR(255) NOT NULL,
    descricao VARCHAR(500),
    caminho_completo VARCHAR(1024) NOT NULL,
    data_criacao DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_atualizacao DATETIME(6) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    criado_por_id BIGINT,
    CONSTRAINT fk_pasta_formulario_usuario FOREIGN KEY (criado_por_id) REFERENCES tb_usuarios(id)
    );
