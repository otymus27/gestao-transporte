-- Tabela de Formulários
CREATE TABLE IF NOT EXISTS tb_formulario (
                                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                             nome_arquivo VARCHAR(255) NOT NULL,
    caminho_armazenamento VARCHAR(1024) NOT NULL,
    tamanho BIGINT NOT NULL,
    data_upload DATETIME(6) NOT NULL DEFAULT CURRENT_TIMESTAMP(6),
    data_atualizacao DATETIME(6) DEFAULT NULL ON UPDATE CURRENT_TIMESTAMP(6),
    criado_por_id BIGINT,
    pasta_formulario_id BIGINT NOT NULL,

    CONSTRAINT fk_formulario_usuario FOREIGN KEY (criado_por_id) REFERENCES tb_usuarios(id),
    CONSTRAINT fk_formulario_pasta FOREIGN KEY (pasta_formulario_id) REFERENCES tb_pasta_formulario(id)
    );
